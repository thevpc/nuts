/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NInfoCmd;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNInfoInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNInfoInternalExecutable(String[] args, NExecCmd execCommand) {
        super("info", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = NSession.of();
        session = NSessionUtils.configureCopyOfSession(session, getExecCommand().getIn(), getExecCommand().getOut(),getExecCommand().getErr());
        NSession finalSession = session;
        return session.callWith(()->{
            boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
            if(dry){
                dryExecute();
                return NExecutionException.SUCCESS;
            }
            if (NAppUtils.processHelpOptions(args)) {
                showDefaultHelp();
                return NExecutionException.SUCCESS;

            }
            NPrintStream out = finalSession.out();
            NInfoCmd.of().configure(false, args).println(out);
            return NExecutionException.SUCCESS;
        });
    }

}
