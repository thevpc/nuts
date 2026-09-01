package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NSetter;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
        return NConcurrentRPI.of().executorService();
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
