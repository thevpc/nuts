/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;

/**
 * @author bacali95
 * @since 0.8.3
 */
public class DefaultUnknownExecutable extends AbstractNutsExecutableCommand {

    NutsSession execSession;

    public DefaultUnknownExecutable(String[] cmd, NutsSession execSession) {
        super(cmd[0], NutsCommandLine.of(cmd).toString(), NutsExecutableType.UNKNOWN);
        this.execSession = execSession;
    }

    @Override
    protected NutsSession getSession() {
        return execSession;
    }

    @Override
    public void execute() {
        if(execSession.isDry()){
            throw new NutsExecutionException(execSession, NutsMessage.ofCstyle("cannot execute an unknown command : %s", name), 1);
        }else {
            throw new NutsExecutionException(execSession, NutsMessage.ofCstyle("cannot execute an unknown command : %s", name), 1);
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }
}
