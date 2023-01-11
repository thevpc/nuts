/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import net.thevpc.nuts.NInfoCommand;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNInfoInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNInfoInternalExecutable(String[] args, NSession session) {
        super("info", args, session);
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
        NOutputStream out = getSession().out();
        NInfoCommand.of(getSession()).configure(false, args).println(out);
    }

}
