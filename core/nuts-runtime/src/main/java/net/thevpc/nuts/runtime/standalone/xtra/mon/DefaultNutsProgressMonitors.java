package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsProgressHandler;
import net.thevpc.nuts.util.NutsProgressMonitors;
import net.thevpc.nuts.util.NutsProgressMonitor;

import java.io.PrintStream;
import java.util.logging.Logger;

public class DefaultNutsProgressMonitors implements NutsProgressMonitors {
    private NutsSession session;

    public DefaultNutsProgressMonitors(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsProgressMonitor ofSilent() {
        return new DefaultProgressMonitor(null,
                new SilentProgressHandler(),
                null, getSession()
        );
    }


    @Override
    public boolean isSilent(NutsProgressMonitor monitor) {
        return monitor == null || monitor.isSilent();
    }


    @Override
    public NutsProgressMonitor[] ofSilent(int count) {
        NutsProgressMonitor[] mon = new NutsProgressMonitor[count];
        for (int i = 0; i < count; i++) {
            mon[i] = ofSilent();
        }
        return mon;
    }


    @Override
    public NutsProgressMonitor ofLogger(String message, long freq) {
        return ofLogger(message, null).temporize(freq);
    }

    @Override
    public NutsProgressMonitor ofLogger(String message, long freq, Logger out) {
        return ofLogger(message, out).temporize(freq);
    }

    @Override
    public NutsProgressMonitor ofOut(long freq) {
        return ofOut().temporize(freq);
    }

    @Override
    public NutsProgressMonitor ofOut(String message, long freq) {
        return ofOut(message).temporize(freq);
    }

    @Override
    public NutsProgressMonitor ofOut(String message, long freq, PrintStream out) {
        return ofPrintStream(message, out).temporize(freq);
    }


    @Override
    public NutsProgressMonitor ofPrintStream(PrintStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NutsProgressMonitor ofPrintStream(String messageFormat, PrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new PrintStreamProgressHandler(messageFormat, printStream),
                null, getSession()
        );
    }

    @Override
    public NutsProgressMonitor ofPrintStream(NutsPrintStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NutsProgressMonitor ofPrintStream(String messageFormat, NutsPrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new NutsPrintStreamProgressHandler(messageFormat, printStream, getSession()),
                null, getSession()
        );
    }

    @Override
    public NutsProgressMonitor ofLogger(String messageFormat, Logger printStream) {
        return new DefaultProgressMonitor(null,
                new JLogProgressHandler(messageFormat, printStream),
                null, getSession()
        );
    }

    @Override
    public NutsProgressMonitor ofLogger(Logger printStream) {
        return ofLogger(null, printStream);
    }

    @Override
    public NutsProgressMonitor ofLogger(long milliseconds) {
        return ofLogger().temporize(milliseconds);
    }

    @Override
    public NutsProgressMonitor ofLogger() {
        return ofLogger(null, null);
    }

    @Override
    public NutsProgressMonitor ofOut(String messageFormat) {
        return ofPrintStream(messageFormat, System.out);
    }

    @Override
    public NutsProgressMonitor ofSysOut() {
        return ofPrintStream(null, System.out);
    }

    @Override
    public NutsProgressMonitor ofSysErr() {
        return ofPrintStream(null, System.err);
    }

    @Override
    public NutsProgressMonitor ofSysErr(String messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NutsProgressMonitor ofOut() {
        return ofPrintStream(null, getSession().out());
    }

    @Override
    public NutsProgressMonitor ofErr() {
        return ofPrintStream(null, getSession().err());
    }

    @Override
    public NutsProgressMonitor ofErr(String messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NutsProgressMonitor of(NutsProgressHandler monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return new DefaultProgressMonitor(null, monitor, null, session);
    }

    @Override
    public NutsProgressMonitor of(NutsProgressMonitor monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return monitor;
    }

    private NutsSession getSession() {
        return session;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }


}
