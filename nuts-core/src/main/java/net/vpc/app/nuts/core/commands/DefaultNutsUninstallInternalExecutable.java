/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUninstallInternalExecutable extends DefaultInternalNutsExecutableCommand {
    
    public DefaultNutsUninstallInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session) {
        super("uninstall", args, ws, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        ws.uninstall().session(getSession(true).trace()).configure(args).run();
    }
    
}
