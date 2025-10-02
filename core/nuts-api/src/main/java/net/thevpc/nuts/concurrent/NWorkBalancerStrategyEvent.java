package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public class NWorkBalancerStrategyEvent implements java.io.Serializable {
    private final String jobId;          // unique per call
    private final String workerName;      // selected worker name (may be null on start)
    private final long startTimeNanos;    // set on start
    private final long endTimeNanos;      // set on end, 0 if not ended
    private final Throwable error;        // non-null if error occurred

    public NWorkBalancerStrategyEvent(String jobId, String workerName, long startTimeNanos, long endTimeNanos, Throwable error) {
        this.jobId = jobId;
        this.workerName = workerName;
        this.startTimeNanos = startTimeNanos;
        this.endTimeNanos = endTimeNanos;
        this.error = error;
    }

    public String getJobId() {
        return jobId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public long getEndTimeNanos() {
        return endTimeNanos;
    }

    public Throwable getError() {
        return error;
    }
}
