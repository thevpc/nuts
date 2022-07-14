/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;

/**
 * @author thevpc
 */
public class NutsCliInfoSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine commandLine, Boolean autoSave, NutsSession session) {
        if (commandLine.next("cli-id").isPresent()) {
            if(commandLine.isEmpty()){
                doLoadCliId(session);
            }else{
                String value = commandLine.nextNonOption().get().toString();
                doSaveCliId(session, value);
            }
        }else if (commandLine.next("get cli-id").isPresent()) {
            doLoadCliId(session);
            return true;
        }else if (commandLine.next("set cli-id").isPresent()) {
            String value = commandLine.nextNonOption().get().toString();
            doSaveCliId(session, value);
            return true;
        }
        return false;
    }

    private void doSaveCliId(NutsSession session, String value) {
        session.out().println(NutsCliInfo.saveCliId(value, session));
        session.out().println("cli-id updated.");
    }

    private void doLoadCliId(NutsSession session) {
        session.out().println(NutsCliInfo.loadCliId(session));
    }
}
