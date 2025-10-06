package net.thevpc.nuts.concurrent;

/**
 * Represents a unit of work that can be executed by a worker
 * within an {@link NWorkBalancer}.
 * <p>
 * Implementations of this interface contain the logic of the job to
 * perform. Each job receives a {@link NWorkBalancerJobContext} providing
 * information about the job itself, the worker, and any relevant options.
 * </p>
 * <p>
 * Jobs should be stateless or handle their own internal state safely,
 * because the same job instance may be wrapped multiple times as an
 * {@link NCallable} using {@link NWorkBalancer#of(String, NWorkBalancerJob)}
 * and executed multiple times.
 * </p>
 *
 * @param <T> the type of the result returned by the job
 * @since 0.8.7
 */
public interface NWorkBalancerJob<T> {
    /**
     * Executes the job using the provided {@link NWorkBalancerJobContext}.
     * <p>
     * This method is called by the {@link NWorkBalancer} when the job
     * is executed. The context provides:
     * <ul>
     *     <li>Job identifier and name</li>
     *     <li>Assigned worker information</li>
     *     <li>Access to global and worker-specific options</li>
     * </ul>
     * </p>
     * <p>
     * Any exception thrown by this method will be captured by the
     * {@link NWorkBalancer} and reported in the strategy events.
     * </p>
     *
     * @param context provides job and worker details, as well as options
     * @return the result of the job execution
     * @throws RuntimeException if the job fails during execution
     */
    T call(NWorkBalancerJobContext context);
}
