package net.thevpc.nuts.toolbox.nsh.jshell;


import net.thevpc.nuts.*;

import java.io.InputStream;

public class DefaultJShellExecutionContext implements JShellExecutionContext {
    private JShellContext shellContext;
    private NutsSession session;
    private JShellBuiltin builtin;
    private NutsTerminalMode terminalMode = null;
    private boolean askHelp;
    private boolean askVersion;
    private Object options;

    public DefaultJShellExecutionContext(JShellContext shellContext, JShellBuiltin command) {
        this.shellContext = shellContext;
        //each execution has its very own session!
        this.session = shellContext.getSession().copy();
        this.builtin = command;
    }


    @Override
    public NutsWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }


    @Override
    public JShell getShell() {
        return shellContext.getShell();
    }

    @Override
    public NutsPrintStream out() {
        return getSession().out();
    }

    @Override
    public NutsPrintStream err() {
        return getSession().err();
    }

    @Override
    public InputStream in() {
        return getSession().in();
    }


    @Override
    public JShellContext getShellContext() {
        return shellContext;
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
                setAskHelp(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
            }
            case "--version": {
                cmd.skip();
                setAskVersion(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
//                cmd.skip();
//                if (cmd.isExecMode()) {
//                    out().printf("%s%n", NutsIdResolver.of(getSession()).resolveId(getClass()).getVersion().toString());
//                    cmd.skipAll();
//                }
//                throw new NutsExecutionException(shellContext.getSession(), NutsMessage.cstyle("Help"), 0);
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
    public boolean isAskVersion() {
        return askVersion;
    }

    public DefaultJShellExecutionContext setAskVersion(boolean askVersion) {
        this.askVersion = askVersion;
        return this;
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

    public NutsTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public boolean isAskHelp() {
        return askHelp;
    }

    public DefaultJShellExecutionContext setAskHelp(boolean askHelp) {
        this.askHelp = askHelp;
        return this;
    }

    public <T> T getOptions() {
        return (T) options;
    }

    public DefaultJShellExecutionContext setOptions(Object options) {
        this.options = options;
        return this;
    }
}
