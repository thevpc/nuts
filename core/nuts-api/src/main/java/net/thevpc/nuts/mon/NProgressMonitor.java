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



    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NProgressMonitor of() {
        NOptional<NProgressMonitor> m = get();
        if (m.isPresent()) {
            return m.get();
        }
        /**
         * Creates a new instance of of silent.
         *
         * @return of silent result
         */
        return ofSilent();
    }


    /**
     * Returns the get.
     *
     * @return get result
     */
    static NOptional<NProgressMonitor> get(){
        return NIORPI.of().currentProgressMonitor();
    }

    /**
     * Creates a new instance of of silent.
     *
     * @return of silent result
     */
    static NProgressMonitor ofSilent(){
        return NIORPI.of().createSilentProgressMonitor();
    }

    /**
     * Creates a new instance of of silent.
     *
     * @param count count
     * @return of silent result
     */
    static NProgressMonitor[] ofSilent(int count){
        return NIORPI.of().createSilentProgressMonitor(count);
    }

    /**
     * Checks if is silent.
     *
     * @param monitor monitor
     * @return is silent result
     */
    static boolean isSilent(NProgressMonitor monitor){
        return NIORPI.of().isSilentProgressMonitor(monitor);
    }

    /**
     * Creates a new instance of of print stream.
     *
     * @param printStream print stream
     * @return of print stream result
     */
    static NProgressMonitor ofPrintStream(PrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(printStream);
    }

    /**
     * Creates a new instance of of print stream.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return of print stream result
     */
    static NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, PrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(messageFormat, printStream);
    }

    /**
     * Creates a new instance of of print stream.
     *
     * @param printStream print stream
     * @return of print stream result
     */
    static NProgressMonitor ofPrintStream(NPrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(printStream);
    }

    /**
     * Creates a new instance of of print stream.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return of print stream result
     */
    static NProgressMonitor ofPrintStream(NMsgTemplate messageFormat, NPrintStream printStream){
        return NIORPI.of().createPrintStreamProgressMonitor(messageFormat,printStream);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NMsgTemplate messageFormat, Logger printStream){
        return NIORPI.of().createLoggerProgressMonitor(messageFormat,printStream);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NMsgTemplate messageFormat, NLog printStream){
        return NIORPI.of().createLoggerProgressMonitor(messageFormat,printStream);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param logger logger
     * @return of logger result
     */
    static NProgressMonitor ofLogger(Logger logger){
        return NIORPI.of().createLoggerProgressMonitor(logger);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param logger logger
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NLog logger){
        return NIORPI.of().createLoggerProgressMonitor(logger);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param milliseconds milliseconds
     * @return of logger result
     */
    static NProgressMonitor ofLogger(long milliseconds){
        return NIORPI.of().createLoggerProgressMonitor(milliseconds);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @return of logger result
     */
    static NProgressMonitor ofLogger(){
        return NIORPI.of().createLoggerProgressMonitor();
    }

    /**
     * Creates a new instance of of out.
     *
     * @param messageFormat message format
     * @return of out result
     */
    static NProgressMonitor ofOut(NMsgTemplate messageFormat){
        return NIORPI.of().createOutProgressMonitor(messageFormat);
    }

    /**
     * Creates a new instance of of sys out.
     *
     * @return of sys out result
     */
    static NProgressMonitor ofSysOut(){
        return NIORPI.of().createSysOutProgressMonitor();
    }

    /**
     * Creates a new instance of of sys err.
     *
     * @return of sys err result
     */
    static NProgressMonitor ofSysErr(){
        return NIORPI.of().createSysErrProgressMonitor();
    }

    /**
     * Creates a new instance of of sys err.
     *
     * @param messageFormat message format
     * @return of sys err result
     */
    static NProgressMonitor ofSysErr(NMsgTemplate messageFormat){
        return NIORPI.of().createSysErrProgressMonitor(messageFormat);
    }

    /**
     * Creates a new instance of of out.
     *
     * @return of out result
     */
    static NProgressMonitor ofOut(){
        return NIORPI.of().createOutProgressMonitor();
    }

    /**
     * Creates a new instance of of err.
     *
     * @return of err result
     */
    static NProgressMonitor ofErr(){
        return NIORPI.of().createErrProgressMonitor();
    }

    /**
     * Creates a new instance of of err.
     *
     * @param messageFormat message format
     * @return of err result
     */
    static NProgressMonitor ofErr(NMsgTemplate messageFormat){
        return NIORPI.of().createErrProgressMonitor(messageFormat);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param message message
     * @param freq freq
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NMsgTemplate message, long freq){
        return NIORPI.of().createLoggerProgressMonitor(message,freq);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NMsgTemplate message, long freq, Logger out){
        return NIORPI.of().createLoggerProgressMonitor(message,freq,out);
    }

    /**
     * Creates a new instance of of logger.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return of logger result
     */
    static NProgressMonitor ofLogger(NMsgTemplate message, long freq, NLog out){
        return NIORPI.of().createLoggerProgressMonitor(message,freq,out);
    }

    /**
     * Creates a new instance of of out.
     *
     * @param freq freq
     * @return of out result
     */
    static NProgressMonitor ofOut(long freq){
        return NIORPI.of().createOutProgressMonitor(freq);
    }

    /**
     * Creates a new instance of of out.
     *
     * @param message message
     * @param freq freq
     * @return of out result
     */
    static NProgressMonitor ofOut(NMsgTemplate message, long freq){
        return NIORPI.of().createOutProgressMonitor(message,freq);
    }

    /**
     * Creates a new instance of of out.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return of out result
     */
    static NProgressMonitor ofOut(NMsgTemplate message, long freq, PrintStream out){
        return NIORPI.of().createOutProgressMonitor(message,freq,out);
    }

    /**
     * Creates a new instance of of.
     *
     * @param monitor monitor
     * @return of result
     */
    static NProgressMonitor of(NProgressMonitor monitor){
        return NIORPI.of().createProgressMonitor(monitor);
    }

    /**
     * Creates a new instance of of.
     *
     * @param monitor monitor
     * @return of result
     */
    static NProgressMonitor of(NProgressHandler monitor){
        return NIORPI.of().createProgressMonitor(monitor);
    }


    /**
     * Start.
     *
     * @return start result
     */
    NProgressMonitor start();

    /**
     * Start.
     *
     * @param message message
     * @return start result
     */
    NProgressMonitor start(NMsg message);

    /**
     * Complete.
     *
     * @return complete result
     */
    NProgressMonitor complete();

    /**
     * Complete.
     *
     * @param message message
     * @return complete result
     */
    NProgressMonitor complete(NMsg message);

    /**
     * Undo complete.
     *
     * @return undo complete result
     */
    NProgressMonitor undoComplete();

    /**
     * Undo complete.
     *
     * @param message message
     * @return undo complete result
     */
    NProgressMonitor undoComplete(NMsg message);

    /**
     * Checks if cancel.
     *
     * @return cancel result
     */
    NProgressMonitor cancel();

    /**
     * Undo cancel.
     *
     * @return undo cancel result
     */
    NProgressMonitor undoCancel();

    /**
     * Checks if cancel.
     *
     * @param message message
     * @return cancel result
     */
    NProgressMonitor cancel(NMsg message);

    /**
     * Undo cancel.
     *
     * @param message message
     * @return undo cancel result
     */
    NProgressMonitor undoCancel(NMsg message);

    /**
     * Undo suspend.
     *
     * @return undo suspend result
     */
    NProgressMonitor undoSuspend();

    /**
     * Undo suspend.
     *
     * @param message message
     * @return undo suspend result
     */
    NProgressMonitor undoSuspend(NMsg message);

    /**
     * Suspend.
     *
     * @return suspend result
     */
    NProgressMonitor suspend();

    /**
     * Suspend.
     *
     * @param message message
     * @return suspend result
     */
    NProgressMonitor suspend(NMsg message);

    /**
     * Checks if is suspended.
     *
     * @return is suspended result
     */
    boolean isSuspended();

    /**
     * Checks if is completed.
     *
     * @return is completed result
     */
    boolean isCompleted();

    /**
     * Checks if is blocked.
     *
     * @return is blocked result
     */
    boolean isBlocked();

    /**
     * Block.
     *
     * @return block result
     */
    NProgressMonitor block();

    /**
     * Block.
     *
     * @param message message
     * @return block result
     */
    NProgressMonitor block(NMsg message);

    /**
     * Undo block.
     *
     * @return undo block result
     */
    NProgressMonitor undoBlock();

    /**
     * Undo block.
     *
     * @param message message
     * @return undo block result
     */
    NProgressMonitor undoBlock(NMsg message);

    /**
     * Checks if is started.
     *
     * @return is started result
     */
    boolean isStarted();

    /**
     * Checks if is canceled.
     *
     * @return is canceled result
     */
    boolean isCanceled();

    /**
     * Reset.
     */
    void reset();

    /**
     * Id.
     *
     * @return id result
     */
    String id();

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Description.
     *
     * @return description result
     */
    NMsg description();

    /**
     * Adds the specified listener.
     *
     * @param listener listener
     * @return add listener result
     */
    NProgressMonitor addListener(NProgressListener listener);

    /**
     * Removes the specified listener.
     *
     * @param listener listener
     * @return remove listener result
     */
    NProgressMonitor removeListener(NProgressListener listener);

    /**
     * Listeners.
     *
     * @return listeners result
     */
    List<NProgressListener> listeners();

    /**
     * Duration.
     *
     * @return duration result
     */
    NDuration duration();

    /**
     * Start clock.
     *
     * @return start clock result
     */
    NClock startClock();

    /**
     * Message.
     *
     * @param message message
     * @return message result
     */
    NProgressMonitor message(NMsg message);

    /**
     * Message.
     *
     * @return message result
     */
    NMsg message();

    /**
     * Checks if is indeterminate.
     *
     * @return is indeterminate result
     */
    boolean isIndeterminate();

    /**
     * Progress.
     *
     * @return progress result
     */
    double progress();

    /**
     * Progress.
     *
     * @param progress progress
     * @return progress result
     */
    NProgressMonitor progress(double progress);

    /**
     * [0..1]
     *
     * @param progress
     * @param message
     */
    NProgressMonitor progress(double progress, NMsg message);

    /**
     * Indeterminate.
     *
     * @return indeterminate result
     */
    NProgressMonitor indeterminate();

    /**
     * Indeterminate.
     *
     * @param message message
     * @return indeterminate result
     */
    NProgressMonitor indeterminate(NMsg message);

    /**
     * Progress.
     *
     * @param i i
     * @param max max
     * @return progress result
     */
    NProgressMonitor progress(long i, long max);

    /**
     * Progress.
     *
     * @param i i
     * @param max max
     * @param message message
     * @return progress result
     */
    NProgressMonitor progress(long i, long max, NMsg message);

    /**
     * Progress.
     *
     * @param i i
     * @param maxi maxi
     * @param j j
     * @param maxj maxj
     * @return progress result
     */
    NProgressMonitor progress(long i, long maxi, long j, long maxj);

    /**
     * Progress.
     *
     * @param i i
     * @param maxi maxi
     * @param j j
     * @param maxj maxj
     * @param message message
     * @return progress result
     */
    NProgressMonitor progress(long i, long maxi, long j, long maxj, NMsg message);

    /**
     * Inc.
     *
     * @return inc result
     */
    NProgressMonitor inc();

    /**
     * Inc.
     *
     * @param message message
     * @return inc result
     */
    NProgressMonitor inc(NMsg message);

    /**
     * Estimated total duration.
     *
     * @return estimated total duration result
     */
    NDuration estimatedTotalDuration();

    /**
     * Estimated remaining duration.
     *
     * @return estimated remaining duration result
     */
    NDuration estimatedRemainingDuration();

    /**
     * Translate.
     *
     * @param index index
     * @param max max
     * @return translate result
     */
    NProgressMonitor translate(long index, long max);

    /**
     * Translate.
     *
     * @param i i
     * @param imax imax
     * @param j j
     * @param jmax jmax
     * @return translate result
     */
    NProgressMonitor translate(long i, long imax, long j, long jmax);

    /**
     * Step into.
     *
     * @param message message
     * @return step into result
     */
    NProgressMonitor stepInto(NMsg message);

    /**
     * Step into.
     *
     * @param index index
     * @param max max
     * @return step into result
     */
    NProgressMonitor stepInto(long index, long max);

    /**
     * Temporize.
     *
     * @param freq freq
     * @return temporize result
     */
    NProgressMonitor temporize(long freq);

    /**
     * Incremental.
     *
     * @param iterations iterations
     * @return incremental result
     */
    NProgressMonitor incremental(long iterations);

    /**
     * Incremental.
     *
     * @param delta delta
     * @return incremental result
     */
    NProgressMonitor incremental(double delta);

    /**
     * Translate.
     *
     * @param factor factor
     * @param start start
     * @return translate result
     */
    NProgressMonitor translate(double factor, double start);

    /**
     * Split.
     *
     * @param nbrElements nbr elements
     * @return split result
     */
    NProgressMonitor[] split(int nbrElements);

    /**
     * Split.
     *
     * @param weight weight
     * @return split result
     */
    NProgressMonitor[] split(double... weight);

    /**
     * Checks if is silent.
     *
     * @return is silent result
     */
    boolean isSilent();

    /**
     * Run with all.
     *
     * @param runnable runnable
     */
    void runWithAll(Runnable... runnable);

    /**
     * Run with all.
     *
     * @param runnable runnable
     * @param weights weights
     */
    void runWithAll(Runnable[] runnable, double[] weights);

    /**
     * Run with.
     *
     * @param runnable runnable
     */
    void runWith(Runnable runnable);

    /**
     * Call with.
     *
     * @param callable callable
     * @return call with result
     */
    <T> T callWith(NCallable<T> callable);
}
