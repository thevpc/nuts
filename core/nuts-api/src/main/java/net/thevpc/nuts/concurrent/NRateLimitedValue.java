package net.thevpc.nuts.concurrent;

import java.time.Duration;

public interface NRateLimitedValue {
    /**
     * creates a builder based on the default (in memory) NRateLimitedValueFactory.
     * should call build() to build a NRateLimitedValue instance
     * @param name value name/identifier
     * @return new NRateLimitedValueBuilder instance,
     */
    static NRateLimitedValueBuilder ofBuilder(String name) {
        return NRateLimitedValueFactory.of().value(name);
    }

    NRateLimitedValueResult take();

    NRateLimitedValueResult take(int count);

    NRateLimitedValueResult take(Runnable runnable);

    NRateLimitedValueResult take(int count, Runnable runnable);

    NRateLimitedValueResult claim(int count);

    NRateLimitedValueResult claim(int count, Duration timeout);

    NRateLimitedValueResult claim(Runnable runnable);

    NRateLimitedValueResult claim(int count, Runnable runnable);

    NRateLimitedValueResult claim(Duration timeout, Runnable runnable);

    /**
     * Attempts to claim the specified number of permits from this rate limiter,
     * waiting up to the given timeout if necessary for permits to become available.
     * <p>
     * If enough permits are available immediately, they are consumed and the result
     * is returned at once. Otherwise, the current thread will block until either
     * the requested number of permits become available or the timeout expires.
     * <p>
     * This method is interruptible and may return early if the thread is interrupted.
     *
     * @param count the number of permits to claim; must be positive
     * @param timeout the maximum time to wait for permits to become available;
     *                a non-null, non-negative duration
     * @param runnable an optional action to run if the claim is successful.
     *                 The runnable is executed in the calling thread after permits
     *                 have been acquired. May be {@code null} if no action is needed.
     * @return a result object describing whether the claim was successful and
     *         any metadata (e.g., remaining permits, wait time)
     */
    NRateLimitedValueResult claim(int count, Duration timeout, Runnable runnable);

}
