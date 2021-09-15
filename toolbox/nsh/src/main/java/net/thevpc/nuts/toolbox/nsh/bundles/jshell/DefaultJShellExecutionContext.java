package net.thevpc.nuts.toolbox.nsh.bundles.jshell;


import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.NshBuiltin;

import java.io.InputStream;

public class DefaultJShellExecutionContext implements JShellExecutionContext {
    private JShellContext shellContext;
    private NshBuiltin builtin;
    private NutsTerminalMode terminalMode = null;
    private final JShellFileContext fileContext;

    public DefaultJShellExecutionContext(JShellContext shellContext, NshBuiltin command,JShellFileContext fileContext) {
        this.shellContext = shellContext;
        this.builtin = command;
        this.fileContext = fileContext;
    }
    @Override
    public JShellContext getNutsShellContext() {
        return shellContext;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return shellContext.getSession();
    }


    @Override
    public JShell getShell() {
        return fileContext.getShell();
    }

    @Override
    public NutsPrintStream out() {
        return shellContext.out();
    }

    @Override
    public NutsPrintStream err() {
        return shellContext.err();
    }

    @Override
    public JShellVariables vars() {
        return shellContext.vars();
    }

    @Override
    public InputStream in() {
        return shellContext.in();
    }


    @Override
    public JShellFileContext getGlobalContext() {
        return fileContext;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "--help": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    showHelp();
                    cmd.skipAll();
                }
                throw new NutsExecutionException(shellContext.getSession(), NutsMessage.cstyle("Help"), 0);
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    out().printf("%s%n", getWorkspace().id().setSession(getSession()).resolveId(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(shellContext.getSession(), NutsMessage.cstyle("Help"), 0);
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
        if (!configureFirst(cmd)) {
            cmd.unexpectedArgument();
        }
    }

    @Override
    public NutsApplicationContext getAppContext() {
        return getShell().getAppContext();
    }

    private void showHelp() {
        out().println(builtin.getHelp());
    }

    public NutsTerminalMode geTerminalMode() {
        return terminalMode;
    }

}
