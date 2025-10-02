package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

import java.time.Instant;

/**
 * Represents a single execution of a worker within a {@link NWorkBalancerCall}.
 *
 * @param <T> the type returned by the worker
 *
 * @since 0.8.7
 */
public interface NWorkBalancerRunningJob<T> {
    /** The call this job belongs to */
    NWorkBalancerCall<T> callable();

    /** Unique identifier of this job */
    String jobId();

    /** Start time of the job */
    Instant startTime();

    /** Duration of the job so far */
    NDuration duration();

    /** Name of the worker executing this job */
    String workerName();
}
