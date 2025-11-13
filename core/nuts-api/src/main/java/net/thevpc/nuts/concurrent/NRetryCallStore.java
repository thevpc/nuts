package net.thevpc.nuts.concurrent;

/**
 * Store interface for persisting and retrieving {@link NRetryCallModel} instances.
 * <p>
 * This abstraction allows retry call state to be saved and restored, enabling
 * persistence across application restarts or distributed systems.
 *
 * @since 0.8.7
 */
public interface NRetryCallStore {

    /**
     * Loads a retry call model by its identifier.
     *
     * @param id the unique identifier of the retry call
     * @return the corresponding {@link NRetryCallModel}, or {@code null} if not found
     */
    NRetryCallModel load(String id);


    /**
     * Saves the given retry call model to the store.
     * <p>
     * This method should update the stored state if a model with the same ID already exists,
     * or create a new entry otherwise.
     *
     * @param value the {@link NRetryCallModel} to persist
     */
    void save(NRetryCallModel value);
}
