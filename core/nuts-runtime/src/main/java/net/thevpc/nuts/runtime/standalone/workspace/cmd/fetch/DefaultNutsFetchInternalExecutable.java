/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsFetchInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsFetchInternalExecutable(String[] args, NutsSession session) {
        super("fetch", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        getSession().fetch().configure(false, args).run();
    }

}
