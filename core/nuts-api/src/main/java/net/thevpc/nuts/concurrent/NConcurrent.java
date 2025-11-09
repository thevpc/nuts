package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @since 0.8.7
 */
public interface NConcurrent extends NComponent {
    static NConcurrent of() {
        return NExtensions.of(NConcurrent.class);
    }

    ExecutorService executorService();

    <T> NCachedValue<T> cachedValue(Supplier<T> supplier);

    <T> NCachedValue<T> cachedValue(String id, Supplier<T> supplier);


    NConcurrent setCachedValueFactory(NCachedValueFactory cachedValueFactory);

    NCachedValueFactory memoryCachedValueFactory();

    NCachedValueFactory defaultCachedValueFactory();

    NCachedValueFactory cachedValueFactory();

    <T> NStableValue<T> stableValue(Supplier<T> supplier);

    <T> NStableValue<T> stableValue(String id, Supplier<T> supplier);

    NConcurrent setStableValueFactory(NStableValueFactory StableValueFactory);

    NStableValueFactory memoryStableValueFactory();

    NStableValueFactory defaultStableValueFactory();

    NStableValueFactory stableValueFactory();

    NRateLimitValueFactory defaultRateLimitValueFactory();

    NRateLimitValueFactory memoryRateLimitValueFactory();

    NConcurrent setRateLimitValueFactory(NRateLimitValueFactory factory);

    NWorkBalancerFactory defaultWorkBalancerFactory();

    NWorkBalancerFactory memoryWorkBalancerFactory();

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

    /**
     * @since 0.8.7
     */
    NConcurrent setWorkBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory);

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

    NTaskSet taskSet();

    IntFunction<NDuration> retryFixedPeriod(NDuration period);
    IntFunction<NDuration> retryFixedPeriods(NDuration ...periods);
    IntFunction<NDuration> retryMultipliedPeriod(NDuration base, double multiplier);


    NBulkheadCallFactory defaultBulkheadCallFactory();

    NBulkheadCallFactory memoryBulkheadCallFactory();

    NBulkheadCallFactory bulkheadCallFactory();

    NConcurrent setBulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory);
}
