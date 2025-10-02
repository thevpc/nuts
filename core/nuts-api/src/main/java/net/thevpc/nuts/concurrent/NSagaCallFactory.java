package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

/**
 * @since 0.8.7
 */
public interface NSagaCallFactory {
    NBeanContainer getBeanContainer();

    NSagaCallFactory withBeanContainer(NBeanContainer container);

    NSagaCallStore getStore();

    NSagaCallFactory withStore(NSagaCallStore store);

    NSagaCallBuilder ofBuilder();

}
