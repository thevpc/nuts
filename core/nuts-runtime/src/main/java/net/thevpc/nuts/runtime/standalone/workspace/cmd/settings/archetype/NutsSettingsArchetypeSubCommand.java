/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.archetype;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NutsSettingsArchetypeSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list archetypes", "la").isPresent()) {
            if (cmdLine.isExecMode()) {
                session.out().printlnf(session.config().getAvailableArchetypes());
            }
            return true;
        }
        return false;
    }
}
