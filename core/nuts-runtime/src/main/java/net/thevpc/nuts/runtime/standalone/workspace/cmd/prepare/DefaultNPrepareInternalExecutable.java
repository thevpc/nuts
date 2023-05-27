/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NPrepareCommand;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 * @author thevpc
 */
public class DefaultNPrepareInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNPrepareInternalExecutable(String[] args, NExecCommand execCommand) {
        super("prepare", args, execCommand);
    }

    @Override
    public int execute() {
        if (getSession().isDry()) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NPrepareCommand.of(getSession()).configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
