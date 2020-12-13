/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.commands;

import java.io.PrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.main.wscommands.DefaultNutsExecCommand;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author thevpc
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
