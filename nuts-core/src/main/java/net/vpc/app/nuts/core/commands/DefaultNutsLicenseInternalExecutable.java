/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsLicenseInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsLicenseInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session) {
        super("license", args, ws, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession(true).getTerminal().fout().println(NutsWorkspaceExt.of(ws).getLicenseText());
    }

}
