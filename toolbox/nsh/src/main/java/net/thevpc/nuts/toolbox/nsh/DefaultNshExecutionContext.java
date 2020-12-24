package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.JShellFileContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.jshell.JShellVariables;

import java.io.InputStream;
import java.io.PrintStream;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsCommandLine;

public class DefaultNshExecutionContext implements NshExecutionContext {

    private NutsShellContext shellContext;
    private NshBuiltin builtin;
    private NutsTerminalMode terminalMode = null;
    private JShellFileContext fileContext = null;

    public DefaultNshExecutionContext(NutsShellContext shellContext, NshBuiltin command, JShellFileContext fileContext) {
        this.shellContext = shellContext;
        this.builtin = command;
        this.fileContext = fileContext;
    }

    @Override
    public NutsJavaShell getShell() {
        return (NutsJavaShell) shellContext.getShell();
    }

    @Override
    public JShellFileContext getGlobalContext() {
        return fileContext;
    }

    @Override
    public NutsShellContext getNutsShellContext() {
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
                    out().printf("%s%n", getWorkspace().id().resolveId(getClass(), getSession()).getVersion().toString());
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

    @Override
    public void configureLast(NutsCommandLine cmd) {
        if(!configureFirst(cmd)){
            cmd.unexpectedArgument();
        }
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
