package net.thevpc.nuts.concurrent;

/**
 * Provides host load and runtime metrics for a worker.
 * <p>
 * Implementations of this interface supply the {@link NWorkBalancerHostLoadMetrics}
 * that are used by load balancing strategies to determine the current load,
 * capacity pressure, and responsiveness of a worker.
 * </p>
 * <p>
 * The returned metrics may include CPU load, memory usage, latency, or
 * any custom metrics that influence worker selection in a strategy such as
 * {@link NWorkBalancerDefaultStrategy#LEAST_LOAD} or a custom strategy.
 * </p>
 * <p>
 * A typical implementation collects real-time data from the host system or
 * from the worker itself. This allows the {@link NWorkBalancer} to make
 * informed decisions when selecting the best worker for executing a job.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * NWorkBalancerHostLoadMetricProvider provider = () -> {
 *     NWorkBalancerHostLoadMetrics metrics = new NWorkBalancerHostLoadMetrics();
 *     metrics.setHostCpuLoad(getCpuLoad());
 *     metrics.setResolveHostMemoryLoad(getMemoryLoad());
 *     metrics.setHostLatency(measureLatency());
 *     return metrics;
 * };
 * }</pre>
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerHostLoadMetricProvider {

    /**
     * Resolves the default host load metrics for this worker.
     * <p>
     * The returned metrics are used by the load balancer strategy to
     * select workers based on load, capacity, or custom criteria.
     * Implementations may return dynamic, real-time metrics or cached values.
     * </p>
     *
     * @return an instance of {@link NWorkBalancerHostLoadMetrics} representing
     *         the current load of the worker
     */
    NWorkBalancerHostLoadMetrics resolveDefaultMetrics();
}
