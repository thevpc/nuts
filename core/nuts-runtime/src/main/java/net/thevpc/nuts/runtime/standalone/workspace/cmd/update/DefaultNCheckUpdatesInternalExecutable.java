/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNCheckUpdatesInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNCheckUpdatesInternalExecutable(String[] args, NExecCmd execCommand) {
        super("check-updates", args, execCommand);
    }

    @Override
    public int execute() {
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NUpdateCmd.of().configure(false, args).checkUpdates();
        return NExecutionException.SUCCESS;
    }

}
