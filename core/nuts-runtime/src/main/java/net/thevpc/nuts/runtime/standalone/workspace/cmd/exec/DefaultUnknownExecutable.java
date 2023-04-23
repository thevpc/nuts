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


    public DefaultUnknownExecutable(String[] cmd, NExecCommand execCommand) {
        super(cmd[0], NCmdLine.of(cmd).toString(), NExecutableType.UNKNOWN,execCommand);
    }

    @Override
    public int execute() {
        if(getSession().isDry()){
            throw new NExecutionException(getSession(), NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }else {
            throw new NExecutionException(getSession(), NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }
    }

    @Override
    public NId getId() {
        return null;
    }
}
