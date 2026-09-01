package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NGetter;

/**
 * Encapsulates the result of a retry call, including status and value.
 *
 * @param <T> type of the result
 */
public interface NRetryResult<T> {
    /**
     * Unique identifier of the retry call.
     *
     * @return the call ID
     */
    @NGetter
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
    @NGetter
    boolean isValid();

    /**
     * Returns true if the call failed.
     *
     * @return true if error occurred
     */
    @NGetter
    boolean isError();

    /**
     * Returns the actual result of the call.
     *
     * @return the result value
     */
    T result();
}
