/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

/**
 * @author thevpc
 */
public class NCliInfoSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("cli-id").isPresent()) {
            if(cmdLine.isEmpty()){
                doLoadCliId(session);
            }else{
                String value = cmdLine.nextNonOption().get().toString();
                doSaveCliId(session, value);
            }
        }else if (cmdLine.next("get cli-id").isPresent()) {
            doLoadCliId(session);
            return true;
        }else if (cmdLine.next("set cli-id").isPresent()) {
            String value = cmdLine.nextNonOption().get().toString();
            doSaveCliId(session, value);
            return true;
        }
        return false;
    }

    private void doSaveCliId(NSession session, String value) {
        session.out().println(NCliInfo.saveCliId(value, session));
        session.out().println("cli-id updated.");
    }

    private void doLoadCliId(NSession session) {
        session.out().println(NCliInfo.loadCliId(session));
    }
}
