/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.FileNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.NutsIdNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.common.io.FileUtils;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class InstallCommand extends AbstractNutsCommand {

    public InstallCommand() {
        super("install", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args,context);
        cmdLine.requireNonEmpty();
        boolean force = false;
        boolean deployOnly = false;
        boolean bundleOnly = false;
        String repositoryId = null;
        String descriptorFile = null;
        do {
            if (cmdLine.readOnce("-f", "--force")) {
                force = true;
            } else if (cmdLine.readOnce("-r", "--repository")) {
                repositoryId = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else if (cmdLine.readOnce("-s", "--descriptor")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getString();
            } else if (cmdLine.readOnce("-t", "--target")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("Target")).getString();
            } else if (cmdLine.readOnce("-y", "--deploy", "--no-install")) {
                deployOnly = true;
                bundleOnly = false;
            } else if (cmdLine.readOnce("-b", "--bundle")) {
                deployOnly = false;
                bundleOnly = true;
            } else {
                String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    NutsPrintStream out = context.getTerminal().getFormattedOut();
                    if (deployOnly) {
                        for (String s : context.expandPath(id)) {
                            NutsId deployedId = context.getValidWorkspace().deploy(
                                    new NutsDeployment()
                                            .setContentPath(s)
                                            .setDescriptorPath(descriptorFile)
                                            .setRepositoryId(repositoryId),
                                    context.getSession()
                            );
                            out.printf("File %s deployed successfully as %s\n", s, deployedId);
                        }
                    } else if (bundleOnly) {
                        for (String s : context.expandPath(id)) {
                            NutsFile deployedId = context.getValidWorkspace().createBundle(
                                    FileUtils.getAbsolutePath(new File(context.getCwd()), s),
                                    descriptorFile == null ? null :
                                            new File(context.resolvePath(descriptorFile)).getPath(),
                                    context.getSession()
                            );
                            out.printf("File %s bundled successfully as %s to %s\n", s, deployedId.getId(), deployedId.getFile());
                        }
                    } else {

                        for (String s : context.expandPath(id)) {
                            if (FileUtils.isFilePath(s)) {
                                //this is a file to deploy first
                                NutsId deployedId = context.getValidWorkspace().deploy(
                                        new NutsDeployment()
                                                .setContentPath(s)
                                                .setDescriptorPath(descriptorFile)
                                                .setRepositoryId(repositoryId),
                                        context.getSession()
                                );
                                out.printf("File %s deployed successfully as %s\n", s, deployedId);
                                s = deployedId.toString();
                            }
                            logInstallStatus(s, context, force);
                        }
                    }
                }
                descriptorFile = null;
            }
        } while (!cmdLine.isEmpty());
        return 0;
    }

    private NutsFile logInstallStatus(String s, NutsCommandContext context, boolean force) {
        NutsTerminal terminal = context.getTerminal();
        NutsFile file = null;
        NutsPrintStream out = terminal.getFormattedOut();
        try {
            file = context.getValidWorkspace().install(s, force, context.getSession());
        } catch (NutsAlreadytInstalledException ex) {
            out.printf("%s already installed\n", s);
            return null;
        } catch (NutsNotInstallableException ex) {
            out.printf("%s requires no installation. It should be usable as is.\n", s);
            return null;
        }
        if (!file.isInstalled()) {
            if (!file.isCached()) {
                if (file.isTemporary()) {
                    out.printf("%s installed successfully from temporarily file %s\n", file.getId(), file.getFile());
                } else {
                    out.printf("%s installed successfully from remote repository\n", file.getId());
                }
            } else {
                if (file.isTemporary()) {
                    out.printf("%s installed from local temporarily file %s \n", file.getId(), file.getFile());
                } else {
                    out.printf("%s installed from local repository\n", file.getId());
                }
            }
        } else {
            out.printf("%s installed successfully\n", file.getId());
        }
        return file;
    }
}
