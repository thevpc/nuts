/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NMsg;

/**
 * @author bacali95
 * @since 0.8.3
 */
public class DefaultUnknownExecutable extends AbstractNExecutableInformationExt {


    public DefaultUnknownExecutable(String[] cmd, NExecCmd execCommand) {
        super(cmd[0], NCmdLine.of(cmd).toString(), NExecutableType.UNKNOWN,execCommand);
    }

    @Override
    public int execute() {
        NSession session = NSession.of();
        if(session.isDry()){
            throw new NExecutionException(NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }else {
            throw new NExecutionException(NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }
    }

    @Override
    public NId getId() {
        return null;
    }
}
