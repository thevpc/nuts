package net.thevpc.nuts.concurrent;

import java.util.function.Supplier;

/**
 * Factory interface for creating {@link NOnceValue} instances.
 * <p>
 * A stable value represents a lazily computed value that can be cached
 * and optionally stored in a backing {@link NOnceValueStore} for reuse
 * across threads or executions. This factory provides methods to create
 * such stable values, either with or without a store.
 *
 * <p>Example usage:
 * <pre>{@code
 * NOnceValueFactory factory = NOnceValueFactory.of();
 * NOnceValue<Integer> stableInt = factory.of(() -> computeExpensiveValue());
 * }</pre>
 *
 * @since 0.8.6
 */
public interface NOnceValueFactory {
    /**
     * Creates a new {@link NOnceValueFactory} using the default store.
     *
     * @return a new stable value factory instance
     */
    static NOnceValueFactory of() {
        return NConcurrent.of().stableValueFactory();
    }

    /**
     * Creates a new {@link NOnceValueFactory} with a custom {@link NOnceValueStore}.
     *
     * @param store the backing store for stable values
     * @return a new stable value factory instance using the provided store
     */
    static NOnceValueFactory of(NOnceValueStore store) {
        return NConcurrent.of().stableValueFactory().withStore(store);
    }

    /**
     * Sets the backing {@link NOnceValueStore} for this factory.
     *
     * @param store the store to use
     * @return this factory instance for method chaining
     */
    NOnceValueFactory withStore(NOnceValueStore store);

    /**
     * Returns the backing store used by this factory.
     *
     * @return the stable value store
     */
    NOnceValueStore getStore();

    /**
     * Creates a new {@link NOnceValue} with the given supplier.
     *
     * @param supplier the supplier to compute the value
     * @param <T> the type of the value
     * @return a stable value instance
     */
    <T> NOnceValue<T> of(Supplier<T> supplier);

    /**
     * Creates a new {@link NOnceValue} with the given identifier and supplier.
     * <p>
     * The identifier can be used to persist or retrieve the value from the backing store.
     *
     * @param id the unique identifier for the stable value
     * @param supplier the supplier to compute the value
     * @param <T> the type of the value
     * @return a stable value instance
     */
    <T> NOnceValue<T> of(String id, Supplier<T> supplier);
}
