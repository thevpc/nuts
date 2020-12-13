/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.config;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsCommandLine;

/**
 *
 * @author thevpc
 */
public class ArchetypeNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list archetypes", "la") != null) {
            if (cmdLine.isExecMode()) {
                context.getSession().formatObject(context.getWorkspace().config().getAvailableArchetypes(context.getSession()))
                        .println();
            }
            return true;
        }
        return false;
    }
}
