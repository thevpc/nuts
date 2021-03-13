package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.core.format.text.renderer.StripperFormattedPrintStreamRenderer;

public class FPrint {

    public static final FormattedPrintStreamRenderer RENDERER_ANSI = AnsiUnixTermPrintRenderer.ANSI_RENDERER;
    public static final FormattedPrintStreamRenderer RENDERER_ANSI_STRIPPER = StripperFormattedPrintStreamRenderer.STRIPPER;


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
