package net.thevpc.nuts.concurrent;

/**
 * Represents the runtime load and metrics of a {@link NWorkBalancerWorker}.
 * <p>
 * All values reflect the state of the worker at a point in time, or are monotonic counters.
 * Useful for strategies to make informed decisions about worker selection.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerWorkerLoad {
    /**
     * Returns cached host load metrics for this worker.
     * <p>
     * Metrics typically include CPU, memory, or other system-level indicators,
     * normalized between 0.0 and 1.0. May be empty if metrics are unavailable.
     * </p>
     *
     * @return the cached host load metrics, or empty if not available
     */
    NCachedValue<NWorkBalancerHostLoadMetrics> hostLoadMetrics();

    /**
     * Returns the number of currently active jobs on this worker.
     * This is an atomic snapshot.
     *
     * @return the count of active jobs
     */
    long activeJobsCount();

    /**
     * Returns the total number of job starts on this worker since initialization.
     * This is a monotonic counter.
     *
     * @return total jobs started
     */
    long totalJobsCount();

    /**
     * Returns the total number of completed jobs (both succeeded and failed).
     *
     * @return completed jobs count
     */
    default long completedJobsCount() {
        return succeededJobCount() + failedJobsCount();
    }

    /**
     * Returns the total number of succeeded jobs.
     * This is a monotonic counter.
     *
     * @return succeeded jobs count
     */
    long succeededJobCount();

    /**
     * Returns the total number of failed jobs.
     * This is a monotonic counter.
     *
     * @return failed jobs count
     */
    long failedJobsCount();

    /**
     * Returns the total CPU or wall-clock time (in nanoseconds) currently consumed
     * by all active jobs on this worker.
     * <p>
     * Useful for estimating current load and predicting completion time.
     * </p>
     *
     * @return total active job duration in nanoseconds
     */
    long activeJobsTotalDurationNano();

    /**
     * Returns the total CPU or wall-clock time (in nanoseconds) spent by all completed jobs.
     * <p>
     * Can be used to compute average completed-job latency or throughput.
     * </p>
     *
     * @return total duration of completed jobs in nanoseconds
     */
    long completedJobsTotalDurationNano();
}
