package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.xtra.time.NDefaultProgressRunner;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.time.NProgressRunner;
import net.thevpc.nuts.util.NMsgTemplate;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.time.NProgressHandler;
import net.thevpc.nuts.time.NProgressMonitors;
import net.thevpc.nuts.time.NProgressMonitor;
import net.thevpc.nuts.util.NOptional;

import java.io.PrintStream;
import java.util.Stack;
import java.util.logging.Logger;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNProgressMonitors implements NProgressMonitors {

    public DefaultNProgressMonitors() {
    }

    @Override
    public NProgressRunner ofRunner() {
        return new NDefaultProgressRunner();
    }

    @Override
    public NProgressMonitor ofSilent() {
        return new DefaultProgressMonitor(null,
                new SilentProgressHandler(),
                null
        );
    }

    @Override
    public NOptional<NProgressMonitor> currentMonitor() {
        NWorkspaceModel m = NWorkspaceExt.of().getModel();
        Stack<NProgressMonitor> u = m.currentProgressMonitors.get();
        if (u == null || u.isEmpty()) {
            return NOptional.ofNamedEmpty("current progress monitor");
        }
        return NOptional.of(u.peek());
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
    public NProgressMonitor ofLogger(NMsgTemplate message, long freq) {
        return ofLogger(message, null).temporize(freq);
    }

    @Override
    public NProgressMonitor ofLogger(NMsgTemplate message, long freq, Logger out) {
        return ofLogger(message, out).temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(long freq) {
        return ofOut().temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(NMsgTemplate message, long freq) {
        return ofOut(message).temporize(freq);
    }

    @Override
    public NProgressMonitor ofOut(NMsgTemplate message, long freq, PrintStream out) {
        return ofPrintStream(message, out).temporize(freq);
    }


    @Override
    public NProgressMonitor ofPrintStream(PrintStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, PrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new PrintStreamProgressHandler(messageFormat, printStream),
                null
        );
    }

    @Override
    public NProgressMonitor ofPrintStream(NPrintStream printStream) {
        return ofPrintStream(null, printStream);
    }

    @Override
    public NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, NPrintStream printStream) {
        return new DefaultProgressMonitor(null,
                new NPrintStreamProgressHandler(messageFormat, printStream),
                null
        );
    }

    @Override
    public NProgressMonitor ofLogger(NMsgTemplate messageFormat, Logger printStream) {
        return new DefaultProgressMonitor(null,
                new JLogProgressHandler(messageFormat, printStream),
                null
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
    public NProgressMonitor ofOut(NMsgTemplate messageFormat) {
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
    public NProgressMonitor ofSysErr(NMsgTemplate messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor ofOut() {
        return ofPrintStream(null, NSession.of().out());
    }

    @Override
    public NProgressMonitor ofErr() {
        return ofPrintStream(null, NSession.of().err());
    }

    @Override
    public NProgressMonitor ofErr(NMsgTemplate messageFormat) {
        return ofPrintStream(messageFormat, System.err);
    }

    @Override
    public NProgressMonitor of(NProgressHandler monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return new DefaultProgressMonitor(null, monitor, null);
    }

    @Override
    public NProgressMonitor of(NProgressMonitor monitor) {
        if (monitor == null) {
            return ofSilent();
        }
        return monitor;
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }


}
