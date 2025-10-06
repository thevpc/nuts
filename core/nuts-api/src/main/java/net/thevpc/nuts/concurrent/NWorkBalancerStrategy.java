package net.thevpc.nuts.concurrent;

/**
 * Strategy interface for selecting a worker in a {@link NWorkBalancer}.
 * <p>
 * Implementations of this interface define how jobs are assigned to workers
 * based on metrics, weights, or custom logic.
 * </p>
 * <p>
 * Strategies can also listen to job execution events via the {@link #onStartCall(NWorkBalancerStrategyEvent)}
 * and {@link #onEndCall(NWorkBalancerStrategyEvent)} methods. These methods have
 * default empty implementations, so they can be optionally overridden.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerStrategy {

    /**
     * Called when a job starts execution.
     * <p>
     * The default implementation does nothing. Override this method to track
     * metrics, logging, or trigger other side effects when a job starts.
     * </p>
     *
     * @param event details about the job and selected worker
     */
    default void onStartCall(NWorkBalancerStrategyEvent event){}

    /**
     * Called when a job ends execution.
     * <p>
     * The default implementation does nothing. Override this method to track
     * metrics, logging, or trigger other side effects when a job ends.
     * </p>
     *
     * @param event details about the job, selected worker, and any error thrown
     */
    default void onEndCall(NWorkBalancerStrategyEvent event){}

    /**
     * Selects a worker from the available workers provided in the context.
     * <p>
     * Implementations should return the <b>unique name</b> of the chosen worker
     * based on the strategy's logic, such as least load, round-robin, or a custom weighting.
     * </p>
     *
     * @param context provides access to worker metrics, options, and other runtime data
     * @return the <b>unique name</b> of the selected worker
     */
    String selectWorker(NWorkBalancerStrategyContext context);
}
