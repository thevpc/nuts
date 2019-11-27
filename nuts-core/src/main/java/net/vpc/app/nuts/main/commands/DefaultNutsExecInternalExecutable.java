/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsExecInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final NutsExecCommand execCommand;

    public DefaultNutsExecInternalExecutable(String[] args, NutsSession session, NutsExecCommand execCommand) {
        super("exec", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        execCommand.copy().session(getSession()).clearCommand().configure(false, args).failFast().run();
    }

    @Override
    public void dryExecute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        execCommand.copy().session(getSession()).clearCommand().configure(false, args).failFast().dry().run();
    }
}
