package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOManager;

import java.io.*;
import java.util.Locale;
import net.thevpc.nuts.NutsSession;

public final class AnsiPrintStreamSupport {

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    public static final boolean IS_CYGWIN = IS_WINDOWS
            && System.getenv("PWD") != null
            && System.getenv("PWD").startsWith("/")
            && !"cygwin".equals(System.getenv("TERM"));

    public static final boolean IS_MINGW_XTERM = IS_WINDOWS
            && System.getenv("MSYSTEM") != null
            && System.getenv("MSYSTEM").startsWith("MINGW")
            && "xterm".equals(System.getenv("TERM"));

    private AnsiPrintStreamSupport() {
    }

    public static void uninstall(NutsWorkspace ws) {
        uninstallStdOut(ws);
        uninstallStdErr(ws);
    }

    public static void install(NutsTerminalMode type, NutsSession session) {
        installStdOut(type,session);
        installStdErr(type,session);
    }

    public static void installStdOut(NutsTerminalMode type, NutsSession session) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) session.getWorkspace().io();
        PrintStream out = io.getCurrentStdout();
        if (out instanceof PrintStreamExt && ((PrintStreamExt) out).getOut() instanceof NutsSystemOutputStream) {
            ((NutsSystemOutputStream) ((PrintStreamExt) out).getOut()).setType(type);
        } else {
            io.setCurrentStdout(new PrintStreamExt(new NutsSystemOutputStream(io.getBootStdout(true), type,session)));
        }
    }

    public static void uninstallStdOut(NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        io.setCurrentStdout(null);
    }

    public static void installStdErr(NutsTerminalMode type, NutsSession session) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) session.getWorkspace().io();
        PrintStream err = io.getCurrentStderr();
        if (err instanceof PrintStreamExt && ((PrintStreamExt) err).getOut() instanceof NutsSystemOutputStream) {
            ((NutsSystemOutputStream) ((PrintStreamExt) err).getOut()).setType(type);
        } else {
            io.setCurrentStderr(new PrintStreamExt(new NutsSystemOutputStream(io.getBootStderr(true), type,session)));
        }
    }

    public static void uninstallStdErr(NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        io.setCurrentStderr(null);
    }
}
