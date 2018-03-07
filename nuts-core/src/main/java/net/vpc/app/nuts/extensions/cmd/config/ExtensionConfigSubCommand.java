/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsWorkspaceExtension;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.ExtensionNonOption;
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
                context.getValidWorkspace().getExtensionManager().addExtension(extensionId, context.getSession());
            }
            while (!cmdLine.isEmpty()) {
                extensionId = cmdLine.readNonOptionOrError(new ExtensionNonOption("ExtensionNutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getExtensionManager().addExtension(extensionId, context.getSession());
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("list extensions", "lx")) {
            if (cmdLine.isExecMode()) {
                for (NutsWorkspaceExtension extension : context.getValidWorkspace().getExtensionManager().getExtensions()) {
                    NutsDescriptor desc = context.getValidWorkspace().fetchDescriptor(extension.getWiredId().toString(), false, context.getSession());
                    String extDesc = CoreStringUtils.trim(desc.getName());
                    if (!extDesc.isEmpty()) {
                        extDesc = " : " + extDesc;
                    }
                    if (!extension.getId().equals(extension.getWiredId())) {
                        context.getTerminal().getOut().printf("%s (%s)%s", extension.getId(), extension.getWiredId(), extDesc);
                    } else {
                        context.getTerminal().getOut().printf("%s%s", extension.getId(), extDesc);
                    }
                }
            }
            return true;
        } else if (cmdLine.read("list extension points", "lxp")) {
            if (cmdLine.isExecMode()) {
                for (Class extension : context.getValidWorkspace().getExtensionManager().getFactory().getExtensionPoints()) {
                    context.getTerminal().getOut().printf("[[%s]]:\n", extension.getName());
                    for (Class impl : context.getValidWorkspace().getExtensionManager().getFactory().getExtensionTypes(extension)) {
                        context.getTerminal().getOut().printf("\t%s\n", impl.getName());
                    }
                    for (Object impl : context.getValidWorkspace().getExtensionManager().getFactory().getExtensionObjects(extension)) {
                        if (impl != null) {
                            context.getTerminal().getOut().printf("\t%s :: %s\n", impl.getClass().getName(), impl);
                        } else {
                            context.getTerminal().getOut().printf("\tnull\n");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
