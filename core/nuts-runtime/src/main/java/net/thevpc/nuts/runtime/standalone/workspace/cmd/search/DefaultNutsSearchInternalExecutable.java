/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsSearchInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsSearchInternalExecutable(String[] args, NutsSession session) {
        super("search", args, session);
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
        getSession().search().setSession(getSession())
                .configure(false, args).run();
    }

}
