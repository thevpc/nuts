/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNVersionInternalExecutable extends DefaultInternalNExecutableCommand {

    private final DefaultNExecCommand execCommand;

    public DefaultNVersionInternalExecutable(String[] args, NSession session, final DefaultNExecCommand execCommand) {
        super("version", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NWorkspace ws = getSession().getWorkspace();
        NStream out = getSession().out();
        NVersionFormat.of(getSession()).configure(false, args).println(out);
    }

}
