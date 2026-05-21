package net.thevpc.nuts.time;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 15 mai 2007 01:34:27
 */
public interface NProgressMonitor {
    double INDETERMINATE_PROGRESS = Double.NaN;

    static NOptional<NProgressMonitor> get() {
        return NProgressMonitors.of().currentMonitor();
    }

    static NProgressMonitor of() {
        NProgressMonitors monitors = NProgressMonitors.of();
        NOptional<NProgressMonitor> m = monitors.currentMonitor();
        if (m.isPresent()) {
            return m.get();
        }
        return monitors.ofSilent();
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
