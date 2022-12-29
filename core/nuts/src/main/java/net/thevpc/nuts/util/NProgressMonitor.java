package net.thevpc.nuts.util;

import net.thevpc.nuts.NMsg;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 15 mai 2007 01:34:27
 */
public interface NProgressMonitor {
    double INDETERMINATE_PROGRESS = Double.NaN;

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

    String getId();

    String getName();

    NMsg getDescription();

    NProgressMonitor addListener(NProgressListener listener);

    NProgressMonitor removeListener(NProgressListener listener);

    NProgressListener[] getListeners();

    NDuration getDuration();

    NClock getStartClock();

    NProgressMonitor setMessage(NMsg message);

    NMsg getMessage();

    boolean isIndeterminate();

    double getProgress();

    NProgressMonitor setProgress(double progress);

    /**
     * [0..1]
     *
     * @param progress
     * @param message
     */
    NProgressMonitor setProgress(double progress, NMsg message);

    NProgressMonitor setIndeterminate();
    NProgressMonitor setIndeterminate(NMsg message);

    NProgressMonitor setProgress(long i, long max);

    NProgressMonitor setProgress(long i, long max, NMsg message);

    NProgressMonitor setProgress(long i, long maxi, long j, long maxj);
    NProgressMonitor setProgress(long i, long maxi, long j, long maxj, NMsg message);

    NProgressMonitor inc();

    NProgressMonitor inc(NMsg message);

    NDuration getEstimatedTotalDuration();

    NDuration getEstimatedRemainingDuration();

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
}
