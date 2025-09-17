package net.thevpc.nuts.concurrent;

import java.util.function.Function;

public interface NRateLimitedValueFactory {
    static NRateLimitedValueFactory of() {
        return NConcurrentFactory.of().rateLimitedValueFactory();
    }

    static NRateLimitedValueFactory of(NRateLimitedValueStore store) {
        return NConcurrentFactory.of().rateLimitedValueFactory(store);
    }

    NRateLimitedValueBuilder value(String id);

    NRateLimitedValueFactory defineStrategy(String name, Function<NRateLimitStrategyModel, NRateLimitStrategy> definition);
}
