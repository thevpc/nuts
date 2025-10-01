package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public interface NCachedValueFactory {
    <T> NCachedValue<T> of(String id, Supplier<T> supplier);

    <T> NCachedValue<T> of(Supplier<T> supplier);
}
