/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.archetype;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NSettingsArchetypeSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsArchetypeSubCommand(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("list archetypes","list archetype","archetype list", "la").isPresent()) {
            if (cmdLine.isExecMode()) {
                NSession session=workspace.currentSession();
                session.out().println(NWorkspace.get().getAvailableArchetypes());
            }
            return true;
        }
        return false;
    }
}
