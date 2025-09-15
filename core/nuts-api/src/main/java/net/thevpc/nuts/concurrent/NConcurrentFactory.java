package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.function.Supplier;

public interface NConcurrentFactory extends NComponent {
    static NConcurrentFactory of() {
        return NExtensions.of(NConcurrentFactory.class);
    }

    <T> NCachedValue<T> createCachedValue(Supplier<T> supplier);

    <T> NStableValue<T> createStableValue(Supplier<T> supplier);
}
