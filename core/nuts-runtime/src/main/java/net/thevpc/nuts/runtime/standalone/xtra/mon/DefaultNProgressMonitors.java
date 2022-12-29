package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NProgressHandler;
import net.thevpc.nuts.util.NProgressMonitors;
import net.thevpc.nuts.util.NProgressMonitor;

import java.io.PrintStream;
import java.util.logging.Logger;

public class DefaultNProgressMonitors implements NProgressMonitors {
    private NSession session;

    public DefaultNProgressMonitors(NSession session) {
        this.session = session;
    }

    @Override
    public NProgressMonitor ofSilent() {
        return new DefaultProgressMonitor(null,
                new SilentProgressHandler(),
                null, getSession()
        );
    }


    @Override
    public boolean isSilent(NProgressMonitor monitor) {
        return monitor == null || monitor.isSilent();
    }


    @Override
    public NProgressMonitor[] ofSilent(int count) {
        NProgressMonitor[] mon = new NProgressMonitor[count];
        for (int i = 0; i < count; i++) {
            mon[i] = ofSilent();
        }
        return mon;
    }


    @Override
    public NProgressMonitor ofLogger(String message, long freq) {
        return ofLogger(message, null).temporize(freq);
    }

    @Override
    public NProgressMonitor ofLogger(String message, long freq, Logger out) {
        return ofLogger(message, out).temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(long freq) {
        return ofOut().temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(String message, long freq) {
        return ofOut(message).temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(String message, long freq, PrintStream out) {
        return ofPrintStream(message, out).temporize(freq);
    }


    @Override
    public NProgressMonitor ofPrintStream(PrintStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NProgressMonitor ofPrintStream(String messageFormat, PrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new PrintStreamProgressHandler(messageFormat, printStream),
                null, getSession()
        );
    }

    @Override
    public NProgressMonitor ofPrintStream(NStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NProgressMonitor ofPrintStream(String messageFormat, NStream printStream) {
        return new DefaultProgressMonitor(null,
                new NPrintStreamProgressHandler(messageFormat, printStream, getSession()),
                null, getSession()
        );
    }

    @Override
    public NProgressMonitor ofLogger(String messageFormat, Logger printStream) {
        return new DefaultProgressMonitor(null,
                new JLogProgressHandler(messageFormat, printStream),
                null, getSession()
        );
    }

    @Override
    public NProgressMonitor ofLogger(Logger printStream) {
        return ofLogger(null, printStream);
    }

    @Override
    public NProgressMonitor ofLogger(long milliseconds) {
        return ofLogger().temporize(milliseconds);
    }

    @Override
    public NProgressMonitor ofLogger() {
        return ofLogger(null, null);
    }

    @Override
    public NProgressMonitor ofOut(String messageFormat) {
        return ofPrintStream(messageFormat, System.out);
    }

    @Override
    public NProgressMonitor ofSysOut() {
        return ofPrintStream(null, System.out);
    }

    @Override
    public NProgressMonitor ofSysErr() {
        return ofPrintStream(null, System.err);
    }

    @Override
    public NProgressMonitor ofSysErr(String messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor ofOut() {
        return ofPrintStream(null, getSession().out());
    }

    @Override
    public NProgressMonitor ofErr() {
        return ofPrintStream(null, getSession().err());
    }

    @Override
    public NProgressMonitor ofErr(String messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor of(NProgressHandler monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return new DefaultProgressMonitor(null, monitor, null, session);
    }

    @Override
    public NProgressMonitor of(NProgressMonitor monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return monitor;
    }

    private NSession getSession() {
        return session;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }


}
