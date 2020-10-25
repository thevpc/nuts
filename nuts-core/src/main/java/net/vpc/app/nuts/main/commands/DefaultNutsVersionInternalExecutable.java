/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.main.wscommands.DefaultNutsExecCommand;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsVersionInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final DefaultNutsExecCommand execCommand;

    public DefaultNutsVersionInternalExecutable(String[] args, NutsSession session, final DefaultNutsExecCommand execCommand) {
        super("version", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        PrintStream out = getSession().out();
        getSession().getWorkspace().version().formatter().configure(false, args).println(out);
    }

}
