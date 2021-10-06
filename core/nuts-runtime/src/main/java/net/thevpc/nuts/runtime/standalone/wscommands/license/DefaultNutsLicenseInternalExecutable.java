/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.license;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
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
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        NutsCommandLine commandLine = getSession().commandLine().create(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                switch (a.getKey().getString()) {
                    case "--help": {
                        commandLine.skipAll();
                        showDefaultHelp();
                        return;
                    }
                    default: {
                        getSession().configureLast(commandLine);
                    }
                }
            } else {
                getSession().configureLast(commandLine);
            }
        }
        getSession().out().println(NutsWorkspaceExt.of(getSession().getWorkspace()).getLicenseText(getSession()));
    }

}
