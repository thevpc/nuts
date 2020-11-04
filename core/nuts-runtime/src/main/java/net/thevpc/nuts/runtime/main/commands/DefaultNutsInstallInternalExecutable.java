/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.commands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsInstallInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsInstallInternalExecutable(String[] args, NutsSession session) {
        super("install", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().install().setSession(getSession().setTrace(true)).configure(false, args).run();
    }

}
