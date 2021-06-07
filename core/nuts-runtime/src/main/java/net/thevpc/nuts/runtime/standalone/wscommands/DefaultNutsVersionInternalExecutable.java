/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import java.io.PrintStream;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

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
        NutsPrintStream out = getSession().out();
        getSession().getWorkspace().version().formatter().configure(false, args).println(out);
    }

}
