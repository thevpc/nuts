/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NUndeployCommand;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNUndeployInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNUndeployInternalExecutable(String[] args, NExecCommand execCommand) {
        super("undeploy", args, execCommand);
    }

    @Override
    public int execute() {
        if(getSession().isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NUndeployCommand.of(getSession()).configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
