/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.common.commandline.CommandLine;

/**
 *
 * @author vpc
 */
public class ArchietypeConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("list archetypes", "la")) {
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getValidWorkspace().getRepositoryManager().getAvailableArchetypes()) {
                    context.getTerminal().getOut().printf("%s\n", archetype);
                }
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
