package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;

/**
 * Represents a callable task with bounded concurrency control.
 * <p>
 * An {@code NBulkheadCall} wraps a computation and ensures that at most N
 * instances of it execute concurrently. When the limit is reached, callers can
 * choose to wait, fail immediately, or provide fallback behavior.
 * </p>
 *
 * <p>
 * Unlike {@link NRateLimitValue}, which controls the rate of requests over time,
 * this interface controls the number of simultaneously active executions. This is
 * useful for protecting resources that have hard concurrency limits (database
 * connections, API quotas, thread pools).
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * NBulkheadCall<String> limiter = NBulkheadCall.of(() -> {
 *     return expensiveDatabaseQuery();
 * })
 * .setMaxConcurrent(5);
 *
 * // Try to execute, fail fast if limit reached
 * NOptional<String> result = limiter.tryCall();
 * if (result.isEmpty()) {
 *     // Handle rejection
 * }
 *
 * // Or wait up to 1 second for a slot
 * String result = limiter.call(NDuration.ofSeconds(1));
 * }</pre>
 * </p>
 *
 * @param <T> the type of result produced by the callable
 * @see NRateLimitValue
 * @see NCircuitBreakerCall
 * @since 0.8.8
 */
public interface NBulkheadCall<T>  extends NCallable<T>, NElementDescribable {
    /**
     * Creates a new concurrency limiter wrapping the given callable.
     *
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@code NConcurrencyLimiter} instance
     */
    static <T> NBulkheadCall<T> of(NCallable<T> callable) {
        return of(null,callable);
    }

    /**
     * Creates a new concurrency limiter with a specific identifier.
     *
     * @param id an identifier for this limiter
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@code NConcurrencyLimiter} instance
     */
    static <T> NBulkheadCall<T> of(String id, NCallable<T> callable) {
        return NConcurrent.of().bulkheadCallFactory().bulkheadCall(id,callable);
    }

    /**
     * Sets permit expiry time for crash recovery.
     * If a permit is held longer than this, it's considered abandoned.
     *
     * @param expiry expiry duration
     * @return this instance for fluent chaining
     */
    NBulkheadCall<T> setPermitExpiry(NDuration expiry);

    /**
     * Sets the maximum number of concurrent executions allowed.
     *
     * @param maxConcurrent maximum concurrent executions (must be > 0)
     * @return this instance for fluent chaining
     */
    NBulkheadCall<T> setMaxConcurrent(int maxConcurrent);

    /**
     * Attempts to execute the callable immediately without waiting.
     * <p>
     * If the concurrency limit has been reached, returns an empty {@link NOptional}
     * immediately without blocking or executing the callable.
     * </p>
     *
     * @return optional containing the result if execution succeeded, empty if rejected
     */
    NOptional<T> tryCall();

    /**
     * Attempts to execute the callable, providing a fallback if rejected.
     * <p>
     * If the concurrency limit has been reached, executes the fallback callable
     * instead of the main callable.
     * </p>
     *
     * @param fallback callable to execute if rejected
     * @return the result of either the main or fallback callable
     */
    T tryCallOrElse(NCallable<T> fallback);

    /**
     * Executes the callable, waiting indefinitely for a slot if necessary.
     * <p>
     * This method blocks until the callable can be executed within the
     * concurrency limit. The thread may be interrupted while waiting.
     * </p>
     *
     * @return the result of the callable
     * @throws NInterruptedException if interrupted while waiting
     */
    T callBlocking() throws NInterruptedException;

    /**
     * Executes the callable, waiting up to the specified timeout for a slot.
     * <p>
     * If a slot becomes available within the timeout, executes the callable.
     * Otherwise, throws {@link NConcurrencyLimitException}.
     * </p>
     *
     * @param timeout maximum time to wait
     * @return the result of the callable
     * @throws NConcurrencyLimitException if timeout expires
     * @throws NInterruptedException if interrupted while waiting
     */
    T callBlocking(NDuration timeout) throws NInterruptedException;

    /**
     * Executes the callable with timeout, providing a fallback on rejection or timeout.
     *
     * @param timeout maximum time to wait
     * @param fallback callable to execute if timeout expires
     * @return the result of either the main or fallback callable
     * @throws NInterruptedException if interrupted while waiting
     */
    T callOrElse(NDuration timeout, NCallable<T> fallback) throws NInterruptedException;

    /**
     * Returns the maximum allowed concurrent executions.
     *
     * @return the concurrency limit
     */
    int getMaxConcurrent();

    /**
     * Returns the current number of active executions.
     *
     * @return number of currently running calls
     */
    int getActiveCalls();

    /**
     * Returns the number of available execution slots.
     *
     * @return number of slots not currently in use
     */
    int getAvailableSlots();

    /**
     * Returns true if the concurrency limit has been reached.
     *
     * @return true if no slots are available
     */
    boolean isFull();
}
