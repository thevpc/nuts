/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;

/**
 *
 * @author vpc
 */
public class ImportConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("list imports", "li")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getValidWorkspace().getConfigManager().getImports())) {
                    context.getTerminal().getFormattedOut().printf("%s\n", imp);
                }
            }
            return true;
        } else if (cmdLine.read("clear imports", "ci")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().getConfigManager().removeAllImports();
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("import", "ia")) {
            do {
                String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfigManager().addImports(a);
                }
            } while (!cmdLine.isEmpty());
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("unimport", "ir")) {
            while (!cmdLine.isEmpty()) {
                String ii = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfigManager().removeImports(ii);
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
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
