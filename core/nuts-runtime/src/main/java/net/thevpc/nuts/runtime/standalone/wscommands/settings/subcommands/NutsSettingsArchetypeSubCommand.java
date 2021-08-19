/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public class NutsSettingsArchetypeSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list archetypes", "la") != null) {
            if (cmdLine.isExecMode()) {
                session.getWorkspace().formats().object(session.getWorkspace().config().getAvailableArchetypes())
                        .println();
            }
            return true;
        }
        return false;
    }
}
