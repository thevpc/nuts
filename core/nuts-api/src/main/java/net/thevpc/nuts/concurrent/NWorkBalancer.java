package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * Represents a synchronous load-balanced call.
 * Executes work on multiple workers according to the chosen strategy.
 *
 * <p><b>Design and Usage Notes:</b></p>
 * <ul>
 *     <li>This interface provides a flexible mechanism for selecting workers based on
 *     real-time metrics, worker weights, and custom strategy logic.</li>
 *     <li><b>Worker selection</b> is fully controlled by the configured {@link NWorkBalancerStrategy}.
 *     Strategies receive all relevant job and worker information via {@link NWorkBalancerStrategyContext}.</li>
 *     <li><b>Queueing, throttling, and priority handling</b> are intentionally not part of the load balancer itself.
 *     These concerns should be handled upstream (before submitting the job) or via strategies
 *     using the available worker metrics and weights.</li>
 *     <li>Advanced features like max active jobs per worker or custom retry policies
 *     can be implemented outside the LB using {@link NRateLimitValue} or pre-processing layers.</li>
 *     <li>The design is intended to be extensible:
 *         <ul>
 *             <li>New strategies can be registered without modifying the core LB logic.</li>
 *             <li>Worker-specific configuration allows custom metrics and weights for intelligent scheduling.</li>
 *         </ul>
 *     </li>
 *     <li>Metrics are exposed via {@link NWorkBalancerWorkerLoad} and {@link NWorkBalancerHostLoadMetrics},
 *     enabling monitoring and observability of active and completed jobs per worker.</li>
 * </ul>
 *
 * @param <T> the type returned by the call
 * @since 0.8.7
 */
public interface NWorkBalancer<T> {


    /**
     * Returns a snapshot of all currently running jobs for this call.
     *
     * <p>Useful for monitoring, metrics aggregation, or custom cancellation logic.</p>
     *
     * @return a list of running jobs
     */
    List<NWorkBalancerRunningJob> getRunningJobs();

    /**
     * Checks whether any jobs are currently active for this call.
     *
     * @return {@code true} if at least one job is running, {@code false} otherwise
     */
    boolean hasRunningJobs();

    /**
     * Returns the number of currently active jobs for this call.
     *
     * <p>This count is computed atomically and can be used to measure system load
     * or for rate-limiting upstream job submissions.</p>
     *
     * @return the number of active jobs
     */
    int getRunningJobsCount();

    /**
     * Returns the list of workers currently registered in this balancer.
     *
     * <p>Worker information can be used by strategies, monitoring dashboards, or dynamic configuration.</p>
     *
     * @return list of workers
     */
    List<NWorkBalancerWorker> getWorkers();

    /**
     * Returns the load metrics and counters for a specific worker.
     *
     * <p>Useful for monitoring, observability, or strategy-based decisions outside the LB.</p>
     *
     * @param workerName the worker's unique name
     * @return optional worker load; empty if the worker does not exist
     */
    NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName);


    /**
     * Returns the load metrics and counters for all workers.
     *
     * <p>This provides a complete snapshot of worker activity for monitoring or reporting purposes.</p>
     *
     * @return a map of worker names to worker loads
     */
    Map<String, NWorkBalancerWorkerLoad> getWorkerLoads();

    /**
     * Creates a {@link NCallable} wrapping the given {@link NWorkBalancerJob}.
     * <p>
     * Note that calling this method does <b>not submit the job</b> immediately.
     * The returned {@link NCallable} can be invoked multiple times; each call:
     * <ul>
     *     <li>Selects a worker at execution time based on the configured strategy and current metrics.</li>
     *     <li>Tracks execution metrics independently (start/end time, success/failure, active jobs, etc.).</li>
     *     <li>Notifies the strategy via {@link NWorkBalancerStrategy#onStartCall(NWorkBalancerStrategyEvent)}
     *     and {@link NWorkBalancerStrategy#onEndCall(NWorkBalancerStrategyEvent)}.</li>
     * </ul>
     * <p>
     * This allows for retries, repeated executions, and benchmarking using the same job instance
     * while maintaining accurate load balancing and metrics tracking.
     *
     * @param name a descriptive name for the job
     * @param job the job to be executed
     * @return a {@link NCallable} that will execute the job when called
     */
    NCallable<T> of(String name, NWorkBalancerJob<T> job);

    /**
     * Returns the value of a global option configured on this balancer.
     *
     * <p>Options can be used by strategies or workers to customize behavior
     * at runtime without modifying core LB logic.</p>
     *
     * @param name option key
     * @return optional value of the option
     */
    NOptional<NElement> getOption(String name);

    /**
     * Returns all global options currently configured on this balancer.
     *
     * @return map of option names to values
     */
    Map<String, NElement> getOptions();
}
