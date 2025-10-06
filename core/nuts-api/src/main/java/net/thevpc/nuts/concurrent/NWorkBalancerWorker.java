package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * Represents a worker in a {@link NWorkBalancer}.
 * <p>
 * A worker is an execution target for jobs submitted to the work balancer.
 * Each worker may have a weight, custom options, and a host load metrics provider
 * that strategies can use to decide which worker to select.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerWorker {

    /**
     * Returns the unique name of this worker.
     * This name is used by strategies and running jobs to identify the worker.
     *
     * @return the unique worker name
     */
    String getName();

    /**
     * Returns the host load metrics provider for this worker.
     * Strategies may use these metrics to compute worker pressure
     * and make load-balancing decisions.
     *
     * @return the metrics provider, may be null if not provided
     */
    NWorkBalancerHostLoadMetricProvider getHostLoadMetricsProvider();

    /**
     * Returns the weight of this worker.
     * Weights can be used by strategies such as weighted round-robin
     * to favor more capable or faster workers.
     *
     * @return the worker weight, default is 1.0
     */
    float getWeight();

    /**
     * Returns the custom options associated with this worker.
     * Options can be used by strategies or jobs for custom behavior.
     *
     * @return an unmodifiable map of option names to values
     */
    Map<String, NElement> getOptions();

    /**
     * Returns the value of a specific option for this worker.
     *
     * @param name the option name
     * @return the option value, or empty if not present
     */
    NOptional<NElement> getOption(String name);
}
