package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.function.Function;

public interface NWorkBalancerCallFactory {
    static NWorkBalancerCallFactory of() {
        return NConcurrent.of().workBalancerFactory();
    }

    static NWorkBalancerCallFactory of(NWorkBalancerCallStore store) {
        return NConcurrent.of().workBalancerFactory().withStore(store);
    }

    NWorkBalancerCallBuilder valueBuilder(String id);

    NWorkBalancerCallFactory withStore(NWorkBalancerCallStore store);

    NWorkBalancerCallFactory withBeanContainer(NBeanContainer beanContainer);

    NBeanContainer getBeanContainer();

    NWorkBalancerCallStore getStore();

    NWorkBalancerCallFactory defineStrategy(String name, NWorkBalancerCallStrategy definition);
}
