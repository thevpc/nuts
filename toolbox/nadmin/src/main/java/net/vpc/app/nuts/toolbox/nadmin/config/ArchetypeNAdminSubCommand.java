/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.commandline.CommandLine;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class ArchetypeNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.readAll("list archetypes", "la")) {
            PrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getWorkspace().repositories().getAvailableArchetypes()) {
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
