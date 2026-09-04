package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.internal.rpi.NConcurrentRPI;

/**
 * Factory interface for creating {@link NSagaCallable} instances and their builders.
 * <p>
 * A saga factory centralizes the creation of saga callables and manages the
 * {@link NSagaStore} used to persist or retrieve saga state. It provides a
 * consistent way to obtain new saga builders and configure the underlying store.
 */
public interface NSagaCallableFactory {

    /**
     * Creates a new default {@link NSagaCallableFactory} instance.
     *
     * @return a new saga callable factory instance
     */
    static NSagaCallableFactory of() {
        return NConcurrentRPI.of().getSagaFactory();
    }

    /**
     * Creates a new {@link NSagaCallableFactory} using the provided store.
     *
     * @param store the saga store to use
     * @return a new saga callable factory instance
     */
    static NSagaCallableFactory of(NSagaStore store) {
        return NConcurrentRPI.of().getSagaFactory().withStore(store);
    }

    static NSagaCallableFactory ofDefault() {
        return NConcurrentRPI.of().getDefaultSagaFactory();
    }

    static NSagaCallableFactory ofMem() {
        return NConcurrentRPI.of().memorySagaFactory();
    }

    static void configure(NSagaCallableFactory factory) {
        NConcurrentRPI.of().setSagaFactory(factory);
    }

    /**
     * Returns the {@link NSagaStore} associated with this factory.
     * <p>
     * The store is used to persist saga progress, variables, or status, enabling
     * long-running or resumable saga workflows.
     *
     * @return the current saga store
     */
    NSagaStore store();

    /**
     * Returns a new factory instance that uses the provided {@link NSagaStore}.
     * <p>
     * This allows saga callables created from the factory to use a different
     * persistence backend without affecting the original factory.
     *
     * @param store the new saga store to use
     * @return a new {@link NSagaCallableFactory} instance configured with the given store
     */
    NSagaCallableFactory withStore(NSagaStore store);

    /**
     * Returns a new {@link NSagaCallableBuilder} instance for defining saga workflows.
     * <p>
     * The builder returned by this method can be used to construct complex saga callables
     * with sequential steps, conditional branches, and loops.
     *
     * @return a new saga callable builder
     */
    NSagaCallableBuilder ofBuilder();

    /**
     * Returns a new {@link NSagaCallableBuilder} instance configured with the specified saga ID.
     *
     * @param id saga identifier
     * @return a new saga callable builder
     * @since 0.8.8
     */
    NSagaCallableBuilder ofBuilder(String id);

    /**
     * Loads a saga model by its unique identifier from the configured store.
     *
     * @param id saga identifier
     * @return loaded saga model, or null if not found or store is null
     * @since 0.8.8
     */
    default NSagaModel load(String id) {
        return store() == null ? null : store().load(id);
    }

    /**
     * Deletes a saga record by its unique identifier from the configured store.
     *
     * @param id saga identifier
     * @return true if deleted, false otherwise
     * @since 0.8.8
     */
    default boolean delete(String id) {
        return store() != null && store().delete(id);
    }

}
