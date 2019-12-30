package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.io.DefaultNutsIOManager;

import java.io.*;
import java.util.Locale;

public final class AnsiPrintStreamSupport {

    static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    static final boolean IS_CYGWIN = IS_WINDOWS
            && System.getenv("PWD") != null
            && System.getenv("PWD").startsWith("/")
            && !"cygwin".equals(System.getenv("TERM"));

    static final boolean IS_MINGW_XTERM = IS_WINDOWS
            && System.getenv("MSYSTEM") != null
            && System.getenv("MSYSTEM").startsWith("MINGW")
            && "xterm".equals(System.getenv("TERM"));

    private AnsiPrintStreamSupport() {
    }

    public static void uninstall(NutsWorkspace ws) {
        uninstallStdOut(ws);
        uninstallStdErr(ws);
    }

    public static void install(NutsTerminalMode type, NutsWorkspace ws) {
        installStdOut(type,ws);
        installStdErr(type,ws);
    }

    public static void installStdOut(NutsTerminalMode type, NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        PrintStream out = io.getCurrentStdout();
        if (out instanceof PrintStreamExt && ((PrintStreamExt) out).getOut() instanceof NutsSystemOutputStream) {
            ((NutsSystemOutputStream) ((PrintStreamExt) out).getOut()).setType(type);
        } else {
            io.setCurrentStdout(new PrintStreamExt(new NutsSystemOutputStream(io.getBootStdout(true), type,ws)));
        }
    }

    public static void uninstallStdOut(NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        io.setCurrentStdout(null);
    }

    public static void installStdErr(NutsTerminalMode type, NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        PrintStream err = io.getCurrentStderr();
        if (err instanceof PrintStreamExt && ((PrintStreamExt) err).getOut() instanceof NutsSystemOutputStream) {
            ((NutsSystemOutputStream) ((PrintStreamExt) err).getOut()).setType(type);
        } else {
            io.setCurrentStderr(new PrintStreamExt(new NutsSystemOutputStream(io.getBootStderr(true), type,ws)));
        }
    }

    public static void uninstallStdErr(NutsWorkspace ws) {
        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
        io.setCurrentStderr(null);
    }
}
