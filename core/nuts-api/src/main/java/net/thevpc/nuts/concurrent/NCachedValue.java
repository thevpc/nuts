package net.thevpc.nuts.concurrent;

import java.time.Duration;
import java.util.function.Supplier;

public interface NCachedValue<T> {

    static <T> NCachedValue<T> of(Supplier<T> supplier) {
        return NConcurrentFactory.of().createCachedValue(supplier);
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
