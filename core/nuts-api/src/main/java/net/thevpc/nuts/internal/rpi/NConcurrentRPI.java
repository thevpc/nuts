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

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @app.category Base
 */
public interface NConcurrentRPI extends NComponent {
    static NConcurrentRPI of() {
        return NExtensions.of(NConcurrentRPI.class);
    }

    NTaskSet createTaskSet();

    // --------------------
    // Locks
    // --------------------

    /**
     * Sets the lock factory used by this component.
     *
     * @param lockFactory the factory to set
     */
    @NSetter
    void setCurrentLockFactory(NLockFactory lockFactory);

    /**
     * Returns a memory-only lock factory.
     *
     * @return memory lock factory
     */
    NLockFactory getMemoryLockFactory();

    /**
     * Returns the default lock factory.
     *
     * @return default lock factory
     */
    NLockFactory getDefaultLockFactory();

    /**
     * Returns the currently configured lock factory.
     *
     * @return lock factory
     */
    NLockFactory getCurrentLockFactory();

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
    void setCachedValueFactory(NCachedValueFactory cachedValueFactory);

    /**
     * Returns a memory-only cached value factory.
     *
     * @return memory cached value factory
     */
    NCachedValueFactory getMemoryCachedValueFactory();

    /**
     * Returns the default cached value factory.
     *
     * @return default cached value factory
     */
    NCachedValueFactory getDefaultCachedValueFactory();

    /**
     * Returns the currently configured cached value factory.
     *
     * @return cached value factory
     */
    NCachedValueFactory getCachedValueFactory();


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
    <T> NOnceValue<T> createOnceValue(Supplier<T> supplier);

    /**
     * Creates a once value with the given identifier and supplier.
     *
     * @param <T>      the type of the once value
     * @param id       identifier for the once value
     * @param supplier the supplier to produce the value
     * @return once value instance
     */
    <T> NOnceValue<T> createOnceValue(String id, Supplier<T> supplier);

    /**
     * Sets the once value factory used by this component.
     *
     * @param onceValueFactory the factory to set
     * @return this instance
     */
    @NSetter
    void setOnceValueFactory(NOnceValueFactory onceValueFactory);

    /**
     * Returns a memory-only once value factory.
     *
     * @return memory once value factory
     */
    NOnceValueFactory getMemoryOnceValueFactory();

    /**
     * Returns the default once value factory.
     *
     * @return default once value factory
     */
    NOnceValueFactory getDefaultOnceValueFactory();

    /**
     * Returns the currently configured once value factory.
     *
     * @return once value factory
     */
    NOnceValueFactory getOnceValueFactory();


    // --------------------
    // Rate Limiting
    // --------------------


    /**
     * Returns the default rate limit value factory.
     *
     * @return default rate limit value factory
     */
    NRateLimitValueFactory getDefaultRateLimitValueFactory();


    /**
     * Returns a memory-only rate limit value factory.
     *
     * @return memory rate limit value factory
     */
    NRateLimitValueFactory getMemoryRateLimitValueFactory();


    /**
     * Sets the rate limit value factory used by this component.
     *
     * @param factory the factory to set
     * @return this instance
     */
    @NSetter
    void setRateLimitValueFactory(NRateLimitValueFactory factory);


    /**
     * Returns the currently configured rate limit value factory.
     *
     * @return rate limit value factory
     */
    NWorkBalancerFactory getDefaultWorkBalancerFactory();

    /**
     * Returns the default work balancer factory.
     *
     * @return default work balancer factory
     */
    NWorkBalancerFactory getMemoryWorkBalancerFactory();

    /**
     * Returns the currently configured work balancer factory.
     *
     * @return work balancer factory
     */
    NWorkBalancerFactory getWorkBalancerFactory();


    /**
     * Rate limit value factory.
     *
     * @return rate limit value factory result
     */
    NRateLimitValueFactory getRateLimitValueFactory();


    /**
     * @since 0.8.7
     */
    <T> NRetryCall<T> createRetryCall(NCallable<T> callable);

    /**
     * @since 0.8.7
     */
    <T> NRetryCall<T> createRetryCall(String id, NCallable<T> callable);


