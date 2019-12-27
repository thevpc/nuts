package net.vpc.app.nuts.runtime.util.fprint;

import java.io.PrintStream;

import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.vpc.app.nuts.runtime.util.fprint.renderer.StripperFormattedPrintStreamRenderer;

public class FPrint {

    public static final FormattedPrintStreamRenderer RENDERER_ANSI = AnsiUnixTermPrintRenderer.ANSI_RENDERER;
    public static final FormattedPrintStreamRenderer RENDERER_ANSI_STRIPPER = StripperFormattedPrintStreamRenderer.STRIPPER;
//    static PrintStream out = null;
//    static PrintStream err = null;

//    public static PrintStream out() {
//        return out == null ? System.out : out;
//    }
//
//    public static PrintStream err() {
//        return err == null ? System.err : err;
//    }

    public static void uninstall(NutsWorkspace ws) {
        AnsiPrintStreamSupport.uninstall(ws);
    }

    public static void install(NutsTerminalMode type, NutsWorkspace ws) {
        AnsiPrintStreamSupport.install(type,ws);
    }

    public static void installStdOut(NutsTerminalMode type, NutsWorkspace ws) {
        AnsiPrintStreamSupport.installStdOut(type,ws);
    }

    public static void uninstallStdOut(NutsWorkspace ws) {
        AnsiPrintStreamSupport.uninstallStdOut(ws);
    }

    public static void installStdErr(NutsTerminalMode type, NutsWorkspace ws) {
        AnsiPrintStreamSupport.installStdErr(type,ws);
    }

    public static void uninstallStdErr(NutsWorkspace ws) {
        AnsiPrintStreamSupport.uninstallStdErr(ws);
    }

}
