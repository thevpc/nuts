package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

/**
 * Factory interface for creating {@link NStableValue} instances.
 * <p>
 * A stable value represents a lazily computed value that can be cached
 * and optionally stored in a backing {@link NStableValueStore} for reuse
 * across threads or executions. This factory provides methods to create
 * such stable values, either with or without a store.
 *
 * <p>Example usage:
 * <pre>{@code
 * NStableValueFactory factory = NStableValueFactory.of();
 * NStableValue<Integer> stableInt = factory.of(() -> computeExpensiveValue());
 * }</pre>
 *
 * @since 0.8.6
 */
public interface NStableValueFactory {
    /**
     * Creates a new {@link NStableValueFactory} using the default store.
     *
     * @return a new stable value factory instance
     */
    static NStableValueFactory of() {
        return NConcurrent.of().stableValueFactory();
    }

    /**
     * Creates a new {@link NStableValueFactory} with a custom {@link NStableValueStore}.
     *
     * @param store the backing store for stable values
     * @return a new stable value factory instance using the provided store
     */
    static NStableValueFactory of(NStableValueStore store) {
        return NConcurrent.of().stableValueFactory().withStore(store);
    }

    /**
     * Sets the backing {@link NStableValueStore} for this factory.
     *
     * @param store the store to use
     * @return this factory instance for method chaining
     */
    NStableValueFactory withStore(NStableValueStore store);

    /**
     * Returns the backing store used by this factory.
     *
     * @return the stable value store
     */
    NStableValueStore getStore();

    /**
     * Creates a new {@link NStableValue} with the given supplier.
     *
     * @param supplier the supplier to compute the value
     * @param <T> the type of the value
     * @return a stable value instance
     */
    <T> NStableValue<T> of(Supplier<T> supplier);

    /**
     * Creates a new {@link NStableValue} with the given identifier and supplier.
     * <p>
     * The identifier can be used to persist or retrieve the value from the backing store.
     *
     * @param id the unique identifier for the stable value
     * @param supplier the supplier to compute the value
     * @param <T> the type of the value
     * @return a stable value instance
     */
    <T> NStableValue<T> of(String id, Supplier<T> supplier);
}
