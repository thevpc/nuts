/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NInstallCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNInstallInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNInstallInternalExecutable(String[] args, NExecCommand execCommand) {
        super("install", args, execCommand);
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NInstallCommand.of(getSession()).configure(false, args).run();
    }
}
