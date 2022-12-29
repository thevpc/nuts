/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNExecInternalExecutable extends DefaultInternalNExecutableCommand {

    private final NExecCommand execCommand;

    public DefaultNExecInternalExecutable(String[] args, NSession session, NExecCommand execCommand) {
        super("exec", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        execCommand.copy().setSession(getSession()).clearCommand().configure(false, args).setFailFast(true).run();
    }

    @Override
    public void dryExecute() {
        if (NAppUtils.processHelpOptions(args, getSession())) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        execCommand.copy().setSession(getSession().copy().setDry(true)).clearCommand().configure(false, args).setFailFast(true).run();
    }
}
