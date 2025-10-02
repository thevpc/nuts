package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;

import java.util.List;

/**
 * Represents a synchronous load-balanced call.
 * Executes work on multiple workers according to the chosen strategy.
 *
 * @param <T> the type returned by the call
 * @since 0.8.7
 */
public interface NWorkBalancerCall<T> extends NCallable<T> {

    /**
     * Returns all currently running jobs for this call.
     */
    List<NWorkBalancerRunningJob<T>> runningJobs();

    /**
     * Returns true if any jobs are currently running for this call.
     */
    boolean hasRunningJobs();

    /**
     * Returns the number of currently active jobs.
     */
    int runningJobsCount();
}
