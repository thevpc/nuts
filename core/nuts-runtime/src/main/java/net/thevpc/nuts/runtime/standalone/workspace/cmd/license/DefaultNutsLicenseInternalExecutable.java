/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.license;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;
import net.thevpc.nuts.text.NutsText;

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
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NutsSession session = getSession();
        NutsCommandLine commandLine = NutsCommandLine.of(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek().get(session);
            session.configureLast(commandLine);
        }

        NutsText licenseString = NutsWorkspaceExt.of(session.getWorkspace()).getLicenseText(session);
        if (session.isPlainOut()) {
            session.out().println(licenseString);
        } else {
            session.out().printlnf(licenseString);
        }
    }

}
