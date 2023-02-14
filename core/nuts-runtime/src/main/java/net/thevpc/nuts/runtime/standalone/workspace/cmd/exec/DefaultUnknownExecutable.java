/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;

/**
 * @author bacali95
 * @since 0.8.3
 */
public class DefaultUnknownExecutable extends AbstractNExecutableCommand {

    NSession execSession;

    public DefaultUnknownExecutable(String[] cmd, NSession execSession) {
        super(cmd[0], NCmdLine.of(cmd).toString(), NExecutableType.UNKNOWN);
        this.execSession = execSession;
    }

    @Override
    protected NSession getSession() {
        return execSession;
    }

    @Override
    public void execute() {
        if(execSession.isDry()){
            throw new NExecutionException(execSession, NMsg.ofC("cannot execute an unknown command : %s", name), 1);
        }else {
            throw new NExecutionException(execSession, NMsg.ofC("cannot execute an unknown command : %s", name), 1);
        }
    }

    @Override
    public NId getId() {
        return null;
    }
}
