package net.thevpc.nuts.concurrent;

/**
 * Storage interface for {@link NBulkheadCallModel} instances.
 * <p>
 * A bulkhead call store provides persistent or transient access to the
 * configuration and state of bulkhead calls, allowing them to be loaded,
 * saved, and managed by {@link NBulkheadCallFactory} implementations.
 *
 * @since 0.8.8
 */
public interface NBulkheadCallStore {

    /**
     * Loads the {@link NBulkheadCallModel} for the given identifier.
     *
     * @param id identifier of the bulkhead call
     * @return the loaded model, or {@code null} if no such model exists
     */
    NBulkheadCallModel load(String id);


    /**
     * Saves the given {@link NBulkheadCallModel}.
     * Implementations may persist or cache the model depending on the backend strategy.
     *
     * @param model the model to save
     */
    void save(NBulkheadCallModel model);
}
