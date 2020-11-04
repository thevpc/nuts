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
public class DefaultNutsSearchInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsSearchInternalExecutable(String[] args, NutsSession session) {
        super("search", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().search().setSession(getSession()).setPrintResult(true)
                .configure(false, args).run();
    }

}
