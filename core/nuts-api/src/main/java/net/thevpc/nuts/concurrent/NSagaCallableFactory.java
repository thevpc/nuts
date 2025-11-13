package net.thevpc.nuts.concurrent;

/**
 * Factory interface for creating {@link NSagaCallable} instances and their builders.
 * <p>
 * A saga factory centralizes the creation of saga callables and manages the
 * {@link NSagaStore} used to persist or retrieve saga state. It provides a
 * consistent way to obtain new saga builders and configure the underlying store.
 */
public interface NSagaCallableFactory {

    /**
     * Returns the {@link NSagaStore} associated with this factory.
     * <p>
     * The store is used to persist saga progress, variables, or status, enabling
     * long-running or resumable saga workflows.
     *
     * @return the current saga store
     */
    NSagaStore getStore();

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

}
