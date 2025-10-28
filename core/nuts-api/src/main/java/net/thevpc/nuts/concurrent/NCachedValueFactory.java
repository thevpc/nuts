package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

/**
 * Factory interface for creating {@link NCachedValue} instances.
 * <p>
 * A {@code NCachedValueFactory} is responsible for producing cached value wrappers
 * that lazily evaluate and store a value produced by a {@link Supplier}.
 * Implementations may support various caching strategies (e.g., memory-based,
 * time-based, or thread-local caching).
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * NCachedValueFactory factory = ...;
 * NCachedValue<String> cached = factory.of("config-value", () -> loadConfig());
 *
 * // The supplier is called only once until cache invalidation
 * String v1 = cached.get();
 * String v2 = cached.get(); // returns cached result
 * }</pre>
 * </p>
 *
 * @see NCachedValue
 * @since 0.8.6
 */
public interface NCachedValueFactory {
    /**
     * Creates a named cached value for the given supplier.
     * <p>
     * The {@code id} can be used by the factory implementation to identify,
     * track, or share cache entries across the application.
     * </p>
     *
     * @param id       unique identifier for the cached value
     * @param supplier the supplier that computes the value when not cached
     * @param <T>      type of the cached value
     * @return a new {@link NCachedValue} that manages caching for the supplier
     */
    <T> NCachedValue<T> of(String id, Supplier<T> supplier);

    /**
     * Creates an unnamed cached value for the given supplier.
     * <p>
     * This variant does not associate an identifier with the cached entry.
     * It is typically used for private or isolated caching cases.
     * </p>
     *
     * @param supplier the supplier that computes the value when not cached
     * @param <T>      type of the cached value
     * @return a new {@link NCachedValue} that manages caching for the supplier
     */
    <T> NCachedValue<T> of(Supplier<T> supplier);
}
