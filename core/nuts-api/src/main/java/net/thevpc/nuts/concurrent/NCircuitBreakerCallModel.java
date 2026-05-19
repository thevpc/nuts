package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;

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
    private Throwable error;

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

    public NCircuitBreakerCallModel(String id) {
        this.id = id;
    }

    @NGetter
    public NCircuitBreakerCall.Status getStatus() {
        return status;
    }

    public NCircuitBreakerCallModel setStatus(NCircuitBreakerCall.Status status) {
        this.status = status;
        return this;
    }


    @NGetter
    public NCallable<?> getCaller() {
        return caller;
    }

    public NCircuitBreakerCallModel setCaller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }


    @NGetter
    public Object getError() {
        return error;
    }



    public NCircuitBreakerCallModel setError(Throwable error) {
        this.error = error;
        return this;
    }


    @NGetter
    public String id() {
        return id;
    }

    public NCircuitBreakerCallModel id(String id) {
        this.id = id;
        return this;
    }

    @NGetter
    public int failureThreshold() {
        return failureThreshold;
    }

    public NCircuitBreakerCallModel failureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
        return this;
    }

    @NGetter
    public int successThreshold() {
        return successThreshold;
    }

    public NCircuitBreakerCallModel successThreshold(int successThreshold) {
        this.successThreshold = successThreshold;
        return this;
    }

    @NGetter
    public int failureCount() {
        return failureCount;
    }

    public NCircuitBreakerCallModel failureCount(int failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    public int successCount() {
        return successCount;
    }

    @NGetter
    public NCircuitBreakerCallModel successCount(int successCount) {
        this.successCount = successCount;
        return this;
    }

    public long openTimestamp() {
        return openTimestamp;
    }

    @NGetter
    public NCircuitBreakerCallModel openTimestamp(long openTimestamp) {
        this.openTimestamp = openTimestamp;
        return this;
    }

    public IntFunction<NDuration> successRetryPeriod() {
        return successRetryPeriod;
    }

    @NGetter
    public NCircuitBreakerCallModel successRetryPeriod(IntFunction<NDuration> successRetryPeriod) {
        this.successRetryPeriod = successRetryPeriod;
        return this;
    }

    public IntFunction<NDuration> failureRetryPeriod() {
        return failureRetryPeriod;
    }

    @NGetter
    public NCircuitBreakerCallModel failureRetryPeriod(IntFunction<NDuration> failureRetryPeriod) {
        this.failureRetryPeriod = failureRetryPeriod;
        return this;
    }

    public Object lastValidResult() {
        return lastValidResult;
    }

    @NGetter
    public NCircuitBreakerCallModel lastValidResult(Object lastValidResult) {
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
