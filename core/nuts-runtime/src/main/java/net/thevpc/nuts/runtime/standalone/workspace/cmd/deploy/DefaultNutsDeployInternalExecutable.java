/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

/**
 * @author thevpc
 */
public class DefaultNutsDeployInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsDeployInternalExecutable(String[] args, NutsSession session) {
        super("deploy", args, session);
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
        getSession().deploy().configure(false, args).run();
    }

}
