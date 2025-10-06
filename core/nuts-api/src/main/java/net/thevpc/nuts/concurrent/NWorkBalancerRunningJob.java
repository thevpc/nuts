package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

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
    String getJobId();

    /**
     * Returns the human-readable name of this job.
     *
     * @return job name
     */
    String getJobName();

    /**
     * Returns the wall-clock start time of this job.
     *
     * @return start time as {@link Instant}
     */
    Instant getStartTime();

    /**
     * Returns the start time of the job in nanoseconds.
     * <p>
     * Useful for precise timing or duration calculations.
     *
     * @return start time in nanoseconds
     */
    long getStartTimeNano();

    /**
     * Returns the end time of the job in nanoseconds.
     * <p>
     * If the job is still running, this value may be 0 or undefined.
     *
     * @return end time in nanoseconds
     */
    long getEndTimeNano();

    /**
     * Returns the duration of the job so far.
     * <p>
     * If the job is still running, this returns the elapsed time since start.
     *
     * @return job duration
     */
    NDuration getDuration();

    /**
     * Returns the name of the worker executing this job.
     *
     * @return worker name
     */
    String getWorkerName();

    /**
     * Returns true if the job is currently running.
     *
     * @return {@code true} if running, {@code false} otherwise
     */
    boolean isRunning();

    /**
     * Returns any exception thrown during the job execution, if any.
     * <p>
     * If the job completed successfully, this returns {@code null}.
     *
     * @return throwable if job failed, or {@code null} if successful
     */
    Throwable getThrowable();
}
