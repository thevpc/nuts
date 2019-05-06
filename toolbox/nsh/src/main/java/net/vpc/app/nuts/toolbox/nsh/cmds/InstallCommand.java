///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <p>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <p>
// * Copyright (C) 2016-2017 Taha BEN SALAH
// * <p>
// * This program is free software; you can redistribute it and/or modify it under
// * the terms of the GNU General Public License as published by the Free Software
// * Foundation; either version 3 of the License, or (at your option) any later
// * version.
// * <p>
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// * details.
// * <p>
// * You should have received a copy of the GNU General Public License along with
// * this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// * ====================================================================
// */
//package net.vpc.app.nuts.toolbox.nsh.cmds;
//
//import net.vpc.app.nuts.*;
//import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
//import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
//import net.vpc.app.nuts.app.options.NutsIdNonOption;
//import net.vpc.app.nuts.app.options.RepositoryNonOption;
//import net.vpc.common.commandline.Argument;
//import net.vpc.common.commandline.FileNonOption;
//import net.vpc.common.io.FileUtils;
//
///**
// * Created by vpc on 1/7/17.
// */
//public class InstallCommand extends AbstractNutsCommand {
//
//    public InstallCommand() {
//        super("install", DEFAULT_SUPPORT);
//    }
//
//    @Override
//    public int exec(String[] args, NutsCommandContext context) throws Exception {
//        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
//        NutsInstallCommand options = context.getWorkspace().install().setSession(context.getSession()).setTrace(true);
//        boolean deployOnly = false;
//        String repositoryId = null;
//        String descriptorFile = null;
//        Argument a;
//        do {
//            if (context.configure(cmdLine)) {
//                //
//            } else if (cmdLine.readAllOnce("-f", "--force")) {
//                options.setForce(true);
//            } else if (cmdLine.readAllOnce("-i", "--ignore")) {
//                options.setForce(false);
//            } else {
//                NutsWorkspace ws = context.getWorkspace();
//                if (cmdLine.readAllOnce("-r", "--repository")) {
//                    repositoryId = cmdLine.readNonOption(new RepositoryNonOption("Repository", ws)).getStringExpression();
//                } else if (cmdLine.readAllOnce("-s", "--descriptor")) {
//                    descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getStringExpression();
//                } else if (cmdLine.readAllOnce("-t", "--target")) {
//                    descriptorFile = cmdLine.readNonOption(new FileNonOption("Target")).getStringExpression();
//                } else if (cmdLine.readAllOnce("-y", "--deploy", "--no-install")) {
//                    deployOnly = true;
//                } else {
//                    String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression();
//                    if (cmdLine.isExecMode()) {
//                        if (deployOnly) {
//                            for (String s : context.getShell().expandPath(id)) {
//                                NutsId deployedId = ws.deploy()
//                                        .setContent(s)
//                                        .setDescriptor(descriptorFile)
//                                        .setRepository(repositoryId)
//                                        .setSession(context.getSession())
//                                        .getResult()[0];
//                                context.out().printf("File %s deployed successfully as %N\n", s, ws.formatter().createIdFormat().toString(deployedId));
//                            }
//                        } else {
//
//                            for (String s : context.getShell().expandPath(id)) {
//                                if (FileUtils.isFilePath(s)) {
//                                    //this is a file to deploy first
//                                    NutsId deployedId = ws.deploy()
//                                            .setContent(s)
//                                            .setDescriptor(descriptorFile)
//                                            .repository(repositoryId)
//                                            .setSession(context.getSession())
//                                            .getResult()[0];
//                                    context.out().printf("File %s deployed successfully as %N\n", s,ws.formatter().createIdFormat().toString(deployedId));
//                                    s = deployedId.toString();
//                                }
//                                logInstallStatus(s, context, options);
//                            }
//                        }
//                    }
//                    descriptorFile = null;
//                }
//            }
//        } while (cmdLine.hasNext());
//        return 0;
//    }
//
//    private NutsDefinition logInstallStatus(String s, NutsCommandContext context, NutsInstallCommand installCommand) {
//        NutsDefinition file = null;
//        NutsWorkspace ws = context.getWorkspace();
//        try {
//            file = installCommand.id(s).getResult()[0];
//        } catch (NutsAlreadyInstalledException ex) {
//            context.out().printf("%s already installed\n", s);
//            return null;
//        } catch (NutsNotInstallableException ex) {
//            context.out().printf("%s requires no installation. It should be usable as is.\n", s);
//            return null;
//        }
//
//        return file;
//    }
//}
