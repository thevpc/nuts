package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.internal.rpi.NConcurrentRPI;

/**
 * Factory interface for creating {@link NRetryCall} instances.
 * <p>
 * Provides methods to configure and create retryable callables with optional persistent storage
 * support via {@link NRetryCallStore}.
 *
 * @since 0.8.7
 */
public interface NRetryCallFactory {
    /**
     * Creates a new default {@link NRetryCallFactory} instance.
     *
     * @return a new retry call factory instance
     */
    static NRetryCallFactory of() {
        return NConcurrentRPI.of().getRetryCallFactory();
    }

    static void configure(NRetryCallFactory global) {
        NConcurrentRPI.of().setRetryCallFactory(global);
    }

    static NRetryCallFactory ofMem() {
        return NConcurrentRPI.of().getMemoryRetryCallFactory();
    }

    static NRetryCallFactory ofDefault() {
        return NConcurrentRPI.of().getDefaultRetryCallFactory();
    }

    /**
     * Creates a new {@link NRetryCallFactory} using the provided store.
     *
     * @param store the retry call store to use
     * @return a new retry call factory instance
     */
    static NRetryCallFactory of(NRetryCallStore store) {
        return NConcurrentRPI.of().getRetryCallFactory().withStore(store);
    }

    /**
     * Returns the underlying store used to persist retry call models.
     *
     * @return the retry call store, may be {@code null} if not configured
     */
    NRetryCallStore store();

    /**
     * Returns a new factory instance using the provided store for persistence.
     *
     * @param store the store to use for retry call persistence
     * @return a new factory instance configured with the given store
     */
    NRetryCallFactory withStore(NRetryCallStore store);

    /**
     * Creates a new retryable call for the given callable using this factory.
     * <p>
     * The returned {@link NRetryCall} instance can be configured with retry policies,
     * recovery handlers, and executed synchronously or asynchronously.
     *
     * @param callable the callable to retry
     * @param <T>      the type of the result
     * @return a new {@link NRetryCall} instance
     */
    <T> NRetryCall<T> of(NCallable<T> callable);


    /**
     * Creates a new retryable call with a specific identifier.
     * <p>
     * The identifier can be used for tracking or persisting the retry call's state.
     *
     * @param id       the unique identifier for the retry call
     * @param callable the callable to retry
     * @param <T>      the type of the result
     * @return a new {@link NRetryCall} instance
     */
    <T> NRetryCall<T> of(String id, NCallable<T> callable);

}
