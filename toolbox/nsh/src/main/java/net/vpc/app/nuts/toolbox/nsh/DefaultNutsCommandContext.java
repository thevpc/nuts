package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.Env;

import java.io.InputStream;
import java.io.PrintStream;
import net.vpc.app.nuts.NutsCommandLine;

import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.common.strings.StringUtils;
import net.vpc.app.nuts.NutsArgument;

public class DefaultNutsCommandContext implements NutsCommandContext {

    private NutsConsoleContext consoleContext;
    private NutsCommand command;
    private NutsTerminalMode terminalMode = null;
    private boolean verbose = false;

    public DefaultNutsCommandContext(NutsConsoleContext consoleContext, NutsCommand command) {
        this.consoleContext = consoleContext;
        this.command = command;
    }

    @Override
    public NutsJavaShell getShell() {
        return (NutsJavaShell) consoleContext.getShell();
    }

    @Override
    public NutsConsoleContext consoleContext() {
        return consoleContext;
    }

    @Override
    public boolean configure(NutsCommandLine cmd) {
        NutsArgument a;
        if ((a = cmd.readOption("--help")) != null) {
            if (cmd.isExecMode()) {
                showHelp();
            }
            cmd.skipAll();
            return true;
        } else if ((a = cmd.readOption("--term-system")) != null) {
            setTerminalMode(null);
        } else if ((a = cmd.readOption("--term-filtered")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readOption("--term-formatted")) != null) {
            setTerminalMode(NutsTerminalMode.FORMATTED);
        } else if ((a = cmd.readOption("--term-inherited")) != null) {
            setTerminalMode(NutsTerminalMode.INHERITED);
        } else if ((a = cmd.readOption("--no-color")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readImmediateStringOption("--color")) != null) {
            switch (StringUtils.trim(a.getValue().getString()).toLowerCase()) {
                case "formatted": {
                    setTerminalMode(NutsTerminalMode.FORMATTED);
                    break;
                }
                case "filtered": {
                    setTerminalMode(NutsTerminalMode.FILTERED);
                    break;
                }
                case "inherited": {
                    setTerminalMode(NutsTerminalMode.INHERITED);
                    break;
                }
                case "auto":
                case "default":
                case "": {
                    setTerminalMode(NutsTerminalMode.FORMATTED);
                    break;
                }
                default:{
                    setTerminalMode(a.getValue().getBoolean(false)?NutsTerminalMode.FORMATTED:NutsTerminalMode.FILTERED);
                }
            }
        } else if ((a = cmd.readStringOption("--term")) != null) {
            String s = a.getValue().getString().toLowerCase();
            switch (s) {
                case "":
                case "system": {
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
        } else if ((a = cmd.readBooleanOption("--verbose")) != null) {
            this.setVerbose((a.getBooleanValue()));
            return true;
        } else if ((a = cmd.readBooleanOption("--version")) != null) {
            if (cmd.isExecMode()) {
                consoleContext.getTerminal().fout().printf("%s%n", consoleContext.getShell().getVersion());
            }
            cmd.skipAll();
            return true;
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
        getWorkspace().getSystemTerminal().setMode(outMode);
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
    public Env env() {
        return consoleContext.env();
    }

}
