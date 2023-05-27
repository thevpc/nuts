/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNExecInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNExecInternalExecutable(String[] args, NExecCommand execCommand) {
        super("exec", args, execCommand);
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
        return getExecCommand().copy().setSession(getSession()).clearCommand().configure(false, args).setFailFast(true).run()
                .getResult();
    }

    @Override
    public void dryExecute() {
        if (NAppUtils.processHelpOptions(args, getSession())) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        getExecCommand()
                .copy()
                .setSession(getSession().copy().setDry(true)).clearCommand().configure(false, args).setFailFast(true)
                .run()
        ;
    }
}
