package net.thevpc.nuts.time;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NMsgTemplate;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.io.PrintStream;
import java.util.logging.Logger;

public interface NProgressMonitors extends NComponent {
    static NProgressMonitors of() {
        return NExtensions.of(NProgressMonitors.class);
    }


    NOptional<NProgressMonitor> currentMonitor();

    NProgressRunner ofRunner();

    NProgressMonitor ofSilent();

    NProgressMonitor[] ofSilent(int count);

    boolean isSilent(NProgressMonitor monitor);

    NProgressMonitor ofPrintStream(PrintStream printStream);

    NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, PrintStream printStream);

    NProgressMonitor ofPrintStream(NPrintStream printStream);

    NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, NPrintStream printStream);

    NProgressMonitor ofLogger(NMsgTemplate messageFormat, Logger printStream);

    NProgressMonitor ofLogger(Logger printStream);

    NProgressMonitor ofLogger(long milliseconds);

    NProgressMonitor ofLogger();

    NProgressMonitor ofOut(NMsgTemplate messageFormat);

    NProgressMonitor ofSysOut();

    NProgressMonitor ofSysErr();

    NProgressMonitor ofSysErr(NMsgTemplate messageFormat);

    NProgressMonitor ofOut();

    NProgressMonitor ofErr();

    NProgressMonitor ofErr(NMsgTemplate messageFormat);

    NProgressMonitor ofLogger(NMsgTemplate message, long freq);

    NProgressMonitor ofLogger(NMsgTemplate message, long freq, Logger out);

    NProgressMonitor ofOut(long freq);

    NProgressMonitor ofOut(NMsgTemplate message, long freq);

    NProgressMonitor ofOut(NMsgTemplate message, long freq, PrintStream out);

    NProgressMonitor of(NProgressMonitor monitor);

    NProgressMonitor of(NProgressHandler monitor);
}
