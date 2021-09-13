/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.NutsExecCommand;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public class DefaultNutsExecInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final NutsExecCommand execCommand;

    public DefaultNutsExecInternalExecutable(String[] args, NutsSession session, NutsExecCommand execCommand) {
        super("exec", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
//        if (CoreNutsUtils.isIncludesHelpOption(args)) {
//            showDefaultHelp();
//            return;
//        }
        execCommand.copy().setSession(getSession()).clearCommand().configure(false, args).setFailFast(true).run();
    }

    @Override
    public void dryExecute() {
//        if (CoreNutsUtils.isIncludesHelpOption(args)) {
//            getSession().out().println("[dry] ==show-help==");
//            return;
//        }
        execCommand.copy().setSession(getSession()).clearCommand().configure(false, args).setFailFast(true).setDry(true).run();
    }
}
