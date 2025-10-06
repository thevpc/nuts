package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * Context provided to {@link NWorkBalancerStrategy} implementations when selecting workers.
 * <p>
 * This interface gives strategies access to runtime information about workers, their load,
 * call options, and user-defined variables. It allows strategies to both read metrics
 * and store temporary state relevant to the current load balancing decision.
 * </p>
 * <p>
 * Variables can be set globally using {@link #setVar(String, NElement)} or per worker
 * using {@link #setWorkerVar(String, String, NElement)}. These variables can influence
 * future strategy decisions within the same {@link NWorkBalancer} instance.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerStrategyContext {
    /**
     * Returns the list of available workers.
     *
     * @return list of {@link NWorkBalancerWorker} currently part of the call
     */
    List<NWorkBalancerWorker> getWorkers();

    /**
     * Returns the current load metrics of a specific worker.
     *
     * @param workerName the unique name of the worker
     * @return an optional containing the worker's load, or empty if not available
     */
    NOptional<NWorkBalancerWorkerLoad> getWorkerLoad(String workerName);


    /**
     * Returns the load metrics of all workers in a map keyed by worker name.
     *
     * @return map of worker names to their load metrics
     */
    Map<String, NWorkBalancerWorkerLoad> getWorkerLoads();

    /**
     * Returns a global option for the current call.
     *
     * @param name option key
     * @return an optional containing the value, or empty if not set
     */
    NOptional<NElement> getOption(String name);

    /**
     * Returns all global options for the current call.
     *
     * @return map of option names to values
     */
    Map<String, NElement> getOptions();


    /**
     * Returns a variable specific to a worker.
     *
     * @param workerName the worker's unique name
     * @param name variable name
     * @return an optional containing the variable value, or empty if not set
     */
    NOptional<NElement> getWorkerVar(String workerName, String name);

    /**
     * Returns a global variable.
     *
     * @param name variable name
     * @return an optional containing the variable value, or empty if not set
     */
    NOptional<NElement> getVar(String name);

    /**
     * Sets a variable specific to a worker.
     * <p>
     * These variables can be used to store strategy-specific state between job selections.
     * </p>
     *
     * @param workerName the worker's unique name
     * @param name variable name
     * @param value variable value
     * @return this context for fluent API
     */
    NWorkBalancerStrategyContext setWorkerVar(String workerName,String name, NElement value);


    /**
     * Sets a global variable for the current call.
     * <p>
     * These variables can influence the strategy's decision for future job selections.
     * </p>
     *
     * @param name variable name
     * @param value variable value
     * @return this context for fluent API
     */
    NWorkBalancerStrategyContext setVar(String name, NElement value);

}
