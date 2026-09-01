/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NSetter;

import java.util.function.Supplier;

/**
 * @app.category Base
 */
public interface NConcurrentRPI extends NComponent {
    static NConcurrentRPI of() {
        return NExtensions.of(NConcurrentRPI.class);
    }

    NTaskSet taskSet();

    // --------------------
    // Cached Values
    // --------------------

    /**
     * Creates a cached value with the given supplier using the default cache.
     *
     * @param <T>      the type of the cached value
     * @param supplier the supplier to produce the value
     * @return a cached value instance
     */
    <T> NCachedValue<T> cachedValue(Supplier<T> supplier);

    /**
     * Creates a cached value with the given identifier and supplier.
     *
     * @param <T>      the type of the cached value
     * @param id       the identifier for this cached value
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
    @NSetter
    void cachedValueFactory(NCachedValueFactory cachedValueFactory);

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
    // once values
    // --------------------

    /**
     * Creates a once value using the default once value factory.
     *
     * @param <T>      the type of the once value
     * @param supplier the supplier to produce the value
     * @return once value instance
     */
    <T> NOnceValue<T> onceValue(Supplier<T> supplier);

    /**
     * Creates a once value with the given identifier and supplier.
     *
     * @param <T>      the type of the once value
     * @param id       identifier for the once value
     * @param supplier the supplier to produce the value
     * @return once value instance
     */
    <T> NOnceValue<T> onceValue(String id, Supplier<T> supplier);

    /**
     * Sets the once value factory used by this component.
     *
     * @param onceValueFactory the factory to set
     * @return this instance
     */
    @NSetter
    void onceValueFactory(NOnceValueFactory onceValueFactory);

    /**
     * Returns a memory-only once value factory.
     *
     * @return memory once value factory
     */
    NOnceValueFactory memoryOnceValueFactory();

    /**
     * Returns the default once value factory.
     *
     * @return default once value factory
     */
    NOnceValueFactory defaultOnceValueFactory();

    /**
     * Returns the currently configured once value factory.
     *
     * @return once value factory
     */
    NOnceValueFactory onceValueFactory();


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
    @NSetter
    void rateLimitValueFactory(NRateLimitValueFactory factory);


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


    /**
     * Rate limit value factory.
     *
     * @return rate limit value factory result
     */
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
    void retryCallFactory(NRetryCallFactory retryCallFactory);

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
    @NSetter
    void circuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory);

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

    void sagaFactory(NSagaCallableFactory factory);

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
    @NSetter
    void workBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory);

    // --------------------
    // Bulkhead Calls
    // --------------------

    /**
     * Default bulkhead call factory.
     *
     * @return default bulkhead call factory result
     */
    NBulkheadCallFactory defaultBulkheadCallFactory();

    /**
     * Memory bulkhead call factory.
     *
     * @return memory bulkhead call factory result
     */
    NBulkheadCallFactory memoryBulkheadCallFactory();

    /**
     * Bulkhead call factory.
     *
     * @return bulkhead call factory result
     */
    NBulkheadCallFactory bulkheadCallFactory();

    /**
     * Bulkhead call factory.
     *
     * @param bulkheadCallFactory bulkhead call factory
     * @return bulkhead call factory result
     */
    @NSetter
    void bulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory);


    // --------------------
    // Retry Call
    // --------------------

    /**
     * Retry fixed periods.
     *
     * @param periods ...periods
     * @return retry fixed periods result
     */
    NRetryPeriodFunction retryFixedPeriods(NDuration... periods);

    /**
     * Retry multiplied period.
     *
     * @param base       base
     * @param multiplier multiplier
     * @return retry multiplied period result
     */
    NRetryPeriodFunction retryMultipliedPeriod(NDuration base, double multiplier);

    NRetryPeriodFunction retryExponentialPeriod(NDuration base, double multiplier);


}
