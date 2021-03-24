package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsSession;
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

    public static void install(NutsTerminalMode type, NutsSession session) {
        AnsiPrintStreamSupport.install(type,session);
    }

    public static void installStdOut(NutsTerminalMode type, NutsSession session) {
        AnsiPrintStreamSupport.installStdOut(type,session);
    }

    public static void uninstallStdOut(NutsWorkspace ws) {
        AnsiPrintStreamSupport.uninstallStdOut(ws);
    }

    public static void installStdErr(NutsTerminalMode type, NutsSession session) {
        AnsiPrintStreamSupport.installStdErr(type,session);
    }

    public static void uninstallStdErr(NutsWorkspace ws) {
        AnsiPrintStreamSupport.uninstallStdErr(ws);
    }

}
