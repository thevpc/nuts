package net.thevpc.nuts.concurrent;

public interface NRateLimitValueFactory {
    static NRateLimitValueFactory of() {
        return NConcurrent.of().rateLimitValueFactory();
    }

    static NRateLimitValueFactory of(NRateLimitValueStore store) {
        return NConcurrent.of().rateLimitValueFactory().withStore(store);
    }

    NRateLimitValueBuilder ofBuilder(String id);

    NRateLimitValueFactory withStore(NRateLimitValueStore store);

    NRateLimitValueStore getStore();

    NRateLimitValueFactory defineStrategy(String name, NRateLimitStrategy definition);
}
