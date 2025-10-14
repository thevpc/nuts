package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;

/**
 * Builder for creating a {@link NWorkBalancer}.
 * Allows adding workers, setting options, and selecting a balancing strategy.
 *
 * @param <T> the type returned by the call
 * @since 0.8.7
 */
public interface NWorkBalancerBuilder<T> {
    /**
     * Adds a worker to this call.
     *
     * @param workerName unique name of the worker
     * @return a WorkerBuilder to configure worker-specific options
     */
    WorkerBuilder<T> addWorker(String workerName);

    /**
     * Removes a worker from this call.
     *
     * @param workerName name of the worker to remove
     * @return this builder
     */
    NWorkBalancerBuilder<T> remove(String workerName);

    /**
     * Sets the strategy to use by name.
     *
     * @param strategy strategy name (must be registered in the factory)
     * @return this builder
     */
    NWorkBalancerBuilder<T> setStrategy(String strategy);

    /**
     * Sets a default strategy implementation.
     *
     * @param strategy default strategy object
     * @return this builder
     */
    NWorkBalancerBuilder<T> setStrategy(NWorkBalancerDefaultStrategy strategy);


    /**
     * Sets a global option for this call.
     *
     * @param optionName  option key
     * @param optionValue option value
     * @return this builder
     */
    NWorkBalancerBuilder<T> setOption(String optionName, NElement optionValue);

    /**
     * Worker-specific configuration builder.
     */
    interface WorkerBuilder<T> {
        WorkerBuilder<T> withOption(String optionName, NElement optionValue);

        WorkerBuilder<T> withWeight(float weight);

        /**
         * Sets the host load provider for this worker.
         * This provider will be used by strategies to calculate worker load.
         *
         * @param hostLoadMetricsProvider provider for metrics and host load
         * @return this builder
         */
        WorkerBuilder<T> withHostLoadMetricsProvider(NWorkBalancerHostLoadMetricProvider hostLoadMetricsProvider);

        /**
         * Returns to the main call builder for further configuration.
         *
         * @return main call builder
         */
        NWorkBalancerBuilder<T> then();
        WorkerBuilder<T> addWorker(String workerName);

        NWorkBalancer<T> build();
    }

    /**
     * Builds the configured {@link NWorkBalancer}.
     *
     * @return a new call
     */
    NWorkBalancer<T> build();
}
