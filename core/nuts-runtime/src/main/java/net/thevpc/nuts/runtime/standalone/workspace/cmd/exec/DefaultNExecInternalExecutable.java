/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNExecInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNExecInternalExecutable(NWorkspace workspace, String[] args, NExecCmd execCommand) {
        super(workspace, "exec", args, execCommand);
    }

    @Override
    public int execute() {
        if (ExtraApiUtils.asBoolean(getExecCommand().getDry())) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        return getExecCommand().copy().clearCommand().configure(false, args)
                .failFast().run()
                .getResultCode();
    }

    @Override
    public void dryExecute() {
        NSession session = workspace.currentSession();
        if (NAppUtils.processHelpOptions(args)) {
            session.out().println("[dry] ==show-help==");
            return;
        }

        getExecCommand()
                .copy()
                .clearCommand().configure(false, args)
                .failFast()
                .setDry(true)
                .run();

    }
}
