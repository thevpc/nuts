package net.thevpc.nuts.concurrent;

/**
 * Storage interface for stable values.
 * <p>
 * Provides methods to persist and retrieve {@link NStableValueModel} instances
 * by their unique identifier.
 *
 * @since 0.8.6
 */
public interface NStableValueStore {

    /**
     * Load a stable value model by its unique identifier.
     *
     * @param id the identifier of the stable value
     * @return the {@link NStableValueModel} corresponding to the given id,
     *         or {@code null} if not found
     */
    NStableValueModel load(String id);

    /**
     * Save a stable value model.
     * <p>
     * This may insert a new record or update an existing one in the underlying store.
     *
     * @param value the {@link NStableValueModel} to save
     */
    void save(NStableValueModel value);
}
