/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.common.commandline.CommandLine;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class ArchietypeConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.readAll("list archetypes", "la")) {
            PrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getValidWorkspace().getRepositoryManager().getAvailableArchetypes()) {
                    out.printf("%s\n", archetype);
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
