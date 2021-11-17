/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

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
        if (CoreNutsUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        getSession().install()
                .configure(true,"--reinstall")
                .configure(false, args).run();
    }

}
