package net.thevpc.nuts.mon;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgTemplate;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 15 mai 2007 01:34:27
 */
public interface NProgressMonitor {
    double INDETERMINATE_PROGRESS = Double.NaN;



    static NProgressMonitor of() {
        NOptional<NProgressMonitor> m = get();
        if (m.isPresent()) {
            return m.get();
        }
        return ofSilent();
    }


    static NOptional<NProgressMonitor> get(){
        return NIORPI.of().currentProgressMonitor();
    }

    static NProgressMonitor ofSilent(){
        return NIORPI.of().createSilentProgressMonitor();
    }

    static NProgressMonitor[] ofSilent(int count){
        return NIORPI.of().createSilentProgressMonitor(count);
    }

    static boolean isSilent(NProgressMonitor monitor){
        return NIORPI.of().isSilentProgressMonitor(monitor);
    }

    static NProgressMonitor ofPrintStream(PrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(printStream);
    }

    static NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, PrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(messageFormat, printStream);
    }

    static NProgressMonitor ofPrintStream(NPrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(printStream);
    }

    static NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, NPrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(messageFormat,printStream);
    }

    static NProgressMonitor ofLogger(NMsgTemplate messageFormat, Logger printStream){
        return NIORPI.of().createLoggerProgressMonitor(messageFormat,printStream);
    }

    static NProgressMonitor ofLogger(NMsgTemplate messageFormat, NLog printStream){
        return NIORPI.of().createLoggerProgressMonitor(messageFormat,printStream);
    }

    static NProgressMonitor ofLogger(Logger logger){
        return NIORPI.of().createLoggerProgressMonitor(logger);
    }

    static NProgressMonitor ofLogger(NLog logger){
        return NIORPI.of().createLoggerProgressMonitor(logger);
    }

    static NProgressMonitor ofLogger(long milliseconds){
        return NIORPI.of().createLoggerProgressMonitor(milliseconds);
    }

    static NProgressMonitor ofLogger(){
        return NIORPI.of().createLoggerProgressMonitor();
    }

    static NProgressMonitor ofOut(NMsgTemplate messageFormat){
        return NIORPI.of().createOutProgressMonitor(messageFormat);
    }

    static NProgressMonitor ofSysOut(){
        return NIORPI.of().createSysOutProgressMonitor();
    }

    static NProgressMonitor ofSysErr(){
        return NIORPI.of().createSysErrProgressMonitor();
    }

    static NProgressMonitor ofSysErr(NMsgTemplate messageFormat){
        return NIORPI.of().createSysErrProgressMonitor(messageFormat);
    }

    static NProgressMonitor ofOut(){
        return NIORPI.of().createOutProgressMonitor();
    }

    static NProgressMonitor ofErr(){
        return NIORPI.of().createErrProgressMonitor();
    }

    static NProgressMonitor ofErr(NMsgTemplate messageFormat){
        return NIORPI.of().createErrProgressMonitor(messageFormat);
    }

    static NProgressMonitor ofLogger(NMsgTemplate message, long freq){
        return NIORPI.of().createLoggerProgressMonitor(message,freq);
    }

    static NProgressMonitor ofLogger(NMsgTemplate message, long freq, Logger out){
        return NIORPI.of().createLoggerProgressMonitor(message,freq,out);
    }

    static NProgressMonitor ofLogger(NMsgTemplate message, long freq, NLog out){
        return NIORPI.of().createLoggerProgressMonitor(message,freq,out);
    }

    static NProgressMonitor ofOut(long freq){
        return NIORPI.of().createOutProgressMonitor(freq);
    }

    static NProgressMonitor ofOut(NMsgTemplate message, long freq){
        return NIORPI.of().createOutProgressMonitor(message,freq);
    }

    static NProgressMonitor ofOut(NMsgTemplate message, long freq, PrintStream out){
        return NIORPI.of().createOutProgressMonitor(message,freq,out);
    }

    static NProgressMonitor of(NProgressMonitor monitor){
        return NIORPI.of().createProgressMonitor(monitor);
    }

    static NProgressMonitor of(NProgressHandler monitor){
        return NIORPI.of().createProgressMonitor(monitor);
    }


    NProgressMonitor start();

    NProgressMonitor start(NMsg message);

    NProgressMonitor complete();

    NProgressMonitor complete(NMsg message);

    NProgressMonitor undoComplete();

    NProgressMonitor undoComplete(NMsg message);

    NProgressMonitor cancel();

    NProgressMonitor undoCancel();

    NProgressMonitor cancel(NMsg message);

    NProgressMonitor undoCancel(NMsg message);

    NProgressMonitor undoSuspend();

    NProgressMonitor undoSuspend(NMsg message);

    NProgressMonitor suspend();

    NProgressMonitor suspend(NMsg message);

    boolean isSuspended();

    boolean isCompleted();

    boolean isBlocked();

    NProgressMonitor block();

    NProgressMonitor block(NMsg message);

    NProgressMonitor undoBlock();

    NProgressMonitor undoBlock(NMsg message);

    boolean isStarted();

    boolean isCanceled();

    void reset();

    String id();

    String name();

    NMsg description();

    NProgressMonitor addListener(NProgressListener listener);

    NProgressMonitor removeListener(NProgressListener listener);

    List<NProgressListener> listeners();

    NDuration duration();

    NClock startClock();

    NProgressMonitor message(NMsg message);

    NMsg message();

    boolean isIndeterminate();

    double progress();

    NProgressMonitor progress(double progress);

    /**
     * [0..1]
     *
     * @param progress
     * @param message
     */
    NProgressMonitor progress(double progress, NMsg message);

    NProgressMonitor indeterminate();

    NProgressMonitor indeterminate(NMsg message);

    NProgressMonitor progress(long i, long max);

    NProgressMonitor progress(long i, long max, NMsg message);

    NProgressMonitor progress(long i, long maxi, long j, long maxj);

    NProgressMonitor progress(long i, long maxi, long j, long maxj, NMsg message);

    NProgressMonitor inc();

    NProgressMonitor inc(NMsg message);

    NDuration estimatedTotalDuration();

    NDuration estimatedRemainingDuration();

    NProgressMonitor translate(long index, long max);

    NProgressMonitor translate(long i, long imax, long j, long jmax);

    NProgressMonitor stepInto(NMsg message);

    NProgressMonitor stepInto(long index, long max);

    NProgressMonitor temporize(long freq);

    NProgressMonitor incremental(long iterations);

    NProgressMonitor incremental(double delta);

    NProgressMonitor translate(double factor, double start);

    NProgressMonitor[] split(int nbrElements);

    NProgressMonitor[] split(double... weight);

    boolean isSilent();

    void runWithAll(Runnable... runnable);

    void runWithAll(Runnable[] runnable, double[] weights);

    void runWith(Runnable runnable);

    <T> T callWith(NCallable<T> callable);
}
