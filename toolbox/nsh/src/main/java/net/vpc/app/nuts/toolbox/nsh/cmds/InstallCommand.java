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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.app.nuts.app.options.RepositoryNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.FileNonOption;
import net.vpc.common.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by vpc on 1/7/17.
 */
public class InstallCommand extends AbstractNutsCommand {

    public InstallCommand() {
        super("install", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsInstallOptions options = new NutsInstallOptions().setTrace(true);
        NutsQueryOptions qoptions = context.getWorkspace().createQueryOptions();
        boolean deployOnly = false;
        boolean bundleOnly = false;
        String repositoryId = null;
        String descriptorFile = null;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAllOnce("-f", "--force")) {
                options.setForce(true);
            }else if (cmdLine.readAllOnce("-i", "--ignore")) {
                options.setForce(false);
            } else {
                NutsWorkspace ws = context.getWorkspace();
                if (cmdLine.readAllOnce("-r", "--repository")) {
                    repositoryId = cmdLine.readNonOption(new RepositoryNonOption("Repository", ws)).getStringExpression();
                } else if (cmdLine.readAllOnce("-s", "--descriptor")) {
                    descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getStringExpression();
                } else if (cmdLine.readAllOnce("-t", "--target")) {
                    descriptorFile = cmdLine.readNonOption(new FileNonOption("Target")).getStringExpression();
                } else if (cmdLine.readAllOnce("-y", "--deploy", "--no-install")) {
                    deployOnly = true;
                    bundleOnly = false;
                } else if (cmdLine.readAllOnce("-b", "--bundle")) {
                    deployOnly = false;
                    bundleOnly = true;
                } else {
                    String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression();
                    if (cmdLine.isExecMode()) {
                        if (deployOnly) {
                            for (String s : context.getShell().expandPath(id)) {
                                NutsId deployedId = ws.deploy(
                                        ws.createDeploymentBuilder()
                                                .setContent(s)
                                                .setDescriptorPath(descriptorFile)
                                                .setRepository(repositoryId)
                                        .build()
                                        ,
                                        context.getSession()
                                );
                                context.out().printf("File %s deployed successfully as "+ws.formatter().createIdFormat().format(deployedId)+"\n", s, deployedId);
                            }
                        } else if (bundleOnly) {
                            for (String s : context.getShell().expandPath(id)) {
                                NutsDefinition deployedFileId = ws.createBundle(
                                        ws.io().path(context.getShell().getCwd()).resolve(s).normalize(),
                                        descriptorFile == null ? null :
                                                ws.io().path(context.getShell().getAbsolutePath(descriptorFile)),
                                        qoptions,
                                        context.getSession()
                                );
                                context.out().printf("File %s bundled successfully as "+ws.formatter().createIdFormat()
                                        .format(deployedFileId.getId())+" to %s\n", s, deployedFileId.getContent().getPath());
                            }
                        } else {

                            for (String s : context.getShell().expandPath(id)) {
                                if (FileUtils.isFilePath(s)) {
                                    //this is a file to deploy first
                                    NutsId deployedId = ws.deploy(
                                            ws.createDeploymentBuilder()
                                                    .setContent(s)
                                                    .setDescriptorPath(descriptorFile)
                                                    .setRepository(repositoryId)
                                            .build()
                                            ,
                                            context.getSession()
                                    );
                                    context.out().printf("File %s deployed successfully as "+ws.formatter().createIdFormat().format(deployedId)+"\n", s);
                                    s = deployedId.toString();
                                }
                                logInstallStatus(s, context, options);
                            }
                        }
                    }
                    descriptorFile = null;
                }
            }
        } while (cmdLine.hasNext());
        return 0;
    }

    private NutsDefinition logInstallStatus(String s, NutsCommandContext context, NutsInstallOptions options) {
        NutsDefinition file = null;
        NutsWorkspace ws = context.getWorkspace();
        try {
            file = ws.install(s, new String[0], options, context.getSession());
        } catch (NutsAlreadyInstalledException ex) {
            context.out().printf("%s already installed\n", s);
            return null;
        } catch (NutsNotInstallableException ex) {
            context.out().printf("%s requires no installation. It should be usable as is.\n", s);
            return null;
        }
        
        return file;
    }
}
