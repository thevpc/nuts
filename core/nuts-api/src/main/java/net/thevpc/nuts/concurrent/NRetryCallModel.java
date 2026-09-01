package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Model class representing the state and configuration of a retryable call.
 * <p>
 * Used internally by {@link NRetryCall} and {@link NRetryCallFactory} to store the
 * current status, results, retry policy, and associated handlers or recovery logic.
 *
 * @since 0.8.7
 */
public class NRetryCallModel implements Cloneable, NCopiable {
    /**
     * Unique identifier for this retry call.
     */
    private String id;
    /**
     * Last throwable encountered during a failed attempt, if any.
     */
    private Throwable error;
    /**
     * Current status of the retry call. Defaults to {@link NRetryCall.Status#CREATED}.
     */
    private NRetryCall.Status status = NRetryCall.Status.CREATED;
    /**
     * Result of the call, if successfully completed.
     */
    private Object result = null;
    /**
     * Number of failed attempts so far.
     */
    private int failedAttempts = 0;
    /**
     * Expiry duration for this retry call. Defaults to essentially infinite.
     */
    private NDuration expiry = NDuration.ofMillis(Long.MAX_VALUE);
    /**
     * Recovery callable executed when max attempts are reached without success.
     */
    private NCallable<?> recover;
    /**
     * Original callable associated with this retry call.
     */
    private NCallable<?> caller;
    /**
     * Optional handler invoked after call completion.
     */
    private NRetryHandler<?> handler;
    /**
     * Function that calculates the retry period dynamically based on attempt index.
     */
    private IntFunction<NDuration> retryPeriod;
    /**
     * Maximum number of retries allowed. Defaults to 0.
     */
    private int maxRetries = 0;

    /**
     * Default constructor.
     */
    public NRetryCallModel() {
    }

    /**
     * Constructor with identifier.
     */
    public NRetryCallModel(String id) {
        this.id = id;
    }

    /**
     * Status.
     *
     * @return status result
     */
    @NGetter
    public NRetryCall.Status status() {
        return status;
    }

    /**
     * Status.
     *
     * @param status status
     * @return status result
     */
    @NSetter
    public NRetryCallModel status(NRetryCall.Status status) {
        this.status = status;
        return this;
    }

    /**
     * Handler.
     *
     * @return handler result
     */
    @NGetter
    public NRetryHandler<?> handler() {
        return handler;
    }

    /**
     * Handler.
     *
     * @param handler handler
     * @return handler result
     */
    @NSetter
    public NRetryCallModel handler(NRetryHandler<?> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Caller.
     *
     * @return caller result
     */
    @NGetter
    public NCallable<?> caller() {
        return caller;
    }

    /**
     * Caller.
     *
     * @param caller caller
     * @return caller result
     */
    @NSetter
    public NRetryCallModel caller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }


    /**
     * Error.
     *
     * @return error result
     */
    @NGetter
    public Object error() {
        return error;
    }

    /**
     * Recover.
     *
     * @return recover result
     */
    @NGetter
    public NCallable<?> recover() {
        return recover;
    }

    /**
     * Recover.
     *
     * @param recover recover
     * @return recover result
     */
    @NSetter
    public NRetryCallModel recover(NCallable<?> recover) {
        this.recover = recover;
        return this;
    }

    /**
     * Error.
     *
     * @param throwable throwable
     * @return error result
     */
    @NSetter
    public NRetryCallModel error(Throwable throwable) {
        this.error = throwable;
        return this;
    }


    /**
     * Id.
     *
     * @return id result
     */
    @NGetter
    public String id() {
        return id;
    }

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    public NRetryCallModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Result.
     *
     * @return result result
     */
    @NGetter
    public Object result() {
        return result;
    }

    /**
     * Result.
     *
     * @param result result
     * @return result result
     */
    @NSetter
    public NRetryCallModel result(Object result) {
        this.result = result;
        return this;
    }

    /**
     * Failed attempts.
     *
     * @return failed attempts result
     */
    @NGetter
    public int failedAttempts() {
        return failedAttempts;
    }

    /**
     * Failed attempts.
     *
     * @param failedAttempts failed attempts
     * @return failed attempts result
     */
    @NSetter
    public NRetryCallModel failedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    /**
     * Expiry.
     *
     * @return expiry result
     */
    @NGetter
    public NDuration expiry() {
        return expiry;
    }

    /**
     * Expiry.
     *
     * @param expiry expiry
     * @return expiry result
     */
    @NSetter
    public NRetryCallModel expiry(NDuration expiry) {
        this.expiry = expiry;
        return this;
    }

    /**
     * Retry period.
     *
     * @return retry period result
     */
    @NGetter
    public IntFunction<NDuration> retryPeriod() {
        return retryPeriod;
    }

    /**
     * Retry period.
     *
     * @param retryPeriod retry period
     * @return retry period result
     */
    @NSetter
    public NRetryCallModel retryPeriod(IntFunction<NDuration> retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    /**
     * Max retries.
     *
     * @return max retries result
     */
    @NGetter
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Max retries.
     *
     * @param maxRetries max retries
     * @return max retries result
     */
    @NSetter
    public NRetryCallModel maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Creates a copy of this model.
     *
     * @return a clone of this instance
     */
    public NRetryCallModel copy() {
        /**
         * Clone.
         *
         * @return clone result
         */
        return clone();
    }

    /**
     * Clone.
     *
     * @return clone result
     */
    protected NRetryCallModel clone() {
        try {
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NRetryCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRetryCallModel that = (NRetryCallModel) o;
        return failedAttempts == that.failedAttempts && maxRetries == that.maxRetries && Objects.equals(id, that.id) && Objects.equals(error, that.error) && status == that.status && Objects.equals(result, that.result) && Objects.equals(expiry, that.expiry) && Objects.equals(recover, that.recover) && Objects.equals(caller, that.caller) && Objects.equals(handler, that.handler) && Objects.equals(retryPeriod, that.retryPeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, error, status, result, failedAttempts, expiry, recover, caller, handler, retryPeriod, maxRetries);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("id", id)
                .add("error", error)
                .add("status", status)
                .add("result", result)
                .add("failedAttempts", failedAttempts)
                .add("expiry", expiry)
                .add("recover", recover)
                .add("caller", caller)
                .add("handler", handler)
                .add("retryPeriod", retryPeriod)
                .add("maxRetries", maxRetries)
                .build();
    }

}
