package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

/**
 * @since 0.8.7
 */
public interface NRetryCall<T> extends NCallable<T>, NElementDescribable {

    public static enum Status {
        CREATED, QUEUED, RUNNING, FAILED_ATTEMPT, RETRYING, SUCCEEDED, FAILED, CANCELLED, HANDLING, HANDLED, HANDLER_FAILED, HANDLER_SUCCEEDED
    }

    static <T> NRetryCall<T> of(NCallable<T> callable) {
        return NConcurrent.of().retryCall(callable);
    }

    static <T> NRetryCall<T> of(String id, NCallable<T> callable) {
        return NConcurrent.of().retryCall(id, callable);
    }

    NRetryCall<T> setMaxRetries(int maxRetries);

    NRetryCall<T> setRetryPeriod(IntFunction<NDuration> retryPeriod);

    NRetryCall<T> setMultipliedRetryPeriod(NDuration basePeriod, double multiplier);
    NRetryCall<T> setExponentialRetryPeriod(NDuration basePeriod, double multiplier);

    NRetryCall<T> setRetryPeriod(NDuration period);

    NRetryCall<T> setRetryPeriods(NDuration... periods);

    /**
     * add recover processing when max attempts are reached without having a good result
     *
     * @param recover recover processing
     * @return @{this}
     */
    NRetryCall<T> setRecover(NCallable<T> recover);

    NRetryCall<T> setHandler(Handler<T> handler);

    /**
     * blocking result retrieval, when error (and after recover) add a second recover
     *
     * @return the result or throws an error
     */
    T callOrElse(NCallable<T> recover);

    /**
     * blocking result retrieval
     *
     * @return the result or throws an error
     */
    T call();

    void callAsync();

    /**
     * return a future instance that returns when the result is retrieved
     *
     * @return the result or throws an error
     */
    Future<Result<T>> callFuture();

    interface Handler<T> {
        void handle(Result<T> result);
    }

    interface Result<T> {

        String id();

        NRetryCall<T> value();

        boolean isValid();

        boolean isError();

        T result();
    }

}
