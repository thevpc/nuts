package net.thevpc.nuts.concurrent;

/**
 * Storage interface for {@link NRateLimitValueModel} instances.
 * <p>
 * Provides basic persistence operations to load and save rate-limit
 * values, allowing rate-limit state to be maintained across JVM restarts
 * or shared between multiple nodes in a distributed system.
 *
 * @since 0.8.7
 */
public interface NRateLimitValueStore {

    /**
     * Loads a {@link NRateLimitValueModel} by its unique identifier.
     *
     * @param id the identifier of the rate-limit value
     * @return the loaded {@link NRateLimitValueModel}, or {@code null} if not found
     */
    NRateLimitValueModel load(String id);

    /**
     * Saves a {@link NRateLimitValueModel} instance.
     * <p>
     * This should persist the current state of the rate-limit value,
     * including its rules and last access time.
     *
     * @param model the rate-limit value model to save
     */
    void save(NRateLimitValueModel model);
}
