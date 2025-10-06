package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * @since 0.8.7
 */
public interface NSagaCallableFactory {
    NBeanContainer getBeanContainer();

    NSagaCallableFactory withBeanContainer(NBeanContainer container);

    NSagaStore getStore();

    NSagaCallableFactory withStore(NSagaStore store);

    NSagaCallableBuilder ofBuilder();

}
