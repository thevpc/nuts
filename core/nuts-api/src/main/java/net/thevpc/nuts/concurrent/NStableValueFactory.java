package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public interface NStableValueFactory {
    static NStableValueFactory of() {
        return NConcurrent.of().stableValueFactory();
    }

    static NStableValueFactory of(NStableValueStore store) {
        return NConcurrent.of().stableValueFactory().withStore(store);
    }

    NStableValueFactory withStore(NStableValueStore store);

    NStableValueFactory withBeanContainer(NBeanContainer beanContainer);

    NBeanContainer getBeanContainer();

    NStableValueStore getStore();

    <T> NStableValue<T> of(Supplier<T> supplier);
    <T> NStableValue<T> of(String id, Supplier<T> supplier);
}
