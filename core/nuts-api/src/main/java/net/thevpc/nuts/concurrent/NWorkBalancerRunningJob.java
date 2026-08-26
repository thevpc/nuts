package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NGetter;

import java.time.Instant;

/**
 * Represents a single execution of a job by a worker within an {@link NWorkBalancer}.
 * <p>
 * This interface provides access to runtime metadata about a job execution,
 * including start and end times, duration, executing worker, and any thrown exceptions.
 * It can be used for monitoring, logging, or metrics aggregation.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerRunningJob {
    /**
     * Returns the unique identifier of this job execution.
     * <p>
     * This ID is generated for each invocation and can be used
     * to correlate metrics or trace logs.
     *
     * @return unique job ID
     */
    @NGetter
    String jobId();

    /**
     * Returns the human-readable name of this job.
     *
     * @return job name
     */
    @NGetter
    String jobName();

    /**
     * Returns the wall-clock start time of this job.
     *
     * @return start time as {@link Instant}
     */
    @NGetter
    Instant startTime();

    /**
     * Returns the start time of the job in nanoseconds.
     * <p>
     * Useful for precise timing or duration calculations.
     *
     * @return start time in nanoseconds
     */
    @NGetter
    long startTimeNano();

    /**
     * Returns the end time of the job in nanoseconds.
     * <p>
     * If the job is still running, this value may be 0 or undefined.
     *
     * @return end time in nanoseconds
     */
    @NGetter
    long endTimeNano();

    /**
     * Returns the duration of the job so far.
     * <p>
     * If the job is still running, this returns the elapsed time since start.
     *
     * @return job duration
     */
    @NGetter
    NDuration duration();

    /**
     * Returns the name of the worker executing this job.
     *
     * @return worker name
     */
    @NGetter
    String workerName();

    /**
     * Returns true if the job is currently running.
     *
     * @return {@code true} if running, {@code false} otherwise
     */
    @NGetter
    boolean isRunning();

    /**
     * Returns any exception thrown during the job execution, if any.
     * <p>
     * If the job completed successfully, this returns {@code null}.
     *
     * @return throwable if job failed, or {@code null} if successful
     */
    @NGetter
    Throwable error();
}
