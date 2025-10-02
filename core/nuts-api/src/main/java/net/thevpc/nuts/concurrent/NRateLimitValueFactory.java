package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.function.Function;

public interface NRateLimitValueFactory {
    static NRateLimitValueFactory of() {
        return NConcurrent.of().rateLimitValueFactory();
    }

    static NRateLimitValueFactory of(NRateLimitValueStore store) {
        return NConcurrent.of().rateLimitValueFactory().withStore(store);
    }

    NRateLimitValueBuilder ofBuilder(String id);

    NRateLimitValueFactory withStore(NRateLimitValueStore store);

    NRateLimitValueFactory withBeanContainer(NBeanContainer beanContainer);

    NBeanContainer getBeanContainer();

    NRateLimitValueStore getStore();

    NRateLimitValueFactory defineStrategy(String name, NRateLimitStrategy definition);
}
