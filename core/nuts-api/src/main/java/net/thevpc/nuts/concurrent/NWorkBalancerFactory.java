package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * Factory for creating and managing {@link NWorkBalancer} instances.
 * <p>
 * This factory provides methods to build new load-balanced calls, manage persistence,
 * integrate with a bean container, and register custom worker selection strategies.
 * </p>
 *
 * <p>
 * Users can register custom strategies via
 * {@link #defineStrategy(String, NWorkBalancerStrategy)} to extend or replace
 * the default balancing strategies. This allows defining specialized worker
 * selection logic beyond {@link NWorkBalancerDefaultStrategy}.
 * </p>
 *
 * <p>
 * Example usage of a custom strategy:
 * <pre>{@code
 * NWorkBalancerFactory factory = NWorkBalancerFactory.of();
 * factory.defineStrategy("myCustomStrategy", new NWorkBalancerStrategy() {
 *     @Override
 *     public void onStartCall(NWorkBalancerStrategyEvent event) { }
 *
 *     @Override
 *     public void onEndCall(NWorkBalancerStrategyEvent event) { }
 *
 *     @Override
 *     public String selectWorker(NWorkBalancerStrategyContext context) {
 *         // custom logic to choose a worker
 *         return context.getWorkers().get(0).getName();
 *     }
 * });
 * NWorkBalancer<String> balancer = factory.ofBuilder("call1")
 *                                       .setStrategy("myCustomStrategy")
 *                                       .build();
 * }</pre>
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerFactory {

    /**
     * Creates a new factory instance with default configuration.
     *
     * @return a new {@link NWorkBalancerFactory} instance
     */
    static NWorkBalancerFactory of() {
        return NConcurrent.of().workBalancerFactory();
    }

    /**
     * Creates a new factory instance using the provided store for persistence.
     *
     * @param store the {@link NWorkBalancerStore} for saving/loading balancer models
     * @return a new {@link NWorkBalancerFactory} instance
     */
    static NWorkBalancerFactory of(NWorkBalancerStore store) {
        return NConcurrent.of().workBalancerFactory().withStore(store);
    }

    /**
     * Creates a builder for a new {@link NWorkBalancer} with the given unique identifier.
     * <p>
     * The builder allows configuring workers, options, and the load balancing strategy.
     * Once built, the {@link NWorkBalancer} can be used to create {@link NCallable} instances
     * for executing jobs across the configured workers.
     * </p>
     *
     * @param id unique identifier for the balancer
     * @param <T> type of the result returned by jobs submitted to this balancer
     * @return a {@link NWorkBalancerBuilder} for configuring the balancer
     */
    <T> NWorkBalancerBuilder<T> ofBuilder(String id);

    /**
     * Returns a new {@link NWorkBalancerFactory} instance configured with the given store.
     * <p>
     * This factory is immutable; the original factory instance is not modified.
     * The store will be used for persisting {@link NWorkBalancerModel}s.
     * </p>
     *
     * @param store the store to use
     * @return a new {@link NWorkBalancerFactory} instance with the specified store
     */
    NWorkBalancerFactory withStore(NWorkBalancerStore store);

    /**
     * Returns a new {@link NWorkBalancerFactory} instance configured with the given bean container.
     * <p>
     * This factory is immutable; the original factory instance is not modified.
     * The bean container will be used to instantiate workers or strategies.
     * </p>
     *
     * @param beanContainer the bean container to use
     * @return a new {@link NWorkBalancerFactory} instance with the specified bean container
     */
    NWorkBalancerFactory withBeanContainer(NBeanContainer beanContainer);

    /**
     * Returns the current bean container.
     *
     * @return the {@link NBeanContainer} instance
     */
    NBeanContainer getBeanContainer();

    /**
     * Returns the current store used for persisting balancer models.
     *
     * @return the {@link NWorkBalancerStore} instance
     */
    NWorkBalancerStore getStore();

    /**
     * Returns a new {@link NWorkBalancerFactory} with the given {@link NWorkBalancerStrategy} registered under the specified name.
     * <p>
     * Once registered, the strategy can be referenced by name when building an {@link NWorkBalancer}:
     * <pre>{@code
     * NWorkBalancerFactory factory = NWorkBalancerFactory.of();
     * NWorkBalancerFactory updatedFactory = factory.defineStrategy("myStrategy", new MyCustomStrategy());
     * NWorkBalancer<MyResult> balancer = updatedFactory.ofBuilder("callId")
     *                                                  .setStrategy("myStrategy")
     *                                                  .build();
     * }</pre>
     * <p>
     * This allows extending the load balancer with custom worker selection
     * logic beyond the predefined strategies such as {@link NWorkBalancerDefaultStrategy#ROUND_ROBIN},
     * {@link NWorkBalancerDefaultStrategy#LEAST_LOAD}, or {@link NWorkBalancerDefaultStrategy#POWER_OF_TWO_CHOICES}.
     * </p>
     *
     * @param name     unique name of the strategy
     * @param strategy the custom strategy implementation
     * @return a new {@link NWorkBalancerFactory} instance including the registered strategy
     */
    NWorkBalancerFactory defineStrategy(String name, NWorkBalancerStrategy strategy);
}
