package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;

import java.time.Duration;
import java.util.function.IntFunction;

/**
 * @since 0.8.7
 */
public interface NCircuitBreakerCall<T> extends NCallable<T>, NElementDescribable {

    public static enum Status {
        CLOSED, OPEN, HALF_OPEN
    }

    static <T> NCircuitBreakerCall<T> of(NCallable<T> callable) {
        return NConcurrent.of().circuitBreakerCall(callable);
    }

    static <T> NCircuitBreakerCall<T> of(String id, NCallable<T> callable) {
        return NConcurrent.of().circuitBreakerCall(id, callable);
    }

    NCircuitBreakerCall<T> setFailureThreshold(int failureThreshold);
    NCircuitBreakerCall<T> setSuccessThreshold(int successThreshold);

    NCircuitBreakerCall<T> setSuccessRetryPeriod(IntFunction<Duration> retryPeriod);
    NCircuitBreakerCall<T> setFailureRetryPeriod(IntFunction<Duration> retryPeriod);

    /**
     * blocking result retrieval, when error (and after recover) add a second recover
     *
     * @return the result or throws an error
     */
    T callOrElse(NCallable<T> recover);
    T callOrLast();
}
