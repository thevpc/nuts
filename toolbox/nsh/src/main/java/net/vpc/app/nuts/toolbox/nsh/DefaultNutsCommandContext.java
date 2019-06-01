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
import net.vpc.app.nuts.NutsObjectFormat;

public class DefaultNutsCommandContext implements NutsCommandContext {

    private NutsShellContext consoleContext;
    private NshCommand command;
    private NutsTerminalMode terminalMode = null;
    private boolean verbose = false;

    public DefaultNutsCommandContext(NutsShellContext consoleContext, NshCommand command) {
        this.consoleContext = consoleContext;
        this.command = command;
    }

    @Override
    public void printObject(Object any, String[] options, boolean err) {
        final NutsObjectFormat o = this.getWorkspace().formatter().createObjectFormat(this.getSession(), any);
        o.configure(this.getWorkspace().parser().parseCommand(options), true);
        final NutsSessionTerminal t = this.getSession().getTerminal();
        if (err) {
            o.print(t.ferr());
        } else {
            o.print(t.fout());
        }
    }

    @Override
    public NutsJavaShell getShell() {
        return (NutsJavaShell) consoleContext.getShell();
    }

    @Override
    public NutsShellContext getGlobalContext() {
        return consoleContext;
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
                throw new NutsExecutionException(consoleContext.getWorkspace(), "Help", 0);
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    out().printf("%s%n", getWorkspace().resolveIdForClass(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(consoleContext.getWorkspace(), "Help", 0);
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
        return consoleContext.out();
    }

    @Override
    public PrintStream err() {
        return consoleContext.err();
    }

    public NutsTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public void setTerminalMode(NutsTerminalMode outMode) {
        getWorkspace().io().getSystemTerminal().setMode(outMode);
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
        return consoleContext.getWorkspace();
    }

    @Override
    public InputStream in() {
        return consoleContext.getTerminal().getIn();
    }

    @Override
    public NutsSession getSession() {
        return consoleContext.getSession();
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return consoleContext.getTerminal();
    }

    @Override
    public JShellVariables vars() {
        return consoleContext.vars();
    }

}
