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
    public NCliInfoSubCommand() {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        return cmdLine
                .selector().with("cli-id").nextEntry((v) -> {
                    if (NBlankable.isBlank(v)) {
                        doLoadCliId();
                    } else {
                        doSaveCliId(v.stringValue());
                    }
                })
                .with("get cli-id").next(a -> doLoadCliId())
                .with("set cli-id").next((a) -> doLoadCliId())
                .anyMatch();
    }

    private void doSaveCliId(String value) {
        NOut.println(NCliInfo.saveCliId(value));
        NOut.println("cli-id updated.");
    }

    private void doLoadCliId() {
        NOut.println(NCliInfo.loadCliId());
    }
}
