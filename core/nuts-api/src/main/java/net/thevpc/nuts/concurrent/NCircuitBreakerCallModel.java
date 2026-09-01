package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NToStringBuilder;
import net.thevpc.nuts.util.NUnexpectedException;

import java.util.Objects;
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

    /**
     * N circuit breaker call model.
     *
     * @return n circuit breaker call model result
     */
    public NCircuitBreakerCallModel() {
    }

    /**
     * N circuit breaker call model.
     *
     * @param id id
     * @return n circuit breaker call model result
     */
    public NCircuitBreakerCallModel(String id) {
        this.id = id;
    }

    /**
     * Status.
     *
     * @return status result
     */
    @NGetter
    public NCircuitBreakerCall.Status status() {
        return status;
    }

    /**
     * Status.
     *
     * @param status status
     * @return status result
     */
    public NCircuitBreakerCallModel status(NCircuitBreakerCall.Status status) {
        this.status = status;
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
    public NCircuitBreakerCallModel caller(NCallable<?> caller) {
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
     * Error.
     *
     * @param error error
     * @return error result
     */
    public NCircuitBreakerCallModel error(Throwable error) {
        this.error = error;
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
    public NCircuitBreakerCallModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Failure threshold.
     *
     * @return failure threshold result
     */
    @NGetter
    public int failureThreshold() {
        return failureThreshold;
    }

    /**
     * Failure threshold.
     *
     * @param failureThreshold failure threshold
     * @return failure threshold result
     */
    public NCircuitBreakerCallModel failureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
        return this;
    }

    /**
     * Success threshold.
     *
     * @return success threshold result
     */
    @NGetter
    public int successThreshold() {
        return successThreshold;
    }

    /**
     * Success threshold.
     *
     * @param successThreshold success threshold
     * @return success threshold result
     */
    public NCircuitBreakerCallModel successThreshold(int successThreshold) {
        this.successThreshold = successThreshold;
        return this;
    }

    /**
     * Failure count.
     *
     * @return failure count result
     */
    @NGetter
    public int failureCount() {
        return failureCount;
    }

    /**
     * Failure count.
     *
     * @param failureCount failure count
     * @return failure count result
     */
    public NCircuitBreakerCallModel failureCount(int failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    /**
     * Success count.
     *
     * @return success count result
     */
    public int successCount() {
        return successCount;
    }

    /**
     * Success count.
     *
     * @param successCount success count
     * @return success count result
     */
    @NGetter
    public NCircuitBreakerCallModel successCount(int successCount) {
        this.successCount = successCount;
        return this;
    }

    /**
     * Open timestamp.
     *
     * @return open timestamp result
     */
    public long openTimestamp() {
        return openTimestamp;
    }

    /**
     * Open timestamp.
     *
     * @param openTimestamp open timestamp
     * @return open timestamp result
     */
    @NGetter
    public NCircuitBreakerCallModel openTimestamp(long openTimestamp) {
        this.openTimestamp = openTimestamp;
        return this;
    }

    /**
     * Success retry period.
     *
     * @return success retry period result
     */
    public IntFunction<NDuration> successRetryPeriod() {
        return successRetryPeriod;
    }

    /**
     * Success retry period.
     *
     * @param successRetryPeriod success retry period
     * @return success retry period result
     */
    @NGetter
    public NCircuitBreakerCallModel successRetryPeriod(IntFunction<NDuration> successRetryPeriod) {
        this.successRetryPeriod = successRetryPeriod;
        return this;
    }

    /**
     * Failure retry period.
     *
     * @return failure retry period result
     */
    public IntFunction<NDuration> failureRetryPeriod() {
        return failureRetryPeriod;
    }

    /**
     * Failure retry period.
     *
     * @param failureRetryPeriod failure retry period
     * @return failure retry period result
     */
    @NGetter
    public NCircuitBreakerCallModel failureRetryPeriod(IntFunction<NDuration> failureRetryPeriod) {
        this.failureRetryPeriod = failureRetryPeriod;
        return this;
    }

    /**
     * Last valid result.
     *
     * @return last valid result result
     */
    public Object lastValidResult() {
        return lastValidResult;
    }

    /**
     * Last valid result.
     *
     * @param lastValidResult last valid result
     * @return last valid result result
     */
    @NGetter
    public NCircuitBreakerCallModel lastValidResult(Object lastValidResult) {
        this.lastValidResult = lastValidResult;
        return this;
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NCircuitBreakerCallModel copy(){
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
    protected NCircuitBreakerCallModel clone(){
        try {
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NCircuitBreakerCallModel) super.clone();
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
        NCircuitBreakerCallModel that = (NCircuitBreakerCallModel) o;
        return failureThreshold == that.failureThreshold && successThreshold == that.successThreshold && failureCount == that.failureCount && successCount == that.successCount && openTimestamp == that.openTimestamp && Objects.equals(id, that.id) && Objects.equals(error, that.error) && status == that.status && Objects.equals(successRetryPeriod, that.successRetryPeriod) && Objects.equals(failureRetryPeriod, that.failureRetryPeriod) && Objects.equals(lastValidResult, that.lastValidResult) && Objects.equals(caller, that.caller);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, error, status, failureThreshold, successThreshold, failureCount, successCount, openTimestamp, successRetryPeriod, failureRetryPeriod, lastValidResult, caller);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("id", id)
                .add("error", error)
                .add("status", status)
                .add("failureThreshold", failureThreshold)
                .add("successThreshold", successThreshold)
                .add("failureCount", failureCount)
                .add("successCount", successCount)
                .add("openTimestamp", openTimestamp)
                .add("successRetryPeriod", successRetryPeriod)
                .add("failureRetryPeriod", failureRetryPeriod)
                .add("lastValidResult", lastValidResult)
                .add("caller", caller)
                .build();
    }
}
