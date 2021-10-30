/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.*;

/**
 * @author bacali95
 * @since 0.8.3
 */
public class DefaultUnknownExecutable extends AbstractNutsExecutableCommand {

    NutsSession execSession;

    public DefaultUnknownExecutable(String[] cmd, NutsSession execSession) {
        super(cmd[0], execSession.commandLine().create(cmd).toString(), NutsExecutableType.UNKNOWN);
        this.execSession = execSession;
    }

    @Override
    public void execute() {
        throw new NutsExecutionException(execSession, NutsMessage.cstyle("cannot execute an unknown command : %s", name), 1);
    }

    @Override
    public void dryExecute() {
        throw new NutsExecutionException(execSession, NutsMessage.cstyle("cannot execute an unknown command : %s", name), 1);
    }

    @Override
    public NutsId getId() {
        return null;
    }
}
