package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * Factory interface for creating {@link NCircuitBreakerCall} instances.
 * <p>
 * This factory provides a centralized way to configure and create
 * circuit-breaker calls within the Nuts concurrency framework. It
 * manages:
 * </p>
 * <ul>
 *     <li>Bean resolution via {@link NBeanContainer}</li>
 *     <li>Persistence or state tracking via {@link NCircuitBreakerCallStore}</li>
 *     <li>Creation of new {@link NCircuitBreakerCall} instances with or without explicit identifiers</li>
 * </ul>
 *
 * <p>
 * Typical usage involves configuring the factory with a bean container
 * and store, then using {@link #of(NCallable)} or {@link #of(String, NCallable)}
 * to produce resilient callable tasks.
 * </p>
 *
 * <pre>{@code
 * NCircuitBreakerCallFactory factory = ...;
 * factory = factory.withBeanContainer(beanContainer)
 *                  .withStore(circuitBreakerStore);
 *
 * NCircuitBreakerCall<String> task = factory.of("taskId", () -> fetchRemoteData());
 * }</pre>
 *
 * <p>
 * The factory is designed to support dependency injection, persistence,
 * and consistent configuration across multiple circuit-breaker calls.
 * </p>
 *
 * @since 0.8.7
 */
public interface NCircuitBreakerCallFactory {

    /**
     * Returns the bean container used by this factory for dependency resolution.
     *
     * @return the bean container
     */
    NBeanContainer getBeanContainer();


    /**
     * Returns a new factory instance using the given bean container.
     *
     * @param container the bean container to use
     * @return a new factory configured with the given container
     */
    NCircuitBreakerCallFactory withBeanContainer(NBeanContainer container);

    /**
     * Returns the store used by this factory to persist or track circuit-breaker states.
     *
     * @return the circuit-breaker call store
     */
    NCircuitBreakerCallStore getStore();


    /**
     * Returns a new factory instance using the given circuit-breaker store.
     *
     * @param store the store to use
     * @return a new factory configured with the given store
     */
    NCircuitBreakerCallFactory withStore(NCircuitBreakerCallStore store);

    /**
     * Creates a new circuit-breaker call wrapping the given callable.
     *
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@link NCircuitBreakerCall} instance
     */
    <T> NCircuitBreakerCall<T> of(NCallable<T> callable);

    /**
     * Creates a new circuit-breaker call with a specific identifier.
     *
     * @param id an identifier for this circuit-breaker call
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@link NCircuitBreakerCall} instance
     */
    <T> NCircuitBreakerCall<T> of(String id, NCallable<T> callable);

}
