package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;
import net.thevpc.nuts.util.NToStringBuilder;

import java.io.Serializable;
import java.util.*;

/**
 * Internal model representing the state of a saga execution.
 * <p>
 * This class serves as the data container underlying a {@link NSagaContext}.
 * It is not itself a {@link NSagaContext} implementation, but rather stores
 * all information that the context uses to track a saga's progress, status,
 * and variables.
 * <p>
 * The model includes:
 * <ul>
 *     <li>Execution stacks: {@code stackStepId}, {@code stackStepGroup}, {@code stackStepIndex} for tracking nested steps and iteration indices.</li>
 *     <li>Variable storage: a {@code values} map to hold key-value pairs shared across steps.</li>
 *     <li>Compensation tracking: {@code stepsToCompensate} stack for steps needing rollback in case of failure.</li>
 *     <li>Status: {@link NSagaStatus} indicating the current state of the saga (RUNNING, FINISHED, FAILED, COMPENSATING).</li>
 *     <li>Timing: {@code startTime} and {@code endTime} to track execution duration.</li>
 *     <li>Failure details: the first failed step's ID, name, and exception.</li>
 *     <li>Last result: stores the output of the most recently executed step.</li>
 * </ul>
 * <p>
 * This class implements {@link Serializable} for persistence and {@link Cloneable}
 * for creating shallow copies of the saga state, e.g., for branching or retrying steps.
 *
 * @since 0.8.7
 */
public class NSagaContextModel implements Serializable, Cloneable {
    private Deque<String> stackStepId = new ArrayDeque<>(); // IDs of nodes whose children are being iterated
    private Deque<String> stackStepGroup = new ArrayDeque<>(); // IDs of nodes whose children are being iterated
    private Deque<Integer> stackStepIndex = new ArrayDeque<>(); // index of next child to execute


    private Map<String, Object> values = new HashMap<>();
    private Deque<String> stepsToCompensate = new ArrayDeque<>();  // node IDs that must be compensated on failure
    private NSagaStatus status;          // RUNNING, FINISHED, FAILED, COMPENSATING
    // optional: timestamps
    private long startTime;
    private long endTime;
    private transient Throwable firstFailStepError;
    private String firstFailStepErrorMessage;
    private String firstFailStepId;
    private String firstFailStepName;

    private Object lastResult;

    /**
     * N saga context model.
     *
     * @return n saga context model result
     */
    public NSagaContextModel() {
    }

    /**
     * First fail step error message.
     *
     * @return first fail step error message
     * @since 0.8.8
     */
    @NGetter
    public String firstFailStepErrorMessage() {
        return firstFailStepErrorMessage;
    }

    /**
     * First fail step error message.
     *
     * @param message error message
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    public NSagaContextModel firstFailStepErrorMessage(String message) {
        this.firstFailStepErrorMessage = message;
        return this;
    }

    /**
     * First fail step id.
     *
     * @return first fail step id result
     */
    @NGetter
    public String firstFailStepId() {
        return firstFailStepId;
    }

    /**
     * First fail step name.
     *
     * @return first fail step name result
     */
    @NGetter
    public String firstFailStepName() {
        return firstFailStepName;
    }

    /**
     * First fail step name.
     *
     * @param firstFailStepName first fail step name
     * @return first fail step name result
     */
    @NSetter
    public NSagaContextModel firstFailStepName(String firstFailStepName) {
        this.firstFailStepName = firstFailStepName;
        return this;
    }

    /**
     * First fail step id.
     *
     * @param firstFailStepId first fail step id
     * @return first fail step id result
     */
    @NSetter
    public NSagaContextModel firstFailStepId(String firstFailStepId) {
        this.firstFailStepId = firstFailStepId;
        return this;
    }

    /**
     * First fail step error.
     *
     * @return first fail step error result
     */
    @NGetter
    public Throwable firstFailStepError() {
        return firstFailStepError;
    }

    /**
     * First fail step error.
     *
     * @param firstFailStepThrowable first fail step throwable
     * @return first fail step error result
     */
    @NSetter
    public NSagaContextModel firstFailStepError(Throwable firstFailStepThrowable) {
        this.firstFailStepError = firstFailStepThrowable;
        if (firstFailStepThrowable != null) {
            this.firstFailStepErrorMessage = firstFailStepThrowable.getMessage();
        }
        return this;
    }

    /**
     * Last result.
     *
     * @return last result result
     */
    @NGetter
    public Object lastResult() {
        return lastResult;
    }

    /**
     * Last result.
     *
     * @param lastResult last result
     * @return last result result
     */
    @NSetter
    public NSagaContextModel lastResult(Object lastResult) {
        this.lastResult = lastResult;
        return this;
    }

    /**
     * Stack step id.
     *
     * @return stack step id result
     */
    @NGetter
    public Deque<String> stackStepId() {
        return stackStepId;
    }

    /**
     * Stack step id.
     *
     * @param stackStepId stack step id
     * @return stack step id result
     */
    @NSetter
    public NSagaContextModel stackStepId(Deque<String> stackStepId) {
        this.stackStepId = stackStepId;
        return this;
    }

