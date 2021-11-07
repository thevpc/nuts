/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.license;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsLicenseInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsLicenseInternalExecutable(String[] args, NutsSession session) {
        super("license", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NutsSession session = getSession();
        NutsCommandLine commandLine = NutsCommandLine.of(args,session);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            session.configureLast(commandLine);
        }

        String licenseString = NutsWorkspaceExt.of(session.getWorkspace()).getLicenseText(session);
        if (session.isPlainOut()) {
            session.out().println(licenseString);
        } else {
            session.out().printlnf(licenseString);
        }
    }

}
