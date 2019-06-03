package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.JShellVariables;

import java.io.InputStream;
import java.io.PrintStream;
import net.vpc.app.nuts.NutsCommand;

import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;

public class DefaultNutsCommandContext implements NutsCommandContext {

    private NutsShellContext shellContext;
    private NshCommand command;
    private NutsTerminalMode terminalMode = null;
    private boolean verbose = false;

    public DefaultNutsCommandContext(NutsShellContext consoleContext, NshCommand command) {
        this.shellContext = consoleContext;
        this.command = command;
    }

    @Override
    public NutsCommandContext printOutObject(Object anyObject) {
        shellContext.printOutObject(anyObject);
        return this;
    }

    @Override
    public NutsCommandContext printErrObject(Object anyObject) {
        shellContext.printErrObject(anyObject);
        return this;
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
    public boolean configureFirst(NutsCommand cmd) {
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
                throw new NutsExecutionException(shellContext.getWorkspace(), "Help", 0);
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    out().printf("%s%n", getWorkspace().resolveIdForClass(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(shellContext.getWorkspace(), "Help", 0);
            }
            case "--term-system": {
                cmd.skip();
                setTerminalMode(null);
                return true;
            }
            case "--term-filtered": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.FILTERED);
                return true;
            }
            case "--term-formatted": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.FORMATTED);
                return true;
            }
            case "--term-inherited": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.INHERITED);
                return true;
            }
            case "--term": {
                String s = cmd.nextString().getValue().getString("").toLowerCase();
                switch (s) {
                    case "":
                    case "system":
                    case "auto": {
                        setTerminalMode(null);
                        break;
                    }
                    case "filtered": {
                        setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "formatted": {
                        setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "inherited": {
                        setTerminalMode(NutsTerminalMode.INHERITED);
                        break;
                    }
                }
                return true;
            }
            case "--color": {
                NutsArgument val = cmd.nextString().getValue();
                String s = val.getString("").toLowerCase();
                switch (s) {
                    case "":
                    case "system":
                    case "auto": {
                        setTerminalMode(null);
                        break;
                    }
                    case "filtered": {
                        setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "formatted": {
                        setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "inherited": {
                        setTerminalMode(NutsTerminalMode.INHERITED);
                        break;
                    }
                    default: {
                        Boolean bval = cmd.newArgument(s).getBoolean(false);
                        setTerminalMode(bval ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                    }
                }
                return true;
            }
            case "--verbose": {
                setVerbose(cmd.nextBoolean().getValue().getBoolean());
                return true;
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
        out().println(command.getHelp());
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
    public NutsCommandContext setTerminalMode(NutsTerminalMode outMode) {
        getWorkspace().io().getSystemTerminal().setMode(outMode);
        return this;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public InputStream in() {
        return shellContext.getTerminal().getIn();
    }

    @Override
    public NutsSession getSession() {
        return shellContext.getSession();
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return shellContext.getTerminal();
    }

    @Override
    public JShellVariables vars() {
        return shellContext.vars();
    }

}
