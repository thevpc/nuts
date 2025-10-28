package net.thevpc.nuts.concurrent;

/**
 * Represents a storage backend for {@link NCachedValue} instances.
 * <p>
 * An {@code NCachedValueStore} provides persistent or shared storage
 * for cached values managed by {@link NCachedValueFactory} and {@link NCachedValue}.
 * Implementations may store cache metadata and values in memory,
 * files, databases, or distributed systems.
 * </p>
 *
 * <p>
 * This interface abstracts away how and where cached data is stored.
 * It allows different caching policies (e.g., local memory cache, shared cluster cache,
 * or disk-based persistence) to be plugged into the caching mechanism.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * NCachedValueStore store = ...;
 *
 * // Load cached model by id
 * NCachedValueModel model = store.load("user-session");
 *
 * // Update and persist cache state
 * model.setValue(newSessionData);
 * store.save(model);
 * }</pre>
 * </p>
 *
 * @see NCachedValue
 * @see NCachedValueModel
 * @see NCachedValueFactory
 * @since 0.8.0
 */
public interface NCachedValueStore {
    /**
     * Loads the cached value model for the given identifier.
     * <p>
     * If no cached entry exists for the given {@code id}, the method
     * may return {@code null} or an empty model, depending on the implementation.
     * </p>
     *
     * @param id the unique identifier of the cached value
     * @return the corresponding {@link NCachedValueModel}, or {@code null} if not found
     */
    NCachedValueModel load(String id);

    /**
     * Persists or updates the given cached value model.
     * <p>
     * Implementations decide how to serialize and store the model
     * (in-memory map, file system, database, etc.).
     * </p>
     *
     * @param model the model to store; must not be {@code null}
     */
    void save(NCachedValueModel model);
}
