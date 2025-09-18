package net.thevpc.nuts.concurrent;

import java.util.function.Function;

public interface NRateLimitValueFactory {
    static NRateLimitValueFactory of() {
        return NConcurrentFactory.of().rateLimitValueFactory();
    }

    static NRateLimitValueFactory of(NRateLimitValueStore store) {
        return NConcurrentFactory.of().rateLimitValueFactory(store);
    }

    NRateLimitValueBuilder valueBuilder(String id);

    NRateLimitValueFactory defineStrategy(String name, Function<NRateLimitRuleModel, NRateLimitRule> definition);
}
