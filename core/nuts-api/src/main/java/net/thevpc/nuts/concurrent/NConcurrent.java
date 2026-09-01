package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NSetter;

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


}
