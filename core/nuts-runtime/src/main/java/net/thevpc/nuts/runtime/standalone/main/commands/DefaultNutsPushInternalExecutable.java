/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.commands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

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
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().push().setSession(getSession().setTrace(true)).configure(false, args).run();
    }

}
