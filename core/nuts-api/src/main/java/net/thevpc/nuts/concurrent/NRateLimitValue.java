package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.util.NCallable;

import java.time.Duration;

public interface NRateLimitValue extends NElementDescribable {
    /**
     * creates a builder based on the default (in memory) NRateLimitValueFactory.
     * should call build() to build a NRateLimitValue instance
     *
     * @param name value name/identifier
     * @return new NRateLimitValueBuilder instance,
     */
    static NRateLimitValueBuilder ofBuilder(String name) {
        return NRateLimitValueFactory.of().valueBuilder(name);
    }

    NRateLimitValueResult take();

    NRateLimitValueResult take(int count);

    NRateLimitValueResult takeAndRun(Runnable runnable);

    NRateLimitValueResult takeAndRun(int count, Runnable runnable);

    <T> NRateLimitValueResult takeAndCall(NCallable<T> callable);

    <T> NRateLimitValueResult takeAndCall(int count, NCallable<T> callable);

    NRateLimitValueResult claim(int count);

    NRateLimitValueResult claim(int count, Duration timeout);

    NRateLimitValueResult claimAndRun(Runnable runnable);

    NRateLimitValueResult claimAndRun(int count, Runnable runnable);

    NRateLimitValueResult claimAndRun(Duration timeout, Runnable runnable);

    <T> NRateLimitValueResult claimAndCall(int count, NCallable<T> callable);

    <T> NRateLimitValueResult claimAndCall(Duration timeout, NCallable<T> callable);

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
     * @param count    the number of permits to claim; must be positive
     * @param timeout  the maximum time to wait for permits to become available;
     *                 a non-null, non-negative duration
     * @param runnable an optional action to run if the claim is successful.
     *                 The runnable is executed in the calling thread after permits
     *                 have been acquired. May be {@code null} if no action is needed.
     * @return a result object describing whether the claim was successful and
     * any metadata (e.g., remaining permits, wait time)
     */
    NRateLimitValueResult claimAndRun(int count, Duration timeout, Runnable runnable);

    <T> NRateLimitValueResult claimAndCall(int count, Duration timeout, NCallable<T> callable);

}