    /**
     * @since 0.8.7
     */
    void setRetryCallFactory(NRetryCallFactory retryCallFactory);

    /**
     * @since 0.8.7
     */
    NRetryCallFactory getMemoryRetryCallFactory();

    /**
     * @since 0.8.7
     */
    NRetryCallFactory getDefaultRetryCallFactory();

    /**
     * @since 0.8.7
     */
    NRetryCallFactory getRetryCallFactory();


    // --------------------
    // Circuit Breaker
    // --------------------

    /**
     * @since 0.8.7
     */
    <T> NCircuitBreakerCall<T> createCircuitBreakerCall(NCallable<T> callable);

    /**
     * @since 0.8.7
     */
    <T> NCircuitBreakerCall<T> createCircuitBreakerCall(String id, NCallable<T> callable);


    /**
     * @since 0.8.7
     */
    @NSetter
    void setCircuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory);

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory getMemoryCircuitBreakerCallFactory();

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory getDefaultCircuitBreakerCallFactory();

    /**
     * @since 0.8.7
     */
    NCircuitBreakerCallFactory getCircuitBreakerCallFactory();


    // --------------------
    // Saga Calls
    // --------------------

    /**
     * @since 0.8.7
     */
    NSagaCallableFactory getDefaultSagaFactory();

    /**
     * @since 0.8.7
     */
    NSagaCallableFactory getSagaFactory();

    void setSagaFactory(NSagaCallableFactory factory);

    /**
     * @since 0.8.7
     */
    NSagaCallableFactory memorySagaFactory();

    /**
     * @since 0.8.7
     */
    NSagaCallableBuilder sagaCallBuilder();

    /**
     * @since 0.8.8
     */
    NSagaCallableBuilder sagaCallBuilder(String id);

    // --------------------
    // Work Balancer
    // --------------------

    /**
     * @since 0.8.7
     */
    @NSetter
    void setWorkBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory);

    // --------------------
    // Bulkhead Calls
    // --------------------

    /**
     * Default bulkhead call factory.
     *
     * @return default bulkhead call factory result
     */
    NBulkheadCallFactory getDefaultBulkheadCallFactory();

    /**
     * Memory bulkhead call factory.
     *
     * @return memory bulkhead call factory result
     */
    NBulkheadCallFactory getMemoryBulkheadCallFactory();

    /**
     * Bulkhead call factory.
     *
     * @return bulkhead call factory result
     */
    NBulkheadCallFactory getBulkheadCallFactory();

    /**
     * Bulkhead call factory.
     *
     * @param bulkheadCallFactory bulkhead call factory
     * @return bulkhead call factory result
     */
    @NSetter
    void setBulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory);


    // --------------------
    // Retry Call
    // --------------------

    /**
     * Retry fixed periods.
     *
     * @param periods ...periods
     * @return retry fixed periods result
     */
    NRetryPeriodFunction createRetryFixedPeriods(NDuration... periods);

    /**
     * Retry multiplied period.
     *
     * @param base       base
     * @param multiplier multiplier
     * @return retry multiplied period result
     */
    NRetryPeriodFunction createRetryMultipliedPeriod(NDuration base, double multiplier);

    NRetryPeriodFunction createRetryExponentialPeriod(NDuration base, double multiplier);


    // --------------------
    // Executor
    // --------------------

    /**
     * Returns the underlying {@link ExecutorService} used by this component
     * for asynchronous task execution.
     *
     * @return the executor service
     */
    ExecutorService getExecutorService();


    // --------------------
    // Sleep Utilities
    // --------------------
    /**
     * @since 0.8.7
     */
    void sleep(NDuration durationMillis) throws NInterruptedException;

    /**
     * @since 0.8.7
     */
    void sleep(Duration durationMillis) throws NInterruptedException;


    /**
     * handy sleep method that wraps InterruptedException into an unchecked exception
     * @param durationMillis durationMillis
     * @return {@code this}
     * @throws NInterruptedException when InterruptedException is thrown
     */
    void sleep(long durationMillis) throws NInterruptedException;

}
