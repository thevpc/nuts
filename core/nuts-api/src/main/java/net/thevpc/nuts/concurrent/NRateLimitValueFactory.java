package net.thevpc.nuts.concurrent;

/**
 * Factory interface for creating and configuring {@link NRateLimitValue} instances.
 * <p>
 * The factory provides methods to create builders for new rate-limited values,
 * configure a persistent store, and define custom rate-limit strategies.
 *
 * <p>Typical usage:</p>
 * <pre>
 * NRateLimitValue value = NRateLimitValueFactory.of()
 *      .ofBuilder("myRateLimit")
 *      .withLimit(10, NDuration.ofSeconds(1))
 *      .build();
 * </pre>
 *
 * @since 0.8.8
 */
public interface NRateLimitValueFactory {

    /**
     * Returns the default in-memory factory instance.
     *
     * @return default factory instance
     */
    static NRateLimitValueFactory of() {
        return NConcurrent.of().rateLimitValueFactory();
    }


    /**
     * Returns a factory instance that uses the given persistent store.
     *
     * @param store store to persist rate-limit values
     * @return factory instance using the provided store
     */
    static NRateLimitValueFactory of(NRateLimitValueStore store) {
        return NConcurrent.of().rateLimitValueFactory().withStore(store);
    }


    /**
     * Creates a new builder for a {@link NRateLimitValue} with the given identifier.
     * <p>
     * The returned builder allows defining one or more limit rules and building
     * the final {@link NRateLimitValue}.
     *
     * @param id unique name or identifier of the value
     * @return a new {@link NRateLimitValueBuilder} instance
     */
    NRateLimitValueBuilder ofBuilder(String id);

    /**
     * Sets a persistent store to be used by this factory.
     *
     * @param store store instance to use
     * @return this factory instance for chaining
     */
    NRateLimitValueFactory withStore(NRateLimitValueStore store);

    /**
     * Returns the currently configured store used for persisting rate-limit values.
     *
     * @return the store instance, may be {@code null} if none configured
     */
    NRateLimitValueStore getStore();

    /**
     * Defines a custom rate-limit strategy with the given name.
     * <p>
     * This allows registering new strategies that can be referenced by limits
     * when creating {@link NRateLimitValue} instances.
     *
     * @param name       unique strategy name
     * @param definition implementation of {@link NRateLimitStrategy}
     * @return this factory instance for chaining
     */
    NRateLimitValueFactory defineStrategy(String name, NRateLimitStrategy definition);
}
