/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsExtensionInfo;
import net.vpc.app.nuts.NutsWorkspaceExtension;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.app.options.ExtensionNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;

/**
 * @author vpc
 */
public class ExtensionNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        if (cmdLine.readAll("add extension", "ax")) {
            String extensionId = cmdLine.readRequiredNonOption(new ExtensionNonOption("ExtensionNutsId", context.getWorkspace())).getStringExpression();
            if (cmdLine.isExecMode()) {
                context.getWorkspace().extensions().addWorkspaceExtension(context.getWorkspace().parser().parseId(extensionId), context.getSession());
            }
            while (cmdLine.hasNext()) {
                extensionId = cmdLine.readRequiredNonOption(new ExtensionNonOption("ExtensionNutsId", context.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {
                    context.getWorkspace().extensions().addWorkspaceExtension(context.getWorkspace().parser().parseId(extensionId), context.getSession());
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else {
            PrintStream out = context.getTerminal().fout();
            if (cmdLine.readAll("list extensions", "lx")) {
                if (cmdLine.isExecMode()) {
                    for (NutsWorkspaceExtension extension : context.getWorkspace().extensions().getWorkspaceExtensions()) {
                        NutsDescriptor desc = context.getWorkspace().find().id(extension.getWiredId()).setSession(context.getSession()).getResultDefinitions().required().getDescriptor();
                        String extDesc = StringUtils.trim(desc.getName());
                        if (!extDesc.isEmpty()) {
                            extDesc = " : " + extDesc;
                        }
                        if (!extension.getId().equals(extension.getWiredId())) {
                            out.printf("%s (%s) : ", extension.getId(), extension.getWiredId());
                            out.println(extDesc);
                        } else {
                            out.printf("%s%s", extension.getId(), extDesc);
                        }
                    }
                }
                return true;
            } else if (cmdLine.readAll("find extensions", "fx")) {
                if (cmdLine.isExecMode()) {
                    for (NutsExtensionInfo extension : context.getWorkspace().extensions().findWorkspaceExtensions(context.getSession())) {
                        NutsDescriptor desc = context.getWorkspace().find().id(extension.getId()).setSession(context.getSession()).getResultDefinitions().required().getDescriptor();
                        String extDesc = StringUtils.trim(desc.getName());
                        if (!extDesc.isEmpty()) {
                            extDesc = " : " + extDesc;
                        }
                        out.printf("%s : ", extension.getId());
                        out.println(extDesc);
                    }
                }
                return true;
            } else if (cmdLine.readAll("list extension points", "lxp")) {
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
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
