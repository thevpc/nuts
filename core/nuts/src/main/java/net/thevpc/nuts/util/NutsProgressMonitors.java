package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.PrintStream;
import java.util.logging.Logger;

public interface NutsProgressMonitors extends NutsComponent {
    static NutsProgressMonitors of(NutsSession session) {
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsProgressMonitors.class, true, null);
    }


    NutsProgressMonitor ofSilent();

    NutsProgressMonitor[] ofSilent(int count);

    boolean isSilent(NutsProgressMonitor monitor);

    NutsProgressMonitor ofPrintStream(PrintStream printStream);

    NutsProgressMonitor ofPrintStream(String messageFormat, PrintStream printStream);

    NutsProgressMonitor ofPrintStream(NutsPrintStream printStream);

    NutsProgressMonitor ofPrintStream(String messageFormat, NutsPrintStream printStream);

    NutsProgressMonitor ofLogger(String messageFormat, Logger printStream);

    NutsProgressMonitor ofLogger(Logger printStream);

    NutsProgressMonitor ofLogger(long milliseconds);

    NutsProgressMonitor ofLogger();

    NutsProgressMonitor ofOut(String messageFormat);

    NutsProgressMonitor ofSysOut();

    NutsProgressMonitor ofSysErr();

    NutsProgressMonitor ofSysErr(String messageFormat);

    NutsProgressMonitor ofOut();

    NutsProgressMonitor ofErr();

    NutsProgressMonitor ofErr(String messageFormat);

    NutsProgressMonitor ofLogger(String message, long freq);

    NutsProgressMonitor ofLogger(String message, long freq, Logger out);

    NutsProgressMonitor ofOut(long freq);

    NutsProgressMonitor ofOut(String message, long freq);

    NutsProgressMonitor ofOut(String message, long freq, PrintStream out);

    NutsProgressMonitor of(NutsProgressMonitor monitor);
    NutsProgressMonitor of(NutsProgressHandler monitor);
}
