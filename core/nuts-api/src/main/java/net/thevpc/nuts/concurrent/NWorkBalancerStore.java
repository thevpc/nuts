package net.thevpc.nuts.concurrent;

/**
 * Interface for persisting and retrieving {@link NWorkBalancerModel} instances.
 * <p>
 * Implementations can store models in memory, a database, or any external storage.
 * This allows load balancers to be recreated or shared across different contexts or application restarts.
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerStore {
    /**
     * Loads a {@link NWorkBalancerModel} by its unique identifier.
     * <p>
     * Returns the stored model or {@code null} if no model exists with the given ID.
     *
     * @param id unique identifier of the work balancer model
     * @return the stored {@link NWorkBalancerModel}, or {@code null} if not found
     */
    NWorkBalancerModel load(String id);

    /**
     * Saves the given {@link NWorkBalancerModel}.
     * <p>
     * This method persists the current state of the model so it can be retrieved later.
     * Implementations may overwrite existing models with the same ID.
     *
     * @param model the work balancer model to save
     */
    void save(NWorkBalancerModel model);
}
