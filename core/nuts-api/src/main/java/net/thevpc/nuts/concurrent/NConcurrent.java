package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * Main concurrency component interface providing factories and utilities
 * for locks, caching, rate-limiting, retrying, bulkhead, sagas, circuit breakers,
 * work balancing, and asynchronous task execution.
 *
 * @since 0.8.7
 */
public final class NConcurrent {
    private NConcurrent() {
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
    public static ExecutorService executorService() {
        return NConcurrentRPI.of().getExecutorService();
    }


    // --------------------
    // Sleep Utilities
    // --------------------

    /**
     * @since 0.8.7
     */
    public static void sleep(NDuration durationMillis) throws NInterruptedException {
        NConcurrentRPI.of().sleep(durationMillis);
    }

    /**
     * @since 0.8.7
     */
    public static void sleep(Duration durationMillis) throws NInterruptedException {
        NConcurrentRPI.of().sleep(durationMillis);
    }


    /**
     * handy sleep method that wraps InterruptedException into an unchecked exception
     *
     * @param durationMillis durationMillis
     * @throws NInterruptedException when InterruptedException is thrown
     */
    public static void sleep(long durationMillis) throws NInterruptedException {
        NConcurrentRPI.of().sleep(durationMillis);
    }

}
