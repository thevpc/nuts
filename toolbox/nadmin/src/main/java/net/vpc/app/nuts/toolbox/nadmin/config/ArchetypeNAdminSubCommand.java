/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSupportLevelContext;

/**
 *
 * @author vpc
 */
public class ArchetypeNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list archetypes", "la") != null) {
            if (cmdLine.isExecMode()) {
                context.getSession().formatObject(context.getWorkspace().config().getAvailableArchetypes())
                        .println();
            }
            return true;
        }
        return false;
    }
}
