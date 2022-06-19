package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsMessage;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 15 mai 2007 01:34:27
 */
public interface NutsProgressMonitor {
    double INDETERMINATE_PROGRESS = Double.NaN;

    NutsProgressMonitor start();

    NutsProgressMonitor start(NutsMessage message);

    NutsProgressMonitor complete();

    NutsProgressMonitor complete(NutsMessage message);

    NutsProgressMonitor undoComplete();

    NutsProgressMonitor undoComplete(NutsMessage message);

    NutsProgressMonitor cancel();
    NutsProgressMonitor undoCancel();

    NutsProgressMonitor cancel(NutsMessage message);

    NutsProgressMonitor undoCancel(NutsMessage message);

    NutsProgressMonitor undoSuspend();

    NutsProgressMonitor undoSuspend(NutsMessage message);

    NutsProgressMonitor suspend();

    NutsProgressMonitor suspend(NutsMessage message);

    boolean isSuspended();

    boolean isCompleted();

    boolean isBlocked();

    NutsProgressMonitor block();

    NutsProgressMonitor block(NutsMessage message);

    NutsProgressMonitor undoBlock();

    NutsProgressMonitor undoBlock(NutsMessage message);

    boolean isStarted();

    boolean isCanceled();

    void reset();

    String getId();

    String getName();

    NutsMessage getDescription();

    NutsProgressMonitor addListener(NutsProgressListener listener);

    NutsProgressMonitor removeListener(NutsProgressListener listener);

    NutsProgressListener[] getListeners();

    NutsDuration getDuration();

    NutsClock getStartClock();

    NutsProgressMonitor setMessage(NutsMessage message);

    NutsMessage getMessage();

    boolean isIndeterminate();

    double getProgress();

    NutsProgressMonitor setProgress(double progress);

    /**
     * [0..1]
     *
     * @param progress
     * @param message
     */
    NutsProgressMonitor setProgress(double progress, NutsMessage message);

    NutsProgressMonitor setIndeterminate();
    NutsProgressMonitor setIndeterminate(NutsMessage message);

    NutsProgressMonitor setProgress(long i, long max);

    NutsProgressMonitor setProgress(long i, long max, NutsMessage message);

    NutsProgressMonitor setProgress(long i, long maxi, long j, long maxj);
    NutsProgressMonitor setProgress(long i, long maxi, long j, long maxj, NutsMessage message);

    NutsProgressMonitor inc();

    NutsProgressMonitor inc(NutsMessage message);

    NutsDuration getEstimatedTotalDuration();

    NutsDuration getEstimatedRemainingDuration();

    NutsProgressMonitor translate(long index, long max);

    NutsProgressMonitor translate(long i, long imax, long j, long jmax);

    NutsProgressMonitor stepInto(NutsMessage message);

    NutsProgressMonitor stepInto(long index, long max);

    NutsProgressMonitor temporize(long freq);

    NutsProgressMonitor incremental(long iterations);

    NutsProgressMonitor incremental(double delta);

    NutsProgressMonitor translate(double factor, double start);

    NutsProgressMonitor[] split(int nbrElements);

    NutsProgressMonitor[] split(double... weight);

    boolean isSilent();
}
