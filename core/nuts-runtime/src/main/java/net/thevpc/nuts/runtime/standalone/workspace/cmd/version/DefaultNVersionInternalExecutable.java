/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNVersionInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNVersionInternalExecutable(String[] args, final DefaultNExecCommand execCommand) {
        super("version", args, execCommand);
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
        NWorkspace ws = getSession().getWorkspace();
        NPrintStream out = getSession().out();
        NVersionFormat.of(getSession()).configure(false, args).println(out);
        return NExecutionException.SUCCESS;
    }

}
