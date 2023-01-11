/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.archetype;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NSettingsArchetypeSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("list archetypes", "la").isPresent()) {
            if (cmdLine.isExecMode()) {
                session.out().println(NConfigs.of(session).getAvailableArchetypes());
            }
            return true;
        }
        return false;
    }
}
