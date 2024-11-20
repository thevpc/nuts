/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNUninstallInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNUninstallInternalExecutable(NWorkspace workspace,String[] args, NExecCmd execCommand) {
        super(workspace,"uninstall", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = workspace.currentSession();
        if(session.isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NUninstallCmd.of().configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
