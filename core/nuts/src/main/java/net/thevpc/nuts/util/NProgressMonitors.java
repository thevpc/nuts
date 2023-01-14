package net.thevpc.nuts.util;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponent;

import java.io.PrintStream;
import java.util.logging.Logger;

public interface NProgressMonitors extends NComponent {
    static NProgressMonitors of(NSession session) {
       return NExtensions.of(session).createSupported(NProgressMonitors.class);
    }


    NProgressMonitor ofSilent();

    NProgressMonitor[] ofSilent(int count);

    boolean isSilent(NProgressMonitor monitor);

    NProgressMonitor ofPrintStream(PrintStream printStream);

    NProgressMonitor ofPrintStream(String messageFormat, PrintStream printStream);

    NProgressMonitor ofPrintStream(NPrintStream printStream);

    NProgressMonitor ofPrintStream(String messageFormat, NPrintStream printStream);

    NProgressMonitor ofLogger(String messageFormat, Logger printStream);

    NProgressMonitor ofLogger(Logger printStream);

    NProgressMonitor ofLogger(long milliseconds);

    NProgressMonitor ofLogger();

    NProgressMonitor ofOut(String messageFormat);

    NProgressMonitor ofSysOut();

    NProgressMonitor ofSysErr();

    NProgressMonitor ofSysErr(String messageFormat);

    NProgressMonitor ofOut();

    NProgressMonitor ofErr();

    NProgressMonitor ofErr(String messageFormat);

    NProgressMonitor ofLogger(String message, long freq);

    NProgressMonitor ofLogger(String message, long freq, Logger out);

    NProgressMonitor ofOut(long freq);

    NProgressMonitor ofOut(String message, long freq);

    NProgressMonitor ofOut(String message, long freq, PrintStream out);

    NProgressMonitor of(NProgressMonitor monitor);
    NProgressMonitor of(NProgressHandler monitor);
}
