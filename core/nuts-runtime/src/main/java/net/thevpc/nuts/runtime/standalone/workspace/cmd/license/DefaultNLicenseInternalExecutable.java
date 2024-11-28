/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.license;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 */
public class DefaultNLicenseInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNLicenseInternalExecutable(NWorkspace workspace,String[] args, NExecCmd execCommand) {
        super(workspace,"license", args, execCommand);
    }

    @Override
    public int execute() {
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        NSession session = workspace.currentSession();
        if (dry) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NCmdLine cmdLine = NCmdLine.of(args);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            session.configureLast(cmdLine);
        }

        NText licenseString = NWorkspaceExt.of().getLicenseText();
        session.out().println(licenseString);
        return NExecutionException.SUCCESS;
    }

}
