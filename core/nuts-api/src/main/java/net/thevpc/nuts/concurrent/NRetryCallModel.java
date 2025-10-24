package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;

import java.util.function.IntFunction;

/**
 * @since 0.8.7
 */
public class NRetryCallModel implements Cloneable, NCopiable {
    private String id;
    private Throwable throwable;

    private NRetryCall.Status status = NRetryCall.Status.CREATED;
    private Object result = null;
    private int failedAttempts = 0;

    private NDuration expiry = NDuration.ofMillis(Long.MAX_VALUE);
    private NCallable<?> recover;
    private NCallable<?> caller;
    private NRetryCall.Handler<?> handler;
    private IntFunction<NDuration> retryPeriod;
    private int maxRetries = 0;

    public NRetryCallModel() {
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

    public NRetryCallModel(String id) {
        this.id = id;
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

    public NRetryCallModel copy(){
        return clone();
    }

    protected NRetryCallModel clone(){
        try {
            return (NRetryCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
