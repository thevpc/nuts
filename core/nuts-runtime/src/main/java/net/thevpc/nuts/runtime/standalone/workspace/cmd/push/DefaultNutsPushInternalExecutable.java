/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.push;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsPushInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsPushInternalExecutable(String[] args, NutsSession session) {
        super("push", args, session);
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        getSession().push().configure(false, args).run();
    }

}
