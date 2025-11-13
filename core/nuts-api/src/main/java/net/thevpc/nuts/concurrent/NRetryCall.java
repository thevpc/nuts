package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

import java.util.concurrent.Future;
import java.util.function.IntFunction;

/**
 * A retryable callable task.
 * <p>
 * This interface extends {@link NCallable} and provides a mechanism to automatically retry
 * failed calls according to configurable retry policies. It also supports asynchronous execution,
 * recover operations, and custom handlers for processing results or failures.
 *
 * @param <T> the type of the result returned by this retry call
 * @since 0.8.7
 */
public interface NRetryCall<T> extends NCallable<T>, NElementDescribable {

    /**
     * Status of the retry call during its lifecycle.
     */
    public static enum Status {
        CREATED, QUEUED, RUNNING, FAILED_ATTEMPT, RETRYING, SUCCEEDED, FAILED, CANCELLED, HANDLING, HANDLED, HANDLER_FAILED, HANDLER_SUCCEEDED
    }


    /**
     * Creates a new retryable call for the given callable using the default concurrency manager.
     *
     * @param callable the callable to retry
     * @param <T> the type of the result
     * @return a new {@link NRetryCall} instance
     */
    static <T> NRetryCall<T> of(NCallable<T> callable) {
        return NConcurrent.of().retryCall(callable);
    }

    /**
     * Creates a new retryable call with a specific identifier.
     *
     * @param id the unique identifier for this retry call
     * @param callable the callable to retry
     * @param <T> the type of the result
     * @return a new {@link NRetryCall} instance
     */
    static <T> NRetryCall<T> of(String id, NCallable<T> callable) {
        return NConcurrent.of().retryCall(id, callable);
    }

    /**
     * Sets the maximum number of retry attempts.
     *
     * @param maxRetries maximum retries
     * @return this instance
     */
    NRetryCall<T> setMaxRetries(int maxRetries);

    /**
     * Sets a dynamic retry period function based on attempt index.
     *
     * @param retryPeriod function mapping attempt index to duration
     * @return this instance
     */
    NRetryCall<T> setRetryPeriod(IntFunction<NDuration> retryPeriod);

    /**
     * Sets a multiplied retry period based on a base period and a multiplier factor.
     *
     * @param basePeriod base duration
     * @param multiplier factor to multiply base duration for each retry
     * @return this instance
     */
    NRetryCall<T> setMultipliedRetryPeriod(NDuration basePeriod, double multiplier);

    /**
     * Sets an exponential retry period.
     *
     * @param basePeriod base duration
     * @param multiplier exponential growth factor
     * @return this instance
     */
    NRetryCall<T> setExponentialRetryPeriod(NDuration basePeriod, double multiplier);

    /**
     * Sets a fixed retry period for all attempts.
     *
     * @param period fixed duration
     * @return this instance
     */
    NRetryCall<T> setRetryPeriod(NDuration period);

    /**
     * Sets a sequence of retry periods for consecutive attempts.
     *
     * @param periods array of durations
     * @return this instance
     */
    NRetryCall<T> setRetryPeriods(NDuration... periods);

    /**
     * Adds a recovery callable to execute if all retry attempts fail.
     *
     * @param recover recovery callable
     * @return this instance
     */
    NRetryCall<T> setRecover(NCallable<T> recover);

    /**
     * Sets a custom handler to process the result after completion.
     *
     * @param handler result handler
     * @return this instance
     */
    NRetryCall<T> setHandler(Handler<T> handler);

    /**
     * Executes the call, blocking until a result is obtained.
     * If an error occurs, the provided recovery callable is executed.
     *
     * @param recover recovery callable to execute on failure
     * @return the result of the call
     */
    T callOrElse(NCallable<T> recover);

    /**
     * Executes the call synchronously, blocking until completion.
     *
     * @return the result of the call
     * @throws RuntimeException if all retries fail and no recovery is provided
     */
    T call();


    /**
     * Executes the call asynchronously.
     */
    void callAsync();


    /**
     * Returns a {@link Future} representing the asynchronous execution of this retry call.
     *
     * @return a future with the {@link Result} of the call
     */
    Future<Result<T>> callFuture();

    /**
     * Handler for processing results of the retry call.
     *
     * @param <T> type of the result
     */
    interface Handler<T> {
        void handle(Result<T> result);
    }

    /**
     * Encapsulates the result of a retry call, including status and value.
     *
     * @param <T> type of the result
     */
    interface Result<T> {
        /**
         * Unique identifier of the retry call.
         *
         * @return the call ID
         */
        String id();

        /**
         * Returns the {@link NRetryCall} instance associated with this result.
         *
         * @return the retry call
         */
        NRetryCall<T> value();

        /**
         * Returns true if the result is valid (call succeeded or recovery succeeded).
         *
         * @return true if valid
         */
        boolean isValid();

        /**
         * Returns true if the call failed.
         *
         * @return true if error occurred
         */
        boolean isError();

        /**
         * Returns the actual result of the call.
         *
         * @return the result value
         */
        T result();
    }

}
