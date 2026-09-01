package net.thevpc.nuts.concurrent;

/**
 * Handler for processing results of the retry call.
 *
 * @param <T> type of the result
 */
public interface NRetryHandler<T> {
    /**
     * Handle.
     *
     * @param result result
     */
    void handle(NRetryResult<T> result);
}
