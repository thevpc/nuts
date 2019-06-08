/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsExecCommand;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsVersionInternalExecutable extends DefaultInternalNutsExecutableCommand {
    
    private final DefaultNutsExecCommand outer;

    public DefaultNutsVersionInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session, final DefaultNutsExecCommand outer) {
        super("version", args, ws, session);
        this.outer = outer;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        PrintStream out = getSession(true).getTerminal().fout();
        ws.format().version().configure(false, args).println(out);
    }
    
}
