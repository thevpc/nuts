/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.io.PrintStream;

import net.vpc.app.nuts.NutsCommandLine;

/**
 * @author vpc
 */
public class ExtensionNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
//        if (cmdLine.next("add extension", "ax") != null) {
//            String extensionId = cmdLine.required().nextNonOption(cmdLine.createName("extension")).getString();
//            if (cmdLine.isExecMode()) {
////                context.getWorkspace().extensions().addWorkspaceExtension(context.getWorkspace().id().parse(extensionId), context.getSession());
//            }
//            while (cmdLine.hasNext()) {
//                extensionId = cmdLine.required().nextNonOption(cmdLine.createName("extension")).getString();
//                if (cmdLine.isExecMode()) {
////                    context.getWorkspace().extensions().addWorkspaceExtension(context.getWorkspace().id().parse(extensionId), context.getSession());
//                }
//            }
//            if (cmdLine.isExecMode()) {
//                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
//            }
//            return true;
//        } else {
//            PrintStream out = context.session().getTerminal().fout();
//            if (cmdLine.next("list extensions", "lx") != null) {
//                if (cmdLine.isExecMode()) {
//                    for (NutsWorkspaceExtension extension : context.getWorkspace().extensions().getWorkspaceExtensions()) {
//                        NutsDescriptor desc = context.getWorkspace().search().id(extension.getWiredId()).setSession(context.getSession()).getResultDefinitions().required().getDescriptor();
//                        String extDesc = StringUtils.trim(desc.getName());
//                        if (!extDesc.isEmpty()) {
//                            extDesc = " : " + extDesc;
//                        }
//                        if (!extension.getId().equals(extension.getWiredId())) {
//                            out.printf("%s (%s) : ", extension.getId(), extension.getWiredId());
//                            out.println(extDesc);
//                        } else {
//                            out.printf("%s%s", extension.getId(), extDesc);
//                        }
//                    }
//                }
//                return true;
//            } else if (cmdLine.next("find extensions", "fx") != null) {
//                if (cmdLine.isExecMode()) {
//                    for (NutsExtensionInfo extension : context.getWorkspace().extensions().findWorkspaceExtensions(context.getSession())) {
//                        NutsDescriptor desc = context.getWorkspace().search().id(extension.getId()).setSession(context.getSession()).getResultDefinitions().required().getDescriptor();
//                        String extDesc = StringUtils.trim(desc.getName());
//                        if (!extDesc.isEmpty()) {
//                            extDesc = " : " + extDesc;
//                        }
//                        out.printf("%s : ", extension.getId());
//                        out.println(extDesc);
//                    }
//                }
//                return true;
//            } else 
        if (cmdLine.next("list extension points", "lxp") != null) {
            PrintStream out = context.session().getTerminal().fout();
            if (cmdLine.isExecMode()) {
                for (Class extension : context.getWorkspace().extensions().getExtensionPoints()) {
                    out.printf("[[%s]]:%n", extension.getName());
                    for (Class impl : context.getWorkspace().extensions().getExtensionTypes(extension)) {
                        out.printf("\t%s%n", impl.getName());
                    }
                    for (Object impl : context.getWorkspace().extensions().getExtensionObjects(extension)) {
                        if (impl != null) {
                            out.printf("\t%s :: %s%n", impl.getClass().getName(), impl);
                        } else {
                            out.printf("\tnull%n");
                        }
                    }
                }
            }
            return true;
        }
//        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
