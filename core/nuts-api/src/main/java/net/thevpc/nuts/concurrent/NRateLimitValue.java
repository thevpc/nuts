package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

/**
 * Represents a runtime rate-limited value, capable of controlling access to
 * a resource or operation based on configured rate limits.
 * <p>
 * Instances are typically created using {@link #ofBuilder(String)}, which
 * delegates to the default in-memory {@link NRateLimitValueFactory}.
 * <p>
 * This interface provides both non-blocking ({@code take}) and blocking
 * ({@code claim}) methods for acquiring permits, with optional support
 * for executing actions or callables once the permits are obtained.
 *
 * @since 0.8.8
 */
public interface NRateLimitValue extends NElementDescribable {
    /**
     * Creates a builder for a new {@link NRateLimitValue} using the default
     * (in-memory) {@link NRateLimitValueFactory}.
     * <p>
     * The builder allows configuration of rate-limiting parameters before
     * invoking {@code build()} to create the final instance.
     *
     * @param name value name or identifier, used to reference this limiter
     * @return a new {@link NRateLimitValueBuilder} instance
     */
    static NRateLimitValueBuilder ofBuilder(String name) {
        return NRateLimitValueFactory.of().ofBuilder(name);
    }

    /**
     * Attempts to immediately consume one token.
     *
     * @return a result indicating whether the token was successfully consumed
     */
    NRateLimitValueResult take();

    /**
     * Attempts to immediately consume the specified number of tokens.
     *
     * @param count number of tokens to consume; must be positive
     * @return a result indicating success or failure
     */
    NRateLimitValueResult take(int count);

    /**
     * Attempts to consume one token and, if successful, executes the given runnable.
     *
     * @param runnable the action to execute upon success
     * @return a result describing the operation
     */
    NRateLimitValueResult takeAndRun(Runnable runnable);

    /**
     * Attempts to consume the specified number of tokens and, if successful,
     * executes the given runnable.
     *
     * @param count    number of tokens to consume
     * @param runnable the action to execute upon success
     * @return a result describing the operation
     */
    NRateLimitValueResult takeAndRun(int count, Runnable runnable);

    /**
     * Attempts to consume one token and, if successful, calls the given callable.
     *
     * @param callable the action to execute upon success
     * @param <T>      callable result type
     * @return a result describing the operation
     */
    <T> NRateLimitValueResult takeAndCall(NCallable<T> callable);

    /**
     * Attempts to consume the specified number of tokens and, if successful,
     * calls the given callable.
     *
     * @param count    number of tokens to consume
     * @param callable the action to execute upon success
     * @param <T>      callable result type
     * @return a result describing the operation
     */
    <T> NRateLimitValueResult takeAndCall(int count, NCallable<T> callable);

    /**
     * Blocks until the specified number of tokens are available, then consumes them.
     *
     * @param count number of tokens to claim
     * @return a result describing the claim
     */
    NRateLimitValueResult claim(int count);


    /**
     * Waits up to the given timeout for the specified number of tokens to become available.
     *
     * @param count   number of tokens to claim
     * @param timeout maximum wait duration
     * @return a result describing whether the claim succeeded
     */
    NRateLimitValueResult claim(int count, NDuration timeout);

    /**
     * Claims one token and, upon success, runs the given runnable.
     *
     * @param runnable action to execute upon success
     * @return a result describing the operation
     */
    NRateLimitValueResult claimAndRun(Runnable runnable);

    /**
     * Claims the specified number of tokens and, upon success, runs the given runnable.
     *
     * @param count    number of tokens to claim
     * @param runnable action to execute upon success
     * @return a result describing the operation
     */
    NRateLimitValueResult claimAndRun(int count, Runnable runnable);

    /**
     * Claims one token, waiting up to the given timeout, then executes the given runnable
     * if successful.
     *
     * @param timeout  maximum time to wait for tokens
     * @param runnable action to execute upon success
     * @return a result describing the operation
     */
    NRateLimitValueResult claimAndRun(NDuration timeout, Runnable runnable);

    /**
     * Claims the specified number of tokens, then executes the given callable if successful.
     *
     * @param count    number of tokens to claim
     * @param callable action to execute upon success
     * @param <T>      callable result type
     * @return a result describing the operation
     */
    <T> NRateLimitValueResult claimAndCall(int count, NCallable<T> callable);

    /**
     * Claims the 1 token, then executes the given callable if successful.
     *
     * @param callable action to execute upon success
     * @param <T>      callable result type
     * @return a result describing the operation
     */
    <T> NRateLimitValueResult claimAndCall(NDuration timeout, NCallable<T> callable);

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
    NRateLimitValueResult claimAndRun(int count, NDuration timeout, Runnable runnable);

    /**
     * Claims the specified number of tokens, waiting up to the given timeout,
     * and calls the provided callable if successful.
     *
     * @param count    number of tokens to claim
     * @param timeout  maximum time to wait for permits
     * @param callable action to execute upon success
     * @param <T>      callable result type
     * @return a result describing the operation
     */
    <T> NRateLimitValueResult claimAndCall(int count, NDuration timeout, NCallable<T> callable);

}
