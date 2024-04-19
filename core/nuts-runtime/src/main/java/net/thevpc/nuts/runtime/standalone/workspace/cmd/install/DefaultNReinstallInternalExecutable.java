/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NInstallCmd;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNReinstallInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNReinstallInternalExecutable(String[] args, NExecCmd execCommand) {
        super("reinstall", args, execCommand);
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
        NInstallCmd.of(getSession())
                .configure(true,"--reinstall")
                .configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
