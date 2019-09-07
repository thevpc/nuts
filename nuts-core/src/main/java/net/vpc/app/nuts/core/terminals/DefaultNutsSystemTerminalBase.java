package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.core.log.NutsLogVerb;
import net.vpc.app.nuts.core.util.fprint.AnsiPrintStreamSupport;
import net.vpc.app.nuts.core.util.fprint.FPrint;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;

@NutsPrototype
public class DefaultNutsSystemTerminalBase implements NutsSystemTerminalBase {

    private NutsLogger LOG;
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;

    @Override
    public void install(NutsWorkspace workspace) {
        LOG=workspace.log().of(DefaultNutsSystemTerminalBase.class);
        NutsTerminalMode terminalMode = workspace.config().options().getTerminalMode();
        if (terminalMode == null) {
            terminalMode = NutsTerminalMode.FORMATTED;
        }
        setOutMode(terminalMode);
        setErrMode(terminalMode);
        NutsIOManager ioManager = workspace.io();
        this.out = ioManager.createPrintStream(FPrint.out(), NutsTerminalMode.FORMATTED);
        this.err = ioManager.createPrintStream(FPrint.err(), NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
        this.in = System.in;
        this.scanner = new Scanner(this.in);
    }

    private AnsiPrintStreamSupport.Type convertMode(NutsTerminalMode outMode) {
        switch (outMode) {
            case INHERITED: {
                return (AnsiPrintStreamSupport.Type.INHERIT);
            }
            case FILTERED: {
                return (AnsiPrintStreamSupport.Type.STRIP);
            }
            case FORMATTED: {
                return (AnsiPrintStreamSupport.Type.ANSI);
            }
        }
        return AnsiPrintStreamSupport.Type.ANSI;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public NutsSystemTerminalBase setOutMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if(LOG!=null) {
            LOG.log(Level.CONFIG, NutsLogVerb.CONFIG, "Changing terminal Out mode : {0}", mode.id());
        }
        FPrint.installStdOut(convertMode(this.outMode = mode));
        return this;
    }

    @Override
    public NutsSystemTerminalBase setErrMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if(LOG!=null) {
            LOG.log(Level.CONFIG, NutsLogVerb.CONFIG, "Changing terminal Err mode : {0}", mode.id());
        }
        FPrint.installStdErr(convertMode(this.errMode = mode));
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return errMode;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

//    @Override
    public String readLine(String prompt, Object... params) {
        return readLine(getOut(), prompt, params);
    }

//    @Override
    public char[] readPassword(String prompt, Object... params) {
        return readPassword(getOut(), prompt, params);
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = FPrint.out();
        }
        out.printf(prompt, params);
        out.flush();
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = FPrint.out();
        }
        out.printf(prompt, params);
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public PrintStream getOut() {
        return this.out;
    }

    @Override
    public PrintStream getErr() {
        return this.err;
    }

    @Override
    public void uninstall() {

    }

    @Override
    public NutsTerminalBase getParent() {
        return null;
    }

}
