/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNInfoInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNInfoInternalExecutable(String[] args, NExecCommand execCommand) {
        super("info", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = NSessionUtils.configureCopyOfSession(getSession(), getExecCommand().getIn(), getExecCommand().getOut(),getExecCommand().getErr());
        if(session.isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;

        }
        NPrintStream out = session.out();
        NInfoCommand.of(session).configure(false, args).println(out);
        return NExecutionException.SUCCESS;
    }

}
