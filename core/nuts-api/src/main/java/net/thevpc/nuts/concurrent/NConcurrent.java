package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NCallable;

import java.util.concurrent.ExecutorService;
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

    NWorkBalancerCallFactory workBalancerFactory();
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
    NSagaCallFactory defaultSagaFactory();
    /**
     * @since 0.8.7
     */
    NSagaCallFactory sagaFactory();
    /**
     * @since 0.8.7
     */
    NSagaCallFactory memorySagaFactory();

    NSagaBuilder saga();
}
