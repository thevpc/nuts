package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NSetter;

import java.util.concurrent.Future;

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
public interface NRetryCall<T> extends NCallable<T>, NDescribable, AutoCloseable {

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
        return NConcurrentRPI.of().createRetryCall(callable);
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
        return NConcurrentRPI.of().createRetryCall(id, callable);
    }

    /**
     * Sets the maximum number of retry attempts.
     *
     * @param maxRetries maximum retries
     * @return this instance
     */
    @NSetter
    NRetryCall<T> maxRetries(int maxRetries);

    /**
     * Sets a dynamic retry period function based on attempt index.
     *
     * @param retryPeriod function mapping attempt index to duration
     * @return this instance
     */
    NRetryCall<T> retryPeriod(NRetryPeriodFunction retryPeriod);

    /**
     * Sets a fixed retry period for all attempts.
     *
     * @param period fixed duration
     * @return this instance
     */
    @NSetter
    NRetryCall<T> retryPeriod(NDuration period);

    /**
     * Adds a recovery callable to execute if all retry attempts fail.
     *
     * @param recover recovery callable
     * @return this instance
     */
    @NSetter
    NRetryCall<T> recover(NCallable<T> recover);

    /**
     * Sets a custom handler to process the result after completion.
     *
     * @param handler result handler
     * @return this instance
     */
    @NSetter
    NRetryCall<T> handler(NRetryHandler<T> handler);

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
     * dispose of the retry call and delete it
     */
    void close();

    /**
     * Returns a {@link Future} representing the asynchronous execution of this retry call.
     *
     * @return a future with the {@link NRetryResult} of the call
     */
    Future<NRetryResult<T>> callFuture();

}
