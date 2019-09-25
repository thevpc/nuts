package net.vpc.app.nuts.runtime.util.fprint;

import java.io.PrintStream;
import net.vpc.app.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.vpc.app.nuts.runtime.util.fprint.renderer.StripperFormattedPrintStreamRenderer;

public class FPrint {

    public static final FormattedPrintStreamRenderer RENDERER_ANSI = AnsiUnixTermPrintRenderer.ANSI_RENDERER;
    public static final FormattedPrintStreamRenderer RENDERER_ANSI_STRIPPER = StripperFormattedPrintStreamRenderer.STRIPPER;
    static PrintStream out = null;
    static PrintStream err = null;

    public static PrintStream out() {
        return out == null ? System.out : out;
    }

    public static PrintStream err() {
        return err == null ? System.err : err;
    }

    public static void uninstall() {
        AnsiPrintStreamSupport.uninstall();
    }

    public static void install(AnsiPrintStreamSupport.Type type) {
        AnsiPrintStreamSupport.install(type);
    }

    public static void installStdOut(AnsiPrintStreamSupport.Type type) {
        AnsiPrintStreamSupport.installStdOut(type);
    }

    public static void uninstallStdOut() {
        AnsiPrintStreamSupport.uninstallStdOut();
    }

    public static void installStdErr(AnsiPrintStreamSupport.Type type) {
        AnsiPrintStreamSupport.installStdErr(type);
    }

    public static void uninstallStdErr() {
        AnsiPrintStreamSupport.uninstallStdErr();
    }

}
