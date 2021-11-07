/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.uninstall;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsUninstallInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsUninstallInternalExecutable(String[] args, NutsSession session) {
        super("uninstall", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        getSession().uninstall().configure(false, args).run();
    }

}