    /**
     * Stack step group.
     *
     * @return stack step group result
     */
    @NGetter
    public Deque<String> stackStepGroup() {
        return stackStepGroup;
    }

    /**
     * Stack step group.
     *
     * @param stackStepGroup stack step group
     * @return stack step group result
     */
    @NSetter
    public NSagaContextModel stackStepGroup(Deque<String> stackStepGroup) {
        this.stackStepGroup = stackStepGroup;
        return this;
    }

    /**
     * Stack step index.
     *
     * @return stack step index result
     */
    @NGetter
    public Deque<Integer> stackStepIndex() {
        return stackStepIndex;
    }

    /**
     * Stack step index.
     *
     * @param stackStepIndex stack step index
     * @return stack step index result
     */
    @NSetter
    public NSagaContextModel stackStepIndex(Deque<Integer> stackStepIndex) {
        this.stackStepIndex = stackStepIndex;
        return this;
    }

    /**
     * Status.
     *
     * @return status result
     */
    @NGetter
    public NSagaStatus status() {
        return status;
    }

    /**
     * Status.
     *
     * @param status status
     * @return status result
     */
    @NSetter
    public NSagaContextModel status(NSagaStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Start time.
     *
     * @return start time result
     */
    @NGetter
    public long startTime() {
        return startTime;
    }

    /**
     * Start time.
     *
     * @param startTime start time
     * @return start time result
     */
    @NSetter
    public NSagaContextModel startTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * End time.
     *
     * @return end time result
     */
    @NGetter
    public long endTime() {
        return endTime;
    }

    /**
     * End time.
     *
     * @param endTime end time
     * @return end time result
     */
    @NSetter
    public NSagaContextModel endTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Values.
     *
     * @return values result
     */
    @NGetter
    public Map<String, Object> values() {
        return values;
    }

    /**
     * Values.
     *
     * @param values values
     * @return values result
     */
    @NSetter
    public NSagaContextModel values(Map<String, Object> values) {
        this.values = values;
        return this;
    }

    /**
     * Put.
     *
     * @param key key
     * @param value value
     * @return put result
     */
    public NSagaContextModel put(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    public Object get(String key) {
        return this.values.get(key);
    }

    /**
     * Steps to compensate.
     *
     * @return steps to compensate result
     */
    @NGetter
    public Deque<String> stepsToCompensate() {
        return stepsToCompensate;
    }

    /**
     * Steps to compensate.
     *
     * @param stepsToCompensate steps to compensate
     * @return steps to compensate result
     */
    @NSetter
    public NSagaContextModel stepsToCompensate(Deque<String> stepsToCompensate) {
        this.stepsToCompensate = stepsToCompensate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSagaContextModel that = (NSagaContextModel) o;
        return startTime == that.startTime && endTime == that.endTime && Objects.equals(stackStepId, that.stackStepId) && Objects.equals(stackStepGroup, that.stackStepGroup) && Objects.equals(stackStepIndex, that.stackStepIndex) && Objects.equals(values, that.values) && Objects.equals(stepsToCompensate, that.stepsToCompensate) && status == that.status && Objects.equals(firstFailStepError, that.firstFailStepError) && Objects.equals(firstFailStepErrorMessage, that.firstFailStepErrorMessage) && Objects.equals(firstFailStepId, that.firstFailStepId) && Objects.equals(firstFailStepName, that.firstFailStepName) && Objects.equals(lastResult, that.lastResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackStepId, stackStepGroup, stackStepIndex, values, stepsToCompensate, status, startTime, endTime, firstFailStepError, firstFailStepErrorMessage, firstFailStepId, firstFailStepName, lastResult);
    }

    @Override
    public NSagaContextModel clone() {
        NSagaContextModel copy = new NSagaContextModel();
        copy.values.putAll(this.values); // shallow copy of values
        copy.stepsToCompensate.addAll(this.stepsToCompensate);
        copy.stackStepId.addAll(this.stackStepId);
        copy.stackStepGroup.addAll(this.stackStepGroup);
        copy.stackStepIndex.addAll(this.stackStepIndex);
        copy.status = status;
        copy.startTime = startTime;
        copy.endTime = endTime;
        copy.firstFailStepError = firstFailStepError;
        copy.firstFailStepErrorMessage = firstFailStepErrorMessage;
        copy.firstFailStepId = firstFailStepId;
        copy.firstFailStepName = firstFailStepName;
        copy.lastResult = lastResult;
        return copy;
    }


    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("status", status)
                .add("stackStepId", stackStepId)
                .add("stackStepGroup", stackStepGroup)
                .add("stackStepIndex", stackStepIndex)
                .add("values", values)
                .add("stepsToCompensate", stepsToCompensate)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .add("firstFailStepId", firstFailStepId)
                .add("endTime", endTime)
                .add("firstFailStepName", firstFailStepName)
                .add("firstFailStepError", firstFailStepError)
                .add("lastResult", lastResult)
                .build();
    }
}
