package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.NutsIOManager;
import net.vpc.app.nuts.NutsSystemTerminalBase;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.fprint.AnsiPrintStreamSupport;
import net.vpc.common.fprint.FPrint;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNutsSystemTerminalBase implements NutsSystemTerminalBase {

    private static Logger log = Logger.getLogger(DefaultNutsSystemTerminalBase.class.getName());
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;

    @Override
    public void install(NutsWorkspace workspace) {
        NutsTerminalMode terminalMode = workspace.getConfigManager().getOptions().getTerminalMode();
        if (terminalMode == null) {
            terminalMode = NutsTerminalMode.FORMATTED;
        }
        setOutMode(terminalMode);
        setErrorMode(terminalMode);
        NutsIOManager ioManager = workspace.getIOManager();
        this.out = ioManager.createPrintStream(System.out, NutsTerminalMode.FORMATTED);
        this.err = ioManager.createPrintStream(System.err, NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
        this.in = System.in;
        scanner = new Scanner(this.in);

    }

    private AnsiPrintStreamSupport.Type convertMode(NutsTerminalMode outMode) {
        if (outMode == null) {
            outMode = NutsTerminalMode.FORMATTED;
        }
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
    public void setOutMode(NutsTerminalMode mode) {
        log.log(Level.FINEST, "Changing Terminal Out Mode : {0}", mode);
        FPrint.installStdOut(convertMode(this.outMode = mode));
    }

    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        log.log(Level.FINEST, "Changing Terminal Err Mode : {0}", mode);
        FPrint.installStdErr(convertMode(this.errMode = mode));
    }

    @Override
    public NutsTerminalMode getErrorMode() {
        return errMode;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return 1;
    }

    @Override
    public String readLine(PrintStream out,String prompt, Object... params) {
        out.printf(prompt, params);
        return scanner.nextLine();
    }

    @Override
    public String readPassword(PrintStream out,String prompt, Object... params) {
        out.printf(prompt, params);
        return scanner.nextLine();
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
}
