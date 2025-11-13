package net.thevpc.nuts.concurrent;

/**
 * Defines a persistence mechanism for saga instances.
 * <p>
 * Implementations of this interface are responsible for storing and retrieving
 * the state of a saga, represented by {@link NSagaModel}. This allows sagas
 * to survive application restarts or be shared across nodes in a distributed system.
 *
 * @since 0.8.7
 */
public interface NSagaStore {
    /**
     * Loads a saga instance by its unique identifier.
     *
     * @param id the unique identifier of the saga
     * @return the {@link NSagaModel} corresponding to the given ID,
     *         or {@code null} if no saga is found
     */
    NSagaModel load(String id);


    /**
     * Saves or updates a saga instance.
     * <p>
     * This method should persist all necessary information, including the
     * saga context, node tree, and execution state.
     *
     * @param value the {@link NSagaModel} to persist
     */
    void save(NSagaModel value);
}
