package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NCallable;

/**
 * @since 0.8.7
 */
public interface NSagaFactory {
    NBeanContainer getBeanContainer();

    NSagaFactory withBeanContainer(NBeanContainer container);

    NSagaStore getStore();

    NSagaFactory withStore(NSagaStore store);

    NSagaBuilder of();

}
