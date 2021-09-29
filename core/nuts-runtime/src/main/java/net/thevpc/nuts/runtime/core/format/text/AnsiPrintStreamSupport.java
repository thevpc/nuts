package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOManager;

import java.io.*;
import java.util.Locale;
import net.thevpc.nuts.NutsSession;

public final class AnsiPrintStreamSupport {

    private AnsiPrintStreamSupport() {
    }

//    public static void uninstall(NutsWorkspace ws) {
//        uninstallStdOut(ws);
//        uninstallStdErr(ws);
//    }

//    public static void install(NutsTerminalMode type, NutsSession session) {
//        installStdOut(type,session);
//        installStdErr(type,session);
//    }
//
//    public static void installStdOut(NutsTerminalMode type, NutsSession session) {
//        DefaultNutsIOManager io=(DefaultNutsIOManager) session.io();
//        PrintStream out = io.getModel().getCurrentStdout();
//        if (out instanceof PrintStreamExt && ((PrintStreamExt) out).getOut() instanceof NutsSystemOutputStream) {
//            ((NutsSystemOutputStream) ((PrintStreamExt) out).getOut()).setType(type);
//        } else {
//            io.getModel().setCurrentStdout(new PrintStreamExt(new NutsSystemOutputStream(io.getModel().getBootStdout(true), type,session)));
//        }
//    }
//
//    public static void uninstallStdOut(NutsWorkspace ws) {
//        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
//        io.getModel().setCurrentStdout(null);
//    }
//
//    public static void installStdErr(NutsTerminalMode type, NutsSession session) {
//        DefaultNutsIOManager io=(DefaultNutsIOManager) session.io();
//        PrintStream err = io.getModel().getCurrentStderr();
//        if (err instanceof PrintStreamExt && ((PrintStreamExt) err).getOut() instanceof NutsSystemOutputStream) {
//            ((NutsSystemOutputStream) ((PrintStreamExt) err).getOut()).setType(type);
//        } else {
//            io.getModel().setCurrentStderr(new PrintStreamExt(new NutsSystemOutputStream(io.getModel().getBootStderr(true), type,session)));
//        }
//    }
//
//    public static void uninstallStdErr(NutsWorkspace ws) {
//        DefaultNutsIOManager io=(DefaultNutsIOManager) ws.io();
//        io.getModel().setCurrentStderr(null);
//    }
}
