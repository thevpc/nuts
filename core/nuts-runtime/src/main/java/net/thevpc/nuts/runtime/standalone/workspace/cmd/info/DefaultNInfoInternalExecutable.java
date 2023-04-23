/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NInfoCommand;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

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
        if(getSession().isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NPrintStream out = getSession().out();
        NInfoCommand.of(getSession()).configure(false, args).println(out);
        return NExecutionException.SUCCESS;
    }

}
