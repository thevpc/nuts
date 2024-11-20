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
    public NCliInfoSubCommand(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("cli-id").isPresent()) {
            if(cmdLine.isEmpty()){
                doLoadCliId();
            }else{
                String value = cmdLine.nextNonOption().get().toString();
                doSaveCliId(value);
            }
        }else if (cmdLine.next("get cli-id").isPresent()) {
            doLoadCliId();
            return true;
        }else if (cmdLine.next("set cli-id").isPresent()) {
            String value = cmdLine.nextNonOption().get().toString();
            doSaveCliId(value);
            return true;
        }
        return false;
    }

    private void doSaveCliId(String value) {
        NSession session=workspace.currentSession();
        session.out().println(NCliInfo.saveCliId(value));
        session.out().println("cli-id updated.");
    }

    private void doLoadCliId() {
        NSession session=workspace.currentSession();
        session.out().println(NCliInfo.loadCliId());
    }
}
