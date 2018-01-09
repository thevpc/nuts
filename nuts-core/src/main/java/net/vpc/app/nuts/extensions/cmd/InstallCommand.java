/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.NutsIdNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class InstallCommand extends AbstractNutsCommand {

    public InstallCommand() {
        super("install", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        cmdLine.requireNonEmpty();
        boolean force = false;
        boolean deployOnly = false;
        boolean bundleOnly = false;
        String repositoryId = null;
        String descriptorFile = null;
        do {
            if (cmdLine.acceptAndRemoveNoDuplicates("-f", "--force")) {
                force = true;
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-r", "--repository")) {
                repositoryId = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-s", "--descriptor")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getString();
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-t", "--target")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("Target")).getString();
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-y", "--deploy", "--no-install")) {
                deployOnly = true;
                bundleOnly = false;
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-b", "--bundle")) {
                deployOnly = false;
                bundleOnly = true;
            } else {
                String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    if (deployOnly) {
                        for (String s : CoreIOUtils.expandPath(id,new File(context.getCommandLine().getCwd()))) {
                            NutsId deployedId = context.getValidWorkspace().deploy(
                                    s,
                                    null,
                                    descriptorFile,
                                    null,
                                    repositoryId,
                                    context.getSession()
                            );
                            context.getTerminal().getOut().println("File " + s + " deployed successfully as " + deployedId);
                        }
                    } else if (bundleOnly) {
                        for (String s : CoreIOUtils.expandPath(id,new File(context.getCommandLine().getCwd()))) {
                            NutsFile deployedId = context.getValidWorkspace().createBundle(
                                    CoreIOUtils.createFileByCwd(s,new File(context.getCommandLine().getCwd())),
                                    descriptorFile == null ? null : CoreIOUtils.createFileByCwd(descriptorFile,new File(context.getCommandLine().getCwd())),
                                    context.getSession()
                            );
                            context.getTerminal().getOut().println("File " + s + " bundled successfully as " + deployedId.getId() + " to " + deployedId.getFile());
                        }
                    } else {

                        for (String s : CoreIOUtils.expandPath(id,new File(context.getCommandLine().getCwd()))) {
                            if (CoreIOUtils.isFilePath(s)) {
                                //this is a file to deploy first
                                NutsId deployedId = context.getValidWorkspace().deploy(
                                        s,
                                        null,
                                        descriptorFile,
                                        null,
                                        repositoryId,
                                        context.getSession()
                                );
                                context.getTerminal().getOut().println("File " + s + " deployed successfully as " + deployedId);
                                s = deployedId.toString();
                            }
                            NutsFile file = context.getValidWorkspace().install(s, context.getSession());
                            logInstallStatus(file, context.getTerminal());
                            if (file.isInstalled()) {
                                if (force) {
                                    context.getValidWorkspace().uninstall(s, context.getSession());
                                    file = context.getValidWorkspace().install(s, context.getSession());
                                    logInstallStatus(file, context.getTerminal());
                                }
                            }
                        }
                    }
                }
                descriptorFile = null;
            }
        } while (!cmdLine.isEmpty());
    }

    private void logInstallStatus(NutsFile file, NutsTerminal terminal) {
        if (!file.isInstalled()) {
            if (!file.isCached()) {
                if (file.isTemporary()) {
                    terminal.getOut().println(file.getId() + " installed successfully from temporarily file " + file.getFile().getPath());
                } else {
                    terminal.getOut().println(file.getId() + " installed successfully from remote repository");
                }
            } else {
                if (file.isTemporary()) {
                    terminal.getOut().println(file.getId() + " installed from local temporarily file " + file.getFile().getPath());
                } else {
                    terminal.getOut().println(file.getId() + " installed from local repository");
                }
            }
        } else {
            terminal.getOut().println(file.getId() + " already installed");
        }
    }
}
