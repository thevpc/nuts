package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

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
    private Throwable firstFailStepError;
    private String firstFailStepId;
    private String firstFailStepName;

    private Object lastResult;

    public NSagaContextModel() {
    }

    @NGetter
    public String firstFailStepId() {
        return firstFailStepId;
    }

    @NGetter
    public String firstFailStepName() {
        return firstFailStepName;
    }

    @NSetter
    public NSagaContextModel firstFailStepName(String firstFailStepName) {
        this.firstFailStepName = firstFailStepName;
        return this;
    }

    @NSetter
    public NSagaContextModel firstFailStepId(String firstFailStepId) {
        this.firstFailStepId = firstFailStepId;
        return this;
    }

    @NGetter
    public Throwable firstFailStepError() {
        return firstFailStepError;
    }

    @NSetter
    public NSagaContextModel firstFailStepError(Throwable firstFailStepThrowable) {
        this.firstFailStepError = firstFailStepThrowable;
        return this;
    }

    @NGetter
    public Object lastResult() {
        return lastResult;
    }

    @NSetter
    public NSagaContextModel lastResult(Object lastResult) {
        this.lastResult = lastResult;
        return this;
    }

    @NGetter
    public Deque<String> stackStepId() {
        return stackStepId;
    }

    @NSetter
    public NSagaContextModel stackStepId(Deque<String> stackStepId) {
        this.stackStepId = stackStepId;
        return this;
    }

    @NGetter
    public Deque<String> stackStepGroup() {
        return stackStepGroup;
    }

    @NSetter
    public NSagaContextModel stackStepGroup(Deque<String> stackStepGroup) {
        this.stackStepGroup = stackStepGroup;
        return this;
    }

    @NGetter
    public Deque<Integer> stackStepIndex() {
        return stackStepIndex;
    }

    @NSetter
    public NSagaContextModel stackStepIndex(Deque<Integer> stackStepIndex) {
        this.stackStepIndex = stackStepIndex;
        return this;
    }

    @NGetter
    public NSagaStatus status() {
        return status;
    }

    @NSetter
    public NSagaContextModel status(NSagaStatus status) {
        this.status = status;
        return this;
    }

    @NGetter
    public long startTime() {
        return startTime;
    }

    @NSetter
    public NSagaContextModel startTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    @NGetter
    public long endTime() {
        return endTime;
    }

    @NSetter
    public NSagaContextModel endTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    @NGetter
    public Map<String, Object> values() {
        return values;
    }

    @NSetter
    public NSagaContextModel values(Map<String, Object> values) {
        this.values = values;
        return this;
    }

    public NSagaContextModel put(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    public Object get(String key) {
        return this.values.get(key);
    }

    @NGetter
    public Deque<String> stepsToCompensate() {
        return stepsToCompensate;
    }

    @NSetter
    public NSagaContextModel stepsToCompensate(Deque<String> stepsToCompensate) {
        this.stepsToCompensate = stepsToCompensate;
        return this;
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
        copy.lastResult = lastResult;
        return copy;
    }

    @Override
    public String toString() {
        return "NSagaContextModel{" +
                " status=" + status +
                ", stackStepId=" + stackStepId +
                ", stackStepGroup=" + stackStepGroup +
                ", stackStepIndex=" + stackStepIndex +
                ", values=" + values +
                ", stepsToCompensate=" + stepsToCompensate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", firstFailStepId=" + firstFailStepId +
                ", firstFailStepName=" + firstFailStepName +
                ", firstFailStepThrowable=" + firstFailStepError +
                ", lastResult=" + lastResult +
                '}';
    }
}
