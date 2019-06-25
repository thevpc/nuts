package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.JShellVariables;

import java.io.InputStream;
import java.io.PrintStream;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsCommandLine;

public class DefaultNshExecutionContext implements NshExecutionContext {

    private NutsShellContext shellContext;
    private NshBuiltin builtin;
    private NutsTerminalMode terminalMode = null;

    public DefaultNshExecutionContext(NutsShellContext shellContext, NshBuiltin command) {
        this.shellContext = shellContext;
        this.builtin = command;
    }

    @Override
    public NutsJavaShell getShell() {
        return (NutsJavaShell) shellContext.getShell();
    }

    @Override
    public NutsShellContext getGlobalContext() {
        return shellContext;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--help": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    showHelp();
                    cmd.skipAll();
                }
                throw new NutsExecutionException(shellContext.getWorkspace(), "Help", 0);
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    out().printf("%s%n", getWorkspace().id().resolveId(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(shellContext.getWorkspace(), "Help", 0);
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showHelp() {
        out().println(builtin.getHelp());
    }

    @Override
    public PrintStream out() {
        return shellContext.out();
    }

    @Override
    public PrintStream err() {
        return shellContext.err();
    }

    public NutsTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NutsWorkspace workspace() {
        return getWorkspace();
    }

    @Override
    public InputStream in() {
        return shellContext.in();
    }

    @Override
    public NutsSession session() {
        return shellContext.getSession();
    }

    @Override
    public NutsSession getSession() {
        return shellContext.getSession();
    }

    @Override
    public JShellVariables vars() {
        return shellContext.vars();
    }

}
