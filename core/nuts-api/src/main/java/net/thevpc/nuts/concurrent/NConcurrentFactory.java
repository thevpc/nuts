package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.function.Supplier;

public interface NConcurrentFactory extends NComponent {
    static NConcurrentFactory of() {
        return NExtensions.of(NConcurrentFactory.class);
    }

    <T> NCachedValue<T> cachedValue(Supplier<T> supplier);

    <T> NStableValue<T> stableValue(Supplier<T> supplier);

    <T> NRateLimitValueFactory rateLimitValueFactory();
    <T> NRateLimitValueFactory rateLimitValueFactory(NRateLimitValueStore store);
}
