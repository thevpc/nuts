/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NBlankable;

/**
 * @author thevpc
 */
public class NCliInfoSubCommand extends AbstractNSettingsSubCommand {
    public NCliInfoSubCommand(NWorkspace workspace) {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if(cmdLine.withNextEntry((v,a)->{
            if(NBlankable.isBlank(v)){
                doLoadCliId();
            }else{
                doSaveCliId(v);
            }
        },"cli-id")){
            return true;
        }
        if (cmdLine.next("get cli-id").isPresent()) {
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
        NOut.println(NCliInfo.saveCliId(value));
        NOut.println("cli-id updated.");
    }

    private void doLoadCliId() {
        NOut.println(NCliInfo.loadCliId());
    }
}
