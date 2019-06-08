/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public class ArchetypeNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list archetypes", "la")!=null) {
            PrintStream out = context.session().getTerminal().fout();
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getWorkspace().config().getAvailableArchetypes()) {
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
