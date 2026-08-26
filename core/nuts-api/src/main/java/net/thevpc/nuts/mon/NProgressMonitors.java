//package net.thevpc.nuts.mon;
//
//import net.thevpc.nuts.ext.NExtensions;
//import net.thevpc.nuts.log.NLog;
//import net.thevpc.nuts.text.NMsgTemplate;
//import net.thevpc.nuts.io.NPrintStream;
//import net.thevpc.nuts.spi.NComponent;
//import net.thevpc.nuts.util.NOptional;
//
//import java.io.PrintStream;
//import java.util.logging.Logger;
//
//public interface NProgressMonitors extends NComponent {
//    static NProgressMonitors createProgressMonitor() {
//        return NExtensions.of(NProgressMonitors.class);
//    }
//
//
//    NOptional<NProgressMonitor> currentProgressMonitor();
//
//    NProgressRunner ofRunner();
//
//    NProgressMonitor ofSilentProgressMonitor();
//
//    NProgressMonitor[] ofSilentProgressMonitor(int count);
//
//    boolean isSilentProgressMonitor(NProgressMonitor monitor);
//
//    NProgressMonitor ofPrintStreamProgressMonitor(PrintStream printStream);
//
//    NProgressMonitor ofPrintStreamProgressMonitor(NMsgTemplate messageFormat, PrintStream printStream);
//
//    NProgressMonitor ofPrintStreamProgressMonitor(NPrintStream printStream);
//
//    NProgressMonitor ofPrintStreamProgressMonitor(NMsgTemplate messageFormat, NPrintStream printStream);
//
//    NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate messageFormat, Logger printStream);
//
//    NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate messageFormat, NLog printStream);
//
//    NProgressMonitor ofLoggerProgressMonitor(Logger logger);
//
//    NProgressMonitor ofLoggerProgressMonitor(NLog logger);
//
//    NProgressMonitor ofLoggerProgressMonitor(long milliseconds);
//
//    NProgressMonitor ofLoggerProgressMonitor();
//
//    NProgressMonitor createOutProgressMonitor(NMsgTemplate messageFormat);
//
//    NProgressMonitor createSysOutProgressMonitor();
//
//    NProgressMonitor createSysErrProgressMonitor();
//
//    NProgressMonitor createSysErrProgressMonitor(NMsgTemplate messageFormat);
//
//    NProgressMonitor createOutProgressMonitor();
//
//    NProgressMonitor createErrProgressMonitor();
//
//    NProgressMonitor createErrProgressMonitor(NMsgTemplate messageFormat);
//
//    NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq);
//
//    NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq, Logger out);
//
//    NProgressMonitor ofLoggerProgressMonitor(NMsgTemplate message, long freq, NLog out);
//
//    NProgressMonitor createOutProgressMonitor(long freq);
//
//    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq);
//
//    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq, PrintStream out);
//
//    NProgressMonitor createProgressMonitor(NProgressMonitor monitor);
//
//    NProgressMonitor createProgressMonitor(NProgressHandler monitor);
//}
