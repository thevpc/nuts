package net.vpc.app.nuts.extensions.core;

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
    private static Logger log=Logger.getLogger(DefaultNutsSystemTerminalBase.class.getName());
    private Scanner scanner;
    private NutsTerminalMode outMode=NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode=NutsTerminalMode.FORMATTED;
    @Override
    public void install(NutsWorkspace workspace) {
        scanner = new Scanner(System.in);
        if(workspace.getConfigManager().getOptions().isNoColors()) {
            setOutMode(NutsTerminalMode.FILTERED);
            setErrorMode(NutsTerminalMode.FILTERED);
        }else{
            setOutMode(NutsTerminalMode.FORMATTED);
            setErrorMode(NutsTerminalMode.FORMATTED);
        }
    }


    private AnsiPrintStreamSupport.Type convertMode(NutsTerminalMode outMode) {
        if(outMode==null){
            outMode=NutsTerminalMode.FORMATTED;
        }
        switch (outMode){
            case INHERITED:{
                return (AnsiPrintStreamSupport.Type.INHERIT);
            }
            case FILTERED:{
                return (AnsiPrintStreamSupport.Type.STRIP);
            }
            case FORMATTED:{
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
        log.log(Level.FINEST, "Changing Terminal Out Mode : {0}",mode);
        FPrint.installStdOut(convertMode(this.outMode=mode));
    }
    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        log.log(Level.FINEST, "Changing Terminal Err Mode : {0}",mode);
        FPrint.installStdErr(convertMode(this.errMode=mode));
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
    public String readLine(String promptFormat, Object... params) {
        getOut().printf(promptFormat,params);
        getOut().print(" : ");
        return scanner.nextLine();
    }

    @Override
    public String readPassword(String prompt) {
        getOut().printf(prompt+" : ");
        return scanner.nextLine();
    }

    @Override
    public InputStream getIn() {
        return System.in;
    }

    @Override
    public PrintStream getOut() {
        return System.out;
    }

    @Override
    public PrintStream getErr() {
        return System.err;
    }
}
