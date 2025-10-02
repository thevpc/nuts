package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * @since 0.8.7
 */
public interface NWorkBalancerWorkerLoad {
    /**
     *  A normalized indicator (0.0–1.0) of the host's overall load,
     *  as reported by the runtime or OS. Represents the capacity pressure
     *  on the machine hosting this worker.
     * @return the host load factor, or empty if not available
     */
    NOptional<Float> hostLoadFactor();

    /**
     * Returns the CPU load of the host (normalized 0.0–1.0).
     *
     * @return CPU load, or empty if not available
     */
    NOptional<Float> hostCpuLoad();


    /**
     * Returns the memory load of the host (normalized 0.0–1.0).
     *
     * @return memory load, or empty if not available
     */
    NOptional<Float> hostMemoryLoad();


    /**
     * Returns an estimate of the host latency in milliseconds.
     *
     * @return host latency, or empty if not available
     */
    NOptional<Long> hostLatency();

    /**
     * Returns a map of additional host metrics. Can include custom metrics
     * relevant to load balancing (e.g., disk I/O, network utilization).
     *
     * @return map of metric names to values
     */
    Map<String, NElement> hostMetrics();

    /**
     * Timestamp (nanoTime) when the host load factor was last measured or updated.
     * Useful to assess freshness of the metric.
     */
    long hostLoadLastUpdateNano();

    void refreshHostLoad();


    /**
     * number of currently active calls on that worker (atomic snapshot).
     *
     * @return
     */
    long activeJobsCount();

    /**
     * total number of job starts (monotonic counter).
     *
     * @return
     */
    long totalJobsCount();

    default long completedJobsCount() {
        return succeededJobCount() + failedJobsCount();
    }

    /**
     * monotonic counters
     *
     * @return
     */
    long succeededJobCount();

    /**
     * monotonic counter for failed jobs
     *
     * @return
     */
    long failedJobsCount();

    /**
     * Total CPU or wall-clock time (nano) currently consumed by all active jobs
     * on this worker. Allows estimating average remaining execution time.
     */
    long activeJobsTotalDurationNano();

    /**
     * Total CPU or wall-clock time (nano) spent by all completed jobs.
     * Use to compute average completed-job latency or throughput.
     */
    long completedJobsTotalDurationNano();
}
