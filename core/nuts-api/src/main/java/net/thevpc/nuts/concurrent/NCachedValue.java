package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public interface NCachedValue<T> extends Supplier<T>, NElementDescribable {

    static <T> NCachedValue<T> of(Supplier<T> supplier) {
        return NConcurrent.of().cachedValue(supplier);
    }

    NCachedValue<T> setExpiry(NDuration expiry);
    NCachedValue<T> setExpiryMillis(long expiry);

    NCachedValue<T> setMaxRetries(int maxRetries);

    NCachedValue<T> setRetryPeriod(NDuration retryPeriod);

    NCachedValue<T> setRetry(int maxRetries, NDuration retryPeriod);

    NCachedValue<T> retainLastOnFailure(boolean retain);

    NCachedValue<T> invalidate();

    boolean isValid();

    boolean isError();

    boolean isEvaluated();

    boolean isExpired();

    T get();

    NCachedValue<T> update(Supplier<T> supplier);

    NCachedValue<T> update();

    NCachedValue<T> setValue(T value);

    boolean computeAndSetIfInvalid(Supplier<T> supplier);

    boolean setValueIfInvalid(T value);
}
