package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;

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
    private Throwable throwable;
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
    private NRetryCall.Handler<?> handler;
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

    public NRetryCall.Status getStatus() {
        return status;
    }

    public NRetryCallModel setStatus(NRetryCall.Status status) {
        this.status = status;
        return this;
    }

    public NRetryCall.Handler<?> getHandler() {
        return handler;
    }

    public NRetryCallModel setHandler(NRetryCall.Handler<?> handler) {
        this.handler = handler;
        return this;
    }

    public NCallable<?> getCaller() {
        return caller;
    }

    public NRetryCallModel setCaller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }


    public Object getThrowable() {
        return throwable;
    }

    public NCallable<?> getRecover() {
        return recover;
    }

    public NRetryCallModel setRecover(NCallable<?> recover) {
        this.recover = recover;
        return this;
    }

    public NRetryCallModel setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }


    public String getId() {
        return id;
    }

    public NRetryCallModel setId(String id) {
        this.id = id;
        return this;
    }

    public Object getResult() {
        return result;
    }

    public NRetryCallModel setResult(Object result) {
        this.result = result;
        return this;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public NRetryCallModel setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    public NDuration getExpiry() {
        return expiry;
    }

    public NRetryCallModel setExpiry(NDuration expiry) {
        this.expiry = expiry;
        return this;
    }

    public IntFunction<NDuration> getRetryPeriod() {
        return retryPeriod;
    }

    public NRetryCallModel setRetryPeriod(IntFunction<NDuration> retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public NRetryCallModel setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Creates a copy of this model.
     *
     * @return a clone of this instance
     */
    public NRetryCallModel copy() {
        return clone();
    }

    protected NRetryCallModel clone() {
        try {
            return (NRetryCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
