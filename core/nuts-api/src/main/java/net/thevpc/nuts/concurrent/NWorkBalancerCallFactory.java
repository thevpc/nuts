package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * Factory for creating and managing {@link NWorkBalancerCall} instances.
 * Supports persistence, bean container integration, and strategy registration.
 *
 * @since 0.8.7
 */
public interface NWorkBalancerCallFactory {

    /**
     * Creates a new factory instance.
     */
    static NWorkBalancerCallFactory of() {
        return NConcurrent.of().workBalancerFactory();
    }

    static NWorkBalancerCallFactory of(NWorkBalancerCallStore store) {
        return NConcurrent.of().workBalancerFactory().withStore(store);
    }

    /**
     * Creates a builder for a new call with the given ID.
     *
     * @param id unique identifier for the call
     * @param <T> return type of the call
     */

    <T> NWorkBalancerCallBuilder<T> ofBuilder(String id);

    /**
     * Sets a store for persisting {@link NWorkBalancerModel}s.
     */
    NWorkBalancerCallFactory withStore(NWorkBalancerCallStore store);

    /**
     * Sets a bean container for instantiating workers or strategies.
     */
    NWorkBalancerCallFactory withBeanContainer(NBeanContainer beanContainer);

    NBeanContainer getBeanContainer();

    NWorkBalancerCallStore getStore();

    /**
     * Registers a custom strategy by name.
     */
    NWorkBalancerCallFactory defineStrategy(String name, NWorkBalancerStrategy strategy);
}
