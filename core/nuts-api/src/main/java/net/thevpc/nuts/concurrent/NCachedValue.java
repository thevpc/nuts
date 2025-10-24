package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

import java.util.function.Supplier;

/**
 * Represents a lazily computed and optionally cached value that can automatically
 * expire, retry loading, and retain its last valid result on failure.
 * <p>
 * An {@code NCachedValue<T>} wraps a {@link Supplier} of a value, caching its result
 * for a configurable duration (expiry). Once expired, the value may be recomputed
 * the next time it is requested. The cache also supports retry behavior to
 * control how often it reattempts loading after a failure.
 * <p>
 * Implementations are typically thread-safe and designed for concurrent access.
 *
 * @param <T> the type of cached value
 * @since 0.8.6
 */
public interface NCachedValue<T> extends Supplier<T>, NElementDescribable {

    /**
     * Creates a new cached value that uses the given supplier.
     * <p>
     * Equivalent to {@code NConcurrent.of().cachedValue(supplier)}.
     *
     * @param supplier the supplier used to compute the value
     * @param <T>      the value type
     * @return a new cached value
     */
    static <T> NCachedValue<T> of(Supplier<T> supplier) {
        return NConcurrent.of().cachedValue(supplier);
    }


    /**
     * Sets the expiry duration for the cached value.
     * After this duration has elapsed since the last successful computation,
     * the value becomes expired and will be recomputed on next access.
     *
     * @param expiry the expiry duration, or {@code null} for no expiry
     * @return this instance
     */
    NCachedValue<T> setExpiry(NDuration expiry);

    /**
     * Sets the expiry duration in milliseconds.
     *
     * @param expiry the expiry duration in milliseconds, or {@code 0} for no expiry
     * @return this instance
     */
    NCachedValue<T> setExpiryMillis(long expiry);

    /**
     * Sets the maximum number of retry attempts after a failed computation.
     * Once the maximum is reached, subsequent attempts will wait for the retry period
     * (if any) before trying again.
     *
     * @param maxRetries the maximum retry count
     * @return this instance
     */
    NCachedValue<T> setMaxRetries(int maxRetries);

    /**
     * Sets the minimum period between retry attempts after a failed computation.
     * <p>
     * A retry period of zero means that a new attempt will be made immediately
     * on the next access. A non-zero period delays retries to avoid hammering
     * a failing source.
     *
     * @param retryPeriod the retry period, or {@code null} for immediate retry
     * @return this instance
     */
    NCachedValue<T> setRetryPeriod(NDuration retryPeriod);

    /**
     * Sets both retry parameters at once: the maximum retry count and the retry period.
     *
     * @param maxRetries  the maximum number of retries
     * @param retryPeriod the delay between retry attempts
     * @return this instance
     */
    NCachedValue<T> setRetry(int maxRetries, NDuration retryPeriod);

    /**
     * Configures whether to retain the last successfully computed value
     * when a subsequent computation fails.
     * <p>
     * When {@code retain} is {@code true}, {@link #get()} will continue returning
     * the last valid value even if the most recent update failed.
     *
     * @param retain {@code true} to retain the last valid value on failure
     * @return this instance
     */
    NCachedValue<T> retainLastOnFailure(boolean retain);

    /**
     * Invalidates the cached value immediately.
     * <p>
     * The next call to {@link #get()} will trigger recomputation.
     *
     * @return this instance
     */
    NCachedValue<T> invalidate();

    /**
     * Returns whether the current cached value is valid (not expired or invalidated).
     *
     * @return {@code true} if the value is valid
     */
    boolean isValid();

    /**
     * Returns whether the last computation resulted in an error.
     *
     * @return {@code true} if the value failed to compute
     */
    boolean isError();

    /**
     * Returns whether the value has been computed at least once.
     *
     * @return {@code true} if the value has been evaluated
     */
    boolean isEvaluated();

    /**
     * Returns whether the cached value has expired and should be recomputed
     * on the next access.
     *
     * @return {@code true} if the value is expired
     */
    boolean isExpired();

    /**
     * Returns the current cached value, computing or recomputing it if necessary.
     * <p>
     * If the value is expired, invalid, or missing, a new computation is triggered
     * according to the configured retry and expiry policies.
     *
     * @return the cached value
     * @throws RuntimeException if the computation fails and no valid value is available
     */
    T get();

    /**
     * Immediately computes a new value using the provided supplier and stores it in the cache.
     * <p>
     * If a non-null {@code supplier} is provided, it is used once to compute
     * a new value, without replacing the instance’s configured supplier.
     * If {@code supplier} is {@code null}, the internal supplier is used instead,
     * behaving exactly like {@link #update()}.
     * <p>
     * The newly computed value replaces the current cached value, marks the cache
     * as valid (clears any previous error state), and resets the expiry timer.
     *
     * @param supplier optional supplier used for this computation; if {@code null},
     *                 the internal supplier is used
     * @return this instance
     * @throws RuntimeException if the computation fails and the cache is not configured
     *                          to retain the last valid value
     */
    NCachedValue<T> update(Supplier<T> supplier);

    /**
     * Forces an immediate recomputation of the cached value using the internal supplier.
     * <p>
     * This method is equivalent to calling {@link #update(Supplier)} with a {@code null}
     * argument. The internal supplier is invoked to compute a new value, which replaces
     * the current cached value. Upon success, the cache is marked as valid, any previous
     * error state is cleared, and the expiry timer is reset.
     *
     * @return this instance
     * @throws RuntimeException if the computation fails and the cache is not configured
     *                          to retain the last valid value
     */    NCachedValue<T> update();

    /**
     * Directly sets the cached value and marks it as valid.
     *
     * @param value the new cached value
     * @return this instance
     */
    NCachedValue<T> setValue(T value);

    /**
     * Computes and updates the cached value if it is currently invalid.
     * <p>
     * If the cached value is considered invalid — meaning it has never been
     * evaluated, has expired, or is in an error state — this method will
     * compute a new value using the provided {@code supplier}. The computed value
     * will then replace the old one in the cache and be marked as valid.
     * <p>
     * If the provided {@code supplier} is {@code null}, the internal supplier
     * (the one originally passed to the cached value at construction) is used instead.
     * This allows recomputation using the cache's default logic without passing
     * a supplier explicitly.
     * <p>
     * If the cached value is already valid, this method does nothing and returns {@code false}.
     * <p>
     * This method is synchronized internally to ensure atomic evaluation and update.
     *
     * @param supplier the value supplier to use for recomputation, or {@code null}
     *                 to fallback to the internal supplier
     * @return {@code true} if a new value was computed and set, {@code false} if the
     * cached value was already valid
     * @throws RuntimeException if the computation fails and no previous valid value
     *                          can be retained
     */
    boolean computeAndSetIfInvalid(Supplier<T> supplier);

    /**
     * Sets a provided value only if the current one is invalid or expired.
     *
     * @param value the value to set
     * @return {@code true} if the provided value was set
     */
    boolean setValueIfInvalid(T value);
}
