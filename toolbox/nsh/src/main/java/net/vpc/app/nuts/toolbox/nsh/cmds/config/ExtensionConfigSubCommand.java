/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.ExtensionNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.commandline.CommandLine;

/**
 * @author vpc
 */
public class ExtensionConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        if (cmdLine.read("add extension", "ax")) {
            String extensionId = cmdLine.readNonOptionOrError(new ExtensionNonOption("ExtensionNutsId", context)).getString();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().getExtensionManager().addWorkspaceExtension(extensionId, context.getSession());
            }
            while (!cmdLine.isEmpty()) {
                extensionId = cmdLine.readNonOptionOrError(new ExtensionNonOption("ExtensionNutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getExtensionManager().addWorkspaceExtension(extensionId, context.getSession());
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else {
            NutsPrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.read("list extensions", "lx")) {
                if (cmdLine.isExecMode()) {
                    for (NutsWorkspaceExtension extension : context.getValidWorkspace().getExtensionManager().getWorkspaceExtensions()) {
                        NutsDescriptor desc = context.getValidWorkspace().fetchDescriptor(extension.getWiredId().toString(), false, context.getSession());
                        String extDesc = CoreStringUtils.trim(desc.getName());
                        if (!extDesc.isEmpty()) {
                            extDesc = " : " + extDesc;
                        }
                        if (!extension.getId().equals(extension.getWiredId())) {
                            out.printf("%s (%s) : ", extension.getId(), extension.getWiredId());
                            out.println(extDesc);
                        } else {
                            out.printf("%s%s\n", extension.getId(), extDesc);
                        }
                    }
                }
                return true;
            } else if (cmdLine.read("find extensions", "fx")) {
                if (cmdLine.isExecMode()) {
                    for (NutsExtensionInfo extension : context.getValidWorkspace().getExtensionManager().findWorkspaceExtensions(context.getSession())) {
                        NutsDescriptor desc = context.getValidWorkspace().fetchDescriptor(extension.getId().toString(), false, context.getSession());
                        String extDesc = CoreStringUtils.trim(desc.getName());
                        if (!extDesc.isEmpty()) {
                            extDesc = " : " + extDesc;
                        }
                        out.printf("%s : ", extension.getId());
                        out.println(extDesc);
                    }
                }
                return true;
            } else if (cmdLine.read("list extension points", "lxp")) {
                if (cmdLine.isExecMode()) {
                    for (Class extension : context.getValidWorkspace().getExtensionManager().getExtensionPoints()) {
                        out.printf("[[%s]]:\n", extension.getName());
                        for (Class impl : context.getValidWorkspace().getExtensionManager().getExtensionTypes(extension)) {
                            out.printf("\t%s\n", impl.getName());
                        }
                        for (Object impl : context.getValidWorkspace().getExtensionManager().getExtensionObjects(extension)) {
                            if (impl != null) {
                                out.printf("\t%s :: %s\n", impl.getClass().getName(), impl);
                            } else {
                                out.printf("\tnull\n");
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
