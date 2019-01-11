package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.javashell.Env;

import java.io.InputStream;
import java.io.PrintStream;

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
    public boolean configure(CommandLine cmd) {
        Argument a;
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
        } else if ((a = cmd.readOption("--no-colors")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readStringOption("--term")) != null) {
            String s=a.getStringValue().toLowerCase();
            switch (s){
                case "":
                case "system":
                {
                    setTerminalMode(null);
                    break;
                }
                case "filtered":{
                    setTerminalMode(NutsTerminalMode.FILTERED);
                    break;
                }
                case "formatted":{
                    setTerminalMode(NutsTerminalMode.FORMATTED);
                    break;
                }
                case "inherited":{
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
                consoleContext.getTerminal().getFormattedOut().printf("%s\n", consoleContext.getShell().getVersion());
            }
            cmd.skipAll();
            return true;
        }
        return false;
    }

    private void showHelp() {
        out().println(command.getHelp());
    }

    public PrintStream out() {
        return consoleContext.out();
    }

    public PrintStream err() {
        return consoleContext.err();
    }

    public NutsTerminalMode geTerminalMode() {
        return terminalMode;
    }

    public void setTerminalMode(NutsTerminalMode outMode) {
        getWorkspace().getSystemTerminal().setMode(outMode);
    }


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
    public NutsTerminal getTerminal() {
        return consoleContext.getTerminal();
    }


    @Override
    public Env env() {
        return consoleContext.env();
    }

}
