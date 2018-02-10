/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;

/**
 *
 * @author vpc
 */
public class ImportConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("list imports", "li")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getValidWorkspace().getImports())) {
                    context.getTerminal().getOut().println(imp);
                }
            }
            return true;
        } else if (cmdLine.read("clear imports", "ci")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().removeAllImports();
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("import", "ia")) {
            do {
                String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().addImports(a);
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
                    context.getValidWorkspace().removeImports(ii);
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
        return CORE_SUPPORT;
    }

}
