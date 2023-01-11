/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.license;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 */
public class DefaultNLicenseInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNLicenseInternalExecutable(String[] args, NSession session) {
        super("license", args, session);
    }

    @Override
    public void execute() {
        if (getSession().isDry()) {
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NSession session = getSession();
        NCommandLine commandLine = NCommandLine.of(args);
        while (commandLine.hasNext()) {
            NArg a = commandLine.peek().get(session);
            session.configureLast(commandLine);
        }

        NText licenseString = NWorkspaceExt.of(session.getWorkspace()).getLicenseText(session);
        session.out().println(licenseString);
    }

}
