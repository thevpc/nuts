package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * @since 0.8.7
 */
public interface NCircuitBreakerCallFactory {
    NBeanContainer getBeanContainer();

    NCircuitBreakerCallFactory withBeanContainer(NBeanContainer container);

    NCircuitBreakerCallStore getStore();

    NCircuitBreakerCallFactory withStore(NCircuitBreakerCallStore store);

    <T> NCircuitBreakerCall<T> of(NCallable<T> callable);

    <T> NCircuitBreakerCall<T> of(String id, NCallable<T> callable);

}
