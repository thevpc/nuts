package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NCopiable;

import java.time.Duration;
import java.util.function.IntFunction;

/**
 * @since 0.8.7
 */
public class NCircuitBreakerCallModel implements Cloneable, NCopiable {
    private String id;
    private Throwable throwable;

    private NCircuitBreakerCall.Status status = NCircuitBreakerCall.Status.OPEN;
    private int failureThreshold = 5;
    private int successThreshold = 3;
    private int failureCount = 0;
    private int successCount = 0;
    private long openTimestamp = 0;
    private IntFunction<Duration> successRetryPeriod;
    private IntFunction<Duration> failureRetryPeriod;

    private Object lastValidResult;
    private NCallable<?> caller;

    public NCircuitBreakerCallModel() {
    }

    public NCircuitBreakerCall.Status getStatus() {
        return status;
    }

    public NCircuitBreakerCallModel setStatus(NCircuitBreakerCall.Status status) {
        this.status = status;
        return this;
    }


    public NCallable<?> getCaller() {
        return caller;
    }

    public NCircuitBreakerCallModel setCaller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }

    public NCircuitBreakerCallModel(String id) {
        this.id = id;
    }

    public Object getThrowable() {
        return throwable;
    }



    public NCircuitBreakerCallModel setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }


    public String getId() {
        return id;
    }

    public NCircuitBreakerCallModel setId(String id) {
        this.id = id;
        return this;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public NCircuitBreakerCallModel setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
        return this;
    }

    public int getSuccessThreshold() {
        return successThreshold;
    }

    public NCircuitBreakerCallModel setSuccessThreshold(int successThreshold) {
        this.successThreshold = successThreshold;
        return this;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public NCircuitBreakerCallModel setFailureCount(int failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public NCircuitBreakerCallModel setSuccessCount(int successCount) {
        this.successCount = successCount;
        return this;
    }

    public long getOpenTimestamp() {
        return openTimestamp;
    }

    public NCircuitBreakerCallModel setOpenTimestamp(long openTimestamp) {
        this.openTimestamp = openTimestamp;
        return this;
    }

    public IntFunction<Duration> getSuccessRetryPeriod() {
        return successRetryPeriod;
    }

    public NCircuitBreakerCallModel setSuccessRetryPeriod(IntFunction<Duration> successRetryPeriod) {
        this.successRetryPeriod = successRetryPeriod;
        return this;
    }

    public IntFunction<Duration> getFailureRetryPeriod() {
        return failureRetryPeriod;
    }

    public NCircuitBreakerCallModel setFailureRetryPeriod(IntFunction<Duration> failureRetryPeriod) {
        this.failureRetryPeriod = failureRetryPeriod;
        return this;
    }

    public Object getLastValidResult() {
        return lastValidResult;
    }

    public NCircuitBreakerCallModel setLastValidResult(Object lastValidResult) {
        this.lastValidResult = lastValidResult;
        return this;
    }

    public NCircuitBreakerCallModel copy(){
        return clone();
    }

    protected NCircuitBreakerCallModel clone(){
        try {
            return (NCircuitBreakerCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
