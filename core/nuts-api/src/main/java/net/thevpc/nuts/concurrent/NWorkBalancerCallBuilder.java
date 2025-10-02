package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCallable;

/**
 * Builder for creating a {@link NWorkBalancerCall}.
 * Allows adding workers, setting options, and selecting a balancing strategy.
 *
 * @param <T> the type returned by the call
 * @since 0.8.7
 */
public interface NWorkBalancerCallBuilder<T> {
    /**
     * Adds a worker to this call.
     *
     * @param workerName unique name of the worker
     * @param callable the work to execute on this worker
     * @return a WorkerBuilder to configure worker-specific options
     */
    WorkerBuilder<T> addWorker(String workerName, NCallable<T> callable);

    /**
     * Removes a worker from this call.
     *
     * @param workerName name of the worker to remove
     * @return this builder
     */
    NWorkBalancerCallBuilder<T> remove(String workerName);

    /**
     * Sets the strategy to use by name.
     *
     * @param strategy strategy name (must be registered in the factory)
     * @return this builder
     */
    NWorkBalancerCallBuilder<T> setStrategy(String strategy);

    /**
     * Sets a default strategy implementation.
     *
     * @param strategy default strategy object
     * @return this builder
     */
    NWorkBalancerCallBuilder<T> setStrategy(NWorkBalancerDefaultStrategy strategy);


    /**
     * Sets a global option for this call.
     *
     * @param optionName option key
     * @param optionValue option value
     * @return this builder
     */
    NWorkBalancerCallBuilder<T> setOption(String optionName, NElement optionValue);

    /**
     * Worker-specific configuration builder.
     */
    interface WorkerBuilder<T>{
        NWorkBalancerCallBuilder<T> setOption(String workerName, String optionName, NElement optionValue);
        /**
         * Sets the host load provider for this worker.
         * This provider will be used by strategies to calculate worker load.
         *
         * @param workerName worker to attach the provider to
         * @param hostLoadProvider provider for metrics and host load
         * @return this builder
         */
        NWorkBalancerCallBuilder<T> setHostLoadProvider(String workerName, NWorkBalancerHostLoadProvider hostLoadProvider);

        /**
         * Returns to the main call builder for further configuration.
         *
         * @return main call builder
         */
        NWorkBalancerCallBuilder<T> then();
    }

    /**
     * Builds the configured {@link NWorkBalancerCall}.
     *
     * @return a new call
     */
    NWorkBalancerCall<T> build();
}
