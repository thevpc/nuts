package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Main concurrency component interface providing factories and utilities
 * for caching, rate-limiting, retrying, bulkhead, sagas, circuit breakers,
 * work balancing, and asynchronous task execution.
 *
 * @since 0.8.7
 */
public interface NConcurrent extends NComponent {

    /**
     * Returns the default {@link NConcurrent} instance.
     *
     * @return the NConcurrent instance
     */
    static NConcurrent of() {
        return NExtensions.of(NConcurrent.class);
    }

    // --------------------
    // Executor
    // --------------------

    /**
     * Returns the underlying {@link ExecutorService} used by this component
     * for asynchronous task execution.
     *
     * @return the executor service
     */
    ExecutorService executorService();

    // --------------------
    // Cached Values
    // --------------------

    /**
     * Creates a cached value with the given supplier using the default cache.
     *
     * @param <T> the type of the cached value
     * @param supplier the supplier to produce the value
     * @return a cached value instance
     */
    <T> NCachedValue<T> cachedValue(Supplier<T> supplier);

    /**
     * Creates a cached value with the given identifier and supplier.
     *
     * @param <T> the type of the cached value
     * @param id the identifier for this cached value
     * @param supplier the supplier to produce the value
     * @return a cached value instance
     */
    <T> NCachedValue<T> cachedValue(String id, Supplier<T> supplier);

    /**
     * Sets the cached value factory used by this component.
     *
     * @param cachedValueFactory the factory to set
     * @return this instance
     */
    NConcurrent setCachedValueFactory(NCachedValueFactory cachedValueFactory);

    /**
     * Returns a memory-only cached value factory.
     *
     * @return memory cached value factory
     */
    NCachedValueFactory memoryCachedValueFactory();

    /**
     * Returns the default cached value factory.
     *
     * @return default cached value factory
     */
    NCachedValueFactory defaultCachedValueFactory();

    /**
     * Returns the currently configured cached value factory.
     *
     * @return cached value factory
     */
    NCachedValueFactory cachedValueFactory();


    // --------------------
    // Stable Values
    // --------------------

    /**
     * Creates a stable value using the default stable value factory.
     *
     * @param <T> the type of the stable value
     * @param supplier the supplier to produce the value
     * @return stable value instance
     */
    <T> NStableValue<T> stableValue(Supplier<T> supplier);

    /**
     * Creates a stable value with the given identifier and supplier.
     *
     * @param <T> the type of the stable value
     * @param id identifier for the stable value
     * @param supplier the supplier to produce the value
     * @return stable value instance
     */
    <T> NStableValue<T> stableValue(String id, Supplier<T> supplier);

    /**
     * Sets the stable value factory used by this component.
     *
     * @param stableValueFactory the factory to set
     * @return this instance
     */
    NConcurrent setStableValueFactory(NStableValueFactory stableValueFactory);

    /**
     * Returns a memory-only stable value factory.
     *
     * @return memory stable value factory
     */
    NStableValueFactory memoryStableValueFactory();

    /**
     * Returns the default stable value factory.
     *
     * @return default stable value factory
     */
    NStableValueFactory defaultStableValueFactory();

    /**
     * Returns the currently configured stable value factory.
     *
     * @return stable value factory
     */
    NStableValueFactory stableValueFactory();


    // --------------------
    // Rate Limiting
    // --------------------


    /**
     * Returns the default rate limit value factory.
     *
     * @return default rate limit value factory
     */
    NRateLimitValueFactory defaultRateLimitValueFactory();


    /**
     * Returns a memory-only rate limit value factory.
     *
     * @return memory rate limit value factory
     */
    NRateLimitValueFactory memoryRateLimitValueFactory();


    /**
     * Sets the rate limit value factory used by this component.
     *
     * @param factory the factory to set
     * @return this instance
     */
    NConcurrent setRateLimitValueFactory(NRateLimitValueFactory factory);


    /**
     * Returns the currently configured rate limit value factory.
     *
     * @return rate limit value factory
     */
    NWorkBalancerFactory defaultWorkBalancerFactory();

    /**
     * Returns the default work balancer factory.
     *
     * @return default work balancer factory
     */
    NWorkBalancerFactory memoryWorkBalancerFactory();

    /**
     * Returns the currently configured work balancer factory.
     *
     * @return work balancer factory
     */
    NWorkBalancerFactory workBalancerFactory();


    NRateLimitValueFactory rateLimitValueFactory();


    /**
     * @since 0.8.7
     */
    <T> NRetryCall<T> retryCall(NCallable<T> callable);

    /**
     * @since 0.8.7
     */
    <T> NRetryCall<T> retryCall(String id, NCallable<T> callable);


    /**
     * @since 0.8.7
     */
    NConcurrent setRetryCallFactory(NRetryCallFactory retryCallFactory);

    /**
     * @since 0.8.7
     */
    NRetryCallFactory memoryRetryCallFactory();

    /**
     * @since 0.8.7
     */
    NRetryCallFactory defaultRetryCallFactory();

    /**
     * @since 0.8.7
     */
    NRetryCallFactory retryCallFactory();


    // --------------------
    // Circuit Breaker
    // --------------------
    /**
     * @since 0.8.7
     */
    <T> NCircuitBreakerCall<T> circuitBreakerCall(NCallable<T> callable);

    /**
     * @since 0.8.7
     */
    <T> NCircuitBreakerCall<T> circuitBreakerCall(String id, NCallable<T> callable);


    /**
     * @since 0.8.7
     */
    NConcurrent setCircuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory);

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory memoryCircuitBreakerCallFactory();

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory defaultCircuitBreakerCallFactory();

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory circuitBreakerCallFactory();


    // --------------------
    // Saga Calls
    // --------------------
    /**
     * @since 0.8.7
     */
    NSagaCallableFactory defaultSagaFactory();

    /**
     * @since 0.8.7
     */
    NSagaCallableFactory sagaFactory();

    /**
     * @since 0.8.7
     */
    NSagaCallableFactory memorySagaFactory();

    /**
     * @since 0.8.7
     */
    NSagaCallableBuilder sagaCallBuilder();

    // --------------------
    // Work Balancer
    // --------------------

    /**
     * @since 0.8.7
     */
    NConcurrent setWorkBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory);

    // --------------------
    // Sleep Utilities
    // --------------------
    /**
     * @since 0.8.7
     */
    NConcurrent sleep(NDuration durationMillis) throws NInterruptedException;

    /**
     * @since 0.8.7
     */
    NConcurrent sleep(Duration durationMillis) throws NInterruptedException;


        /**
         * handy sleep method that wraps InterruptedException into an unchecked exception
         * @param durationMillis durationMillis
         * @return {@code this}
         * @throws NInterruptedException when InterruptedException is thrown
         */
    NConcurrent sleep(long durationMillis) throws NInterruptedException;

    // --------------------
    // NTaskSet
    // --------------------

    NTaskSet taskSet();

    // --------------------
    // Retry Call
    // --------------------
    IntFunction<NDuration> retryFixedPeriod(NDuration period);
    IntFunction<NDuration> retryFixedPeriods(NDuration ...periods);
    IntFunction<NDuration> retryMultipliedPeriod(NDuration base, double multiplier);


    // --------------------
    // Bulkhead Calls
    // --------------------
    NBulkheadCallFactory defaultBulkheadCallFactory();

    NBulkheadCallFactory memoryBulkheadCallFactory();

    NBulkheadCallFactory bulkheadCallFactory();

    NConcurrent setBulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory);
}
