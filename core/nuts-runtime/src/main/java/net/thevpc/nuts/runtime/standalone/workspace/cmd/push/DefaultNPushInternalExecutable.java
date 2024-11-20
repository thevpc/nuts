/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.push;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.util.NUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNPushInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNPushInternalExecutable(NWorkspace workspace, String[] args, NExecCmd execCommand) {
        super(workspace,"push", args, execCommand);
    }

    @Override
    public int execute() {
        boolean dry = NUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NPushCmd.of().configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
