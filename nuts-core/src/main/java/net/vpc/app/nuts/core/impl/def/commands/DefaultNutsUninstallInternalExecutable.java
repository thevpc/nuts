/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUninstallInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsUninstallInternalExecutable(String[] args, NutsSession session) {
        super("uninstall", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().uninstall().session(getSession().trace()).configure(false, args).run();
    }

}
