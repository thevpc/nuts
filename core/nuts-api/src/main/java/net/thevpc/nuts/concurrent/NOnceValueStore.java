package net.thevpc.nuts.concurrent;

/**
 * Storage interface for once values.
 * <p>
 * Provides methods to persist and retrieve {@link NOnceValueModel} instances
 * by their unique identifier.
 *
 * @since 0.8.6
 */
public interface NOnceValueStore {

    /**
     * Load a once value model by its unique identifier.
     *
     * @param id the identifier of the once value
     * @return the {@link NOnceValueModel} corresponding to the given id,
     *         or {@code null} if not found
     */
    NOnceValueModel load(String id);

    /**
     * Save a once value model.
     * <p>
     * This may insert a new record or update an existing one in the underlying store.
     *
     * @param value the {@link NOnceValueModel} to save
     */
    void save(NOnceValueModel value);
}
