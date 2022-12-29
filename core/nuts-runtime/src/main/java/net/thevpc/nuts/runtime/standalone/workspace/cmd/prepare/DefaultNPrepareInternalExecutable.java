/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.NPrepareCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 * @author thevpc
 */
public class DefaultNPrepareInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNPrepareInternalExecutable(String[] args, NSession session) {
        super("prepare", args, session);
    }

    @Override
    public void execute() {
        if (getSession().isDry()) {
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NPrepareCommand.of(getSession()).configure(false, args).run();
    }

}
