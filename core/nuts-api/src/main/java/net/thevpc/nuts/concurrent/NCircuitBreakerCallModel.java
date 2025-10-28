package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;

import java.util.function.IntFunction;

/**
 * Represents the persisted state of a {@link NCircuitBreakerCall}.
 * <p>
 * This model tracks the current status of a circuit-breaker call, its thresholds,
 * retry periods, last known valid result, and any exception encountered during execution.
 * It is used internally by {@link NCircuitBreakerCallStore} and {@link NCircuitBreakerCallFactory}.
 * </p>
 *
 * <p>
 * Key responsibilities:
 * <ul>
 *     <li>Track circuit-breaker status ({@link NCircuitBreakerCall.Status})</li>
 *     <li>Maintain failure/success counters to control transitions</li>
 *     <li>Store retry timing functions for success and failure scenarios</li>
 *     <li>Hold the last successful result for fallback purposes</li>
 *     <li>Associate the original caller task ({@link NCallable}) for execution</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * NCircuitBreakerCallModel model = new NCircuitBreakerCallModel("taskId")
 *      .setFailureThreshold(5)
 *      .setSuccessThreshold(3)
 *      .setFailureRetryPeriod(attempt -> NDuration.ofSeconds(5))
 *      .setSuccessRetryPeriod(attempt -> NDuration.ofSeconds(1));
 *
 * model.setLastValidResult(fetchData());
 * model.setStatus(NCircuitBreakerCall.Status.CLOSED);
 * }</pre>
 * </p>
 *
 * <p>
 * The model is {@link Cloneable} and {@link NCopiable}, allowing safe copying
 * when updating or persisting state.
 * </p>
 *
 * @since 0.8.7
 */
public class NCircuitBreakerCallModel implements Cloneable, NCopiable {
    /**
     * Unique identifier for this circuit-breaker call.
     * <p>
     * This ID is used by {@link NCircuitBreakerCallStore} to persist and
     * retrieve the model. It distinguishes independent tasks so that each
     * can have its own thresholds, counters, and state.
     * </p>
     */
    private String id;

    /**
     * Last exception thrown by the associated {@link NCallable}.
     * <p>
     * This field is used to determine the cause of failure and can influence
     * the circuit-breaker state transitions (e.g., OPEN). It is also available
     * for logging or diagnostic purposes, or for fallback logic when using
     * {@link NCircuitBreakerCall#callOrElse(NCallable)}.
     * </p>
     */
    private Throwable throwable;

    /** Current state of the circuit breaker. Defaults to OPEN. */
    private NCircuitBreakerCall.Status status = NCircuitBreakerCall.Status.OPEN;

    /** Maximum consecutive failures before opening the circuit. */
    private int failureThreshold = 5;

    /** Required consecutive successes to close a half-open circuit. */
    private int successThreshold = 3;

    /** Current count of consecutive failures. */
    private int failureCount = 0;

    /** Current count of consecutive successes. */
    private int successCount = 0;

    /** Timestamp when the circuit was last opened. */
    private long openTimestamp = 0;

    /** Retry period after successful executions in half-open state. */
    private IntFunction<NDuration> successRetryPeriod;

    /** Retry period after failed executions. */
    private IntFunction<NDuration> failureRetryPeriod;

    /** Last successfully computed result, used for fallback. */
    private Object lastValidResult;

    /** Original caller task associated with this model. */
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

    public IntFunction<NDuration> getSuccessRetryPeriod() {
        return successRetryPeriod;
    }

    public NCircuitBreakerCallModel setSuccessRetryPeriod(IntFunction<NDuration> successRetryPeriod) {
        this.successRetryPeriod = successRetryPeriod;
        return this;
    }

    public IntFunction<NDuration> getFailureRetryPeriod() {
        return failureRetryPeriod;
    }

    public NCircuitBreakerCallModel setFailureRetryPeriod(IntFunction<NDuration> failureRetryPeriod) {
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
