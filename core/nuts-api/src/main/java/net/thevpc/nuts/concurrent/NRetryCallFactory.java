package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * @since 0.8.7
 */
public interface NRetryCallFactory {
    NBeanContainer getBeanContainer();

    NRetryCallFactory withBeanContainer(NBeanContainer container);

    NRetryCallStore getStore();

    NRetryCallFactory withStore(NRetryCallStore store);

    <T> NRetryCall<T> of(NCallable<T> callable);

    <T> NRetryCall<T> of(String id, NCallable<T> callable);

}
