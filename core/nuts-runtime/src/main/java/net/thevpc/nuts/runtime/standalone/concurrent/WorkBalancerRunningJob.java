package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerJob;
import net.thevpc.nuts.concurrent.NWorkBalancerRunningJob;
import net.thevpc.nuts.time.NDuration;

import java.time.Instant;

// --- Inner class for running job ---
class WorkBalancerRunningJob<T> implements NWorkBalancerRunningJob {
    private final NWorkBalancerJob<T> job;
    private final String jobName;
    private final String jobId;
    private final String workerName;
    private final long startNano;
    private long endNano;
    private Throwable throwable;

    public WorkBalancerRunningJob(NWorkBalancerJob<T> job, String jobId, String jobName,String workerName, long startNano) {
        this.job = job;
        this.jobId = jobId;
        this.workerName = workerName;
        this.startNano = startNano;
        this.jobName = jobName;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    @Override
    public Instant getStartTime() {
        return Instant.ofEpochMilli(startNano / 1_000_000);
    }

    @Override
    public long getStartTimeNano() {
        return startNano;
    }

    @Override
    public long getEndTimeNano() {
        return endNano;
    }

    @Override
    public NDuration getDuration() {
        long now = (endNano > 0 ? endNano : System.nanoTime()) - startNano;
        return NDuration.ofNanos(now);
    }

    @Override
    public String getWorkerName() {
        return workerName;
    }

    public void setEndNano(long endNano) {
        this.endNano = endNano;
    }

    public WorkBalancerRunningJob<T> setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public boolean isRunning() {
        return endNano == 0;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
