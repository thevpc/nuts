/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NUninstallCmd;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNUninstallInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNUninstallInternalExecutable(String[] args, NExecCmd execCommand) {
        super("uninstall", args, execCommand);
    }

    @Override
    public int execute() {
        if(getSession().getDry().orDefault()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NUninstallCmd.of(getSession()).configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
