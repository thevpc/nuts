package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NToStringBuilder;

import java.util.Map;
import java.util.Objects;

/**
 * Event representing the execution lifecycle of a single job in a {@link NWorkBalancer}.
 * <p>
 * Instances of this class are passed to {@link NWorkBalancerStrategy#onStartCall(NWorkBalancerStrategyEvent)}
 * and {@link NWorkBalancerStrategy#onEndCall(NWorkBalancerStrategyEvent)} to allow strategies
 * to track job progress, update metrics, or implement custom logging/monitoring.
 * </p>
 * <p>
 * All fields are immutable. The {@code workerName} may be {@code null} in the start event
 * if no worker has been selected yet. The {@code endTimeNanos} is zero for start events
 * and set to the actual end time when the job finishes.
 * The {@code error} is non-null only if the job threw an exception during execution.
 * </p>
 *
 * @since 0.8.7
 */
public class NWorkBalancerStrategyEvent implements java.io.Serializable {
    /** Unique identifier of the job for this call */
    private final String jobId;

    /** Name of the job */
    private final String jobName;

    /** Name of the selected worker executing this job, may be null on start */
    private final String workerName;

    /** Start time of the job in nanoseconds, set when job starts */
    private final long startTimeNanos;

    /** End time of the job in nanoseconds, 0 if job has not ended */
    private final long endTimeNanos;

    /** Non-null if an error occurred during job execution */
    private final Throwable error;

    /**
     * Constructs a new strategy event.
     *
     * @param jobId unique job identifier
     * @param jobName job name
     * @param workerName selected worker name, may be null on start
     * @param startTimeNanos start time in nanoseconds
     * @param endTimeNanos end time in nanoseconds, 0 if not ended
     * @param error the thrown exception, or null if none occurred
     */
    public NWorkBalancerStrategyEvent(String jobId, String jobName, String workerName, long startTimeNanos, long endTimeNanos, Throwable error) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.workerName = workerName;
        this.startTimeNanos = startTimeNanos;
        this.endTimeNanos = endTimeNanos;
        this.error = error;
    }

    /** Returns the job name */
    @NGetter
    public String jobName() {
        return jobName;
    }

    /** Returns the job unique ID */
    @NGetter
    public String jobId() {
        return jobId;
    }

    /** Returns the selected worker name, may be null for start events */
    @NGetter
    public String workerName() {
        return workerName;
    }

    /** Returns the job start time in nanoseconds */
    @NGetter
    public long startTimeNanos() {
        return startTimeNanos;
    }

    /** Returns the job end time in nanoseconds, 0 if not finished */
    @NGetter
    public long endTimeNanos() {
        return endTimeNanos;
    }

    /** Returns the error thrown during execution, or null if none */
    @NGetter
    public Throwable error() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NWorkBalancerStrategyEvent that = (NWorkBalancerStrategyEvent) o;
        return startTimeNanos == that.startTimeNanos && endTimeNanos == that.endTimeNanos && Objects.equals(jobId, that.jobId) && Objects.equals(jobName, that.jobName) && Objects.equals(workerName, that.workerName) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, jobName, workerName, startTimeNanos, endTimeNanos, error);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("jobId", jobId)
                .add("jobName", jobName)
                .add("workerName", workerName)
                .add("startTimeNanos", startTimeNanos)
                .add("endTimeNanos", endTimeNanos)
                .add("error", error)
                .build();
    }

}
