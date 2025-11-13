package net.thevpc.nuts.concurrent;

/**
 * Factory interface for creating and configuring {@link NBulkheadCall} instances.
 * <p>
 * A bulkhead call wraps a {@link NCallable} with concurrency control, ensuring that
 * only a limited number of concurrent calls (per bulkhead ID) are allowed to execute.
 * This is part of the bulkhead pattern for fault isolation and system resilience.
 *
 * <p>Instances can be obtained using the static factory methods {@link #of()} or {@link #of(NBulkheadCallStore)}.
 * Implementations typically rely on {@link NBulkheadCallBackend} to enforce concurrency limits
 * and on {@link NBulkheadCallStore} to persist related state or metrics.
 *
 * <p>Example usage:
 * <pre>{@code
 * NBulkheadCallFactory factory = NBulkheadCallFactory.of();
 *
 * NBulkheadCall<String> call = factory.bulkheadCall("userService", () -> {
 *     return userService.fetchUserData();
 * });
 *
 * String result = call.execute();
 * }</pre>
 *
 * @since 0.8.8
 */
public interface NBulkheadCallFactory {
    /**
     * Creates a default {@link NBulkheadCallFactory} instance using the global
     * {@link NConcurrent} configuration.
     *
     * @return a new {@link NBulkheadCallFactory} instance
     */
    static NBulkheadCallFactory of() {
        return NConcurrent.of().bulkheadCallFactory();
    }

    /**
     * Creates a {@link NBulkheadCallFactory} bound to a specific {@link NBulkheadCallStore}.
     *
     * @param store the store used to persist bulkhead call information
     * @return a new {@link NBulkheadCallFactory} configured with the given store
     */
    static NBulkheadCallFactory of(NBulkheadCallStore store) {
        return NConcurrent.of().bulkheadCallFactory().withStore(store);
    }

    /**
     * Wraps a callable with bulkhead protection, automatically assigning an internal bulkhead ID.
     *
     * @param callable the callable task to execute under bulkhead control
     * @param <T> the type of the callable result
     * @return a new {@link NBulkheadCall} instance wrapping the given callable
     */
    <T> NBulkheadCall<T> bulkheadCall(NCallable<T> callable);

    /**
     * Wraps a callable with bulkhead protection, using a specific bulkhead identifier.
     * Calls sharing the same ID are subject to the same concurrency limits.
     *
     * @param id the bulkhead identifier
     * @param callable the callable task to execute under bulkhead control
     * @param <T> the type of the callable result
     * @return a new {@link NBulkheadCall} instance bound to the given ID
     */
    <T> NBulkheadCall<T> bulkheadCall(String id, NCallable<T> callable);

    /**
     * Returns a new factory instance bound to the specified {@link NBulkheadCallStore}.
     *
     * @param store the store used to persist bulkhead call state
     * @return a factory instance using the given store
     */
    NBulkheadCallFactory withStore(NBulkheadCallStore store);

    /**
     * Returns a new factory instance using the specified {@link NBulkheadCallBackend}.
     * <p>
     * The backend enforces concurrency limits and manages permit acquisition.
     *
     * @param backend the backend responsible for enforcing concurrency limits
     * @return a factory instance using the given backend
     */
    NBulkheadCallFactory withBackend(NBulkheadCallBackend backend);


    /**
     * Returns the currently configured {@link NBulkheadCallStore}.
     *
     * @return the configured store
     */
    NBulkheadCallStore getStore();


    /**
     * Returns the currently configured {@link NBulkheadCallBackend}.
     *
     * @return the configured backend
     */
    NBulkheadCallBackend getBackend();
}
