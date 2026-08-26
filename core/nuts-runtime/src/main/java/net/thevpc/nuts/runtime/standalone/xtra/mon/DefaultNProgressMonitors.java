//package net.thevpc.nuts.runtime.standalone.xtra.mon;
//
//import net.thevpc.nuts.log.NLog;
//import net.thevpc.nuts.runtime.standalone.xtra.time.NDefaultProgressRunner;
//import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
//import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
//import net.thevpc.nuts.spi.NComponentScope;
//import net.thevpc.nuts.spi.NScopeType;
//import net.thevpc.nuts.reflect.NScore;
//import net.thevpc.nuts.reflect.NScorable;
//import net.thevpc.nuts.mon.NProgressRunner;
//import net.thevpc.nuts.text.NMsgTemplate;
//import net.thevpc.nuts.core.NSession;
//import net.thevpc.nuts.io.NPrintStream;
//import net.thevpc.nuts.mon.NProgressHandler;
//import net.thevpc.nuts.mon.NProgressMonitors;
//import net.thevpc.nuts.mon.NProgressMonitor;
//import net.thevpc.nuts.util.NOptional;
//
//import java.io.PrintStream;
//import java.util.logging.Logger;
//
//@NComponentScope(NScopeType.WORKSPACE)
//@NScore(fixed = NScorable.DEFAULT_SCORE)
//public class DefaultNProgressMonitors implements NProgressMonitors {
//
//    public DefaultNProgressMonitors() {
//    }
//
//    @Override
//    public NProgressRunner ofRunner() {
//        return new NDefaultProgressRunner();
//    }
//
//    @Override
//    public NProgressMonitor ofSilentProgressMonitor() {
//        return new DefaultProgressMonitor(null,
//                new SilentProgressHandler(),
//                null
//        );
//    }
//
//    @Override
//    public NOptional<NProgressMonitor> currentProgressMonitor() {
//        NWorkspaceModel m = NWorkspaceExt.of().getModel();
//        return NOptional.of(m.currentProgressMonitors.get());
//    }
//
//    @Override
//    public boolean isSilentProgressMonitor(NProgressMonitor monitor) {
//        return monitor == null || monitor.isSilent();
//    }
//
//
//    @Override
//    public NProgressMonitor[] ofSilentProgressMonitor(int count) {
//        NProgressMonitor[] mon = new NProgressMonitor[count];
//        for (int i = 0; i < count; i++) {
//            mon[i] = ofSilentProgressMonitor();
//        }
//        return mon;
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq) {
//        return ofLoggerProgressMonitor(message, (NLog)null).temporize(freq);
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq, Logger out) {
//        return ofLoggerProgressMonitor(message, out).temporize(freq);
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq, NLog out) {
//        return ofLoggerProgressMonitor(message, out).temporize(freq);
//    }
//
//    @Override
//    public NProgressMonitor createOutProgressMonitor(long freq) {
//        return createOutProgressMonitor().temporize(freq);
//    }
//
//    @Override
//    public NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq) {
//        return createOutProgressMonitor(message).temporize(freq);
//    }
//
//    @Override
//    public NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq, PrintStream out) {
//        return ofPrintStreamProgressMonitor(message, out).temporize(freq);
//    }
//
//
//    @Override
//    public NProgressMonitor ofPrintStreamProgressMonitor(PrintStream printStream) {
//        return ofPrintStreamProgressMonitor(null, printStream);
//    }
//
//    @Override
//    public NProgressMonitor ofPrintStreamProgressMonitor(NMsgTemplate messageFormat, PrintStream printStream) {
//        return new DefaultProgressMonitor(null,
//                new PrintStreamProgressHandler(messageFormat, printStream),
//                null
//        );
//    }
//
//    @Override
//    public NProgressMonitor ofPrintStreamProgressMonitor(NPrintStream printStream) {
//        return ofPrintStreamProgressMonitor(null, printStream);
//    }
//
//    @Override
//    public NProgressMonitor ofPrintStreamProgressMonitor(NMsgTemplate messageFormat, NPrintStream printStream) {
//        return new DefaultProgressMonitor(null,
//                new NPrintStreamProgressHandler(messageFormat, printStream),
//                null
//        );
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate messageFormat, Logger printStream) {
//        return ofLoggerProgressMonitor(messageFormat,printStream==null?null:NLog.of(printStream));
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate messageFormat, NLog log) {
//        return new DefaultProgressMonitor(null,
//                new JLogProgressHandler(messageFormat, log),
//                null
//        );
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(Logger logger) {
//        return ofLoggerProgressMonitor(null, logger);
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(NLog logger) {
//        return ofLoggerProgressMonitor(null, logger);
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor(long milliseconds) {
//        return ofLoggerProgressMonitor().temporize(milliseconds);
//    }
//
//    @Override
//    public NProgressMonitor ofLoggerProgressMonitor() {
//        return ofLoggerProgressMonitor(null, (NLog) null);
//    }
//
//    @Override
//    public NProgressMonitor createOutProgressMonitor(NMsgTemplate messageFormat) {
//        return ofPrintStreamProgressMonitor(messageFormat, System.out);
//    }
//
//    @Override
//    public NProgressMonitor createSysOutProgressMonitor() {
//        return ofPrintStreamProgressMonitor(null, System.out);
//    }
//
//    @Override
//    public NProgressMonitor createSysErrProgressMonitor() {
//        return ofPrintStreamProgressMonitor(null, System.err);
//    }
//
//    @Override
//    public NProgressMonitor createSysErrProgressMonitor(NMsgTemplate messageFormat) {
//        return ofPrintStreamProgressMonitor(messageFormat, System.err);
//    }
//
//    @Override
//    public NProgressMonitor createOutProgressMonitor() {
//        return ofPrintStreamProgressMonitor(null, NSession.of().out());
//    }
//
//    @Override
//    public NProgressMonitor createErrProgressMonitor() {
//        return ofPrintStreamProgressMonitor(null, NSession.of().err());
//    }
//
//    @Override
//    public NProgressMonitor createErrProgressMonitor(NMsgTemplate messageFormat) {
//        return ofPrintStreamProgressMonitor(messageFormat, System.err);
//    }
//
//    @Override
//    public NProgressMonitor createProgressMonitor(NProgressHandler monitor) {
//        if (monitor == null) {
//            return ofSilentProgressMonitor();
//        }
//        return new DefaultProgressMonitor(null, monitor, null);
//    }
//
//    @Override
//    public NProgressMonitor createProgressMonitor(NProgressMonitor monitor) {
//        if (monitor == null) {
//            return ofSilentProgressMonitor();
//        }
//        return monitor;
//    }
//
//}
