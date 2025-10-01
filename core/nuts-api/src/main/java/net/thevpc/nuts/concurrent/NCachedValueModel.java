package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.time.Duration;

/**
 * @since 0.8.6
 */
public class NCachedValueModel implements Cloneable, NCopiable {
    private String id;
    private Object value;
    private Throwable throwable;
    private Boolean errorState;

    private Object lastValidValue = null;
    private long lastEvalTimestamp = 0;
    private int failedAttempts = 0;

    private Duration expiry = Duration.ofMillis(Long.MAX_VALUE);
    private Duration retryPeriod = Duration.ZERO;
    private int maxRetries = 0;
    private boolean retainLastOnFailure = false;

    public NCachedValueModel() {
    }

    public NCachedValueModel(String id) {
        this.id = id;
    }

    public Object getThrowable() {
        return throwable;
    }

    public NCachedValueModel setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public String getId() {
        return id;
    }

    public NCachedValueModel setId(String id) {
        this.id = id;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public NCachedValueModel setValue(Object value) {
        this.value = value;
        return this;
    }

    public Boolean getErrorState() {
        return errorState;
    }

    public NCachedValueModel setErrorState(Boolean errorState) {
        this.errorState = errorState;
        return this;
    }

    public Object getLastValidValue() {
        return lastValidValue;
    }

    public NCachedValueModel setLastValidValue(Object lastValidValue) {
        this.lastValidValue = lastValidValue;
        return this;
    }

    public long getLastEvalTimestamp() {
        return lastEvalTimestamp;
    }

    public NCachedValueModel setLastEvalTimestamp(long lastEvalTimestamp) {
        this.lastEvalTimestamp = lastEvalTimestamp;
        return this;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public NCachedValueModel setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    public Duration getExpiry() {
        return expiry;
    }

    public NCachedValueModel setExpiry(Duration expiry) {
        this.expiry = expiry;
        return this;
    }

    public Duration getRetryPeriod() {
        return retryPeriod;
    }

    public NCachedValueModel setRetryPeriod(Duration retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public NCachedValueModel setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public boolean isRetainLastOnFailure() {
        return retainLastOnFailure;
    }

    public NCachedValueModel setRetainLastOnFailure(boolean retainLastOnFailure) {
        this.retainLastOnFailure = retainLastOnFailure;
        return this;
    }

    public NCachedValueModel copy(){
        return clone();
    }

    protected NCachedValueModel clone(){
        try {
            return (NCachedValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
