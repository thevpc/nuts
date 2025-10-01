package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public interface NCachedValue<T> extends Supplier<T>, NElementDescribable {

    static <T> NCachedValue<T> of(Supplier<T> supplier) {
        return NConcurrent.of().cachedValue(supplier);
    }

    NCachedValue<T> setExpiry(Duration expiry);

    NCachedValue<T> setMaxRetries(int maxRetries);

    NCachedValue<T> setRetryPeriod(Duration retryPeriod);

    NCachedValue<T> setRetry(int maxRetries, Duration retryPeriod);

    NCachedValue<T> retainLastOnFailure(boolean retain);

    void invalidate();

    boolean isValid();

    boolean isError();

    boolean isEvaluated();

    boolean isExpired();

    T get();

    NCachedValue<T> computeAndSet(Supplier<T> supplier);

    NCachedValue<T> setValue(T value);

    boolean computeAndSetIfInvalid(Supplier<T> supplier);

    boolean setValueIfInvalid(T value) ;
}
