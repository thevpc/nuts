package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

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

    @NGetter
    public NRetryCall.Status status() {
        return status;
    }

    @NSetter
    public NRetryCallModel status(NRetryCall.Status status) {
        this.status = status;
        return this;
    }

    @NGetter
    public NRetryCall.Handler<?> handler() {
        return handler;
    }

    @NSetter
    public NRetryCallModel handler(NRetryCall.Handler<?> handler) {
        this.handler = handler;
        return this;
    }

    @NGetter
    public NCallable<?> caller() {
        return caller;
    }

    @NSetter
    public NRetryCallModel caller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }


    @NGetter
    public Object error() {
        return error;
    }

    @NGetter
    public NCallable<?> recover() {
        return recover;
    }

    @NSetter
    public NRetryCallModel recover(NCallable<?> recover) {
        this.recover = recover;
        return this;
    }

    @NSetter
    public NRetryCallModel error(Throwable throwable) {
        this.error = throwable;
        return this;
    }


    @NGetter
    public String id() {
        return id;
    }

    @NSetter
    public NRetryCallModel id(String id) {
        this.id = id;
        return this;
    }

    @NGetter
    public Object result() {
        return result;
    }

    @NSetter
    public NRetryCallModel result(Object result) {
        this.result = result;
        return this;
    }

    @NGetter
    public int failedAttempts() {
        return failedAttempts;
    }

    @NSetter
    public NRetryCallModel failedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    @NGetter
    public NDuration expiry() {
        return expiry;
    }

    @NSetter
    public NRetryCallModel expiry(NDuration expiry) {
        this.expiry = expiry;
        return this;
    }

    @NGetter
    public IntFunction<NDuration> retryPeriod() {
        return retryPeriod;
    }

    @NSetter
    public NRetryCallModel retryPeriod(IntFunction<NDuration> retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    @NGetter
    public int maxRetries() {
        return maxRetries;
    }

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
