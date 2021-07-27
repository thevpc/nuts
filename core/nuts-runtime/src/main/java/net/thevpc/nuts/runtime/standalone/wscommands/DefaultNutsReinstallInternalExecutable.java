/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsReinstallInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsReinstallInternalExecutable(String[] args, NutsSession session) {
        super("reinstall", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().install()
                .configure(true,"--reinstall")
                .setSession(getSession().setTrace(true)).configure(false, args).run();
    }

}
