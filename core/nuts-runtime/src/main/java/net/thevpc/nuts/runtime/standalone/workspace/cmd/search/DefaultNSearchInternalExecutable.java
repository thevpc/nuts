/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NSearchCommand;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNSearchInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNSearchInternalExecutable(String[] args, NExecCommand execCommand) {
        super("search", args, execCommand);
    }

    @Override
    public int execute() {
        if(getSession().isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NSearchCommand.of(getSession())
                .configure(false, args).run();
        return NExecutionException.SUCCESS;
    }

}
