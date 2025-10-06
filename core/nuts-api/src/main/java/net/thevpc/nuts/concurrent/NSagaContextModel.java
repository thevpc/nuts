package net.thevpc.nuts.concurrent;

import java.io.Serializable;
import java.util.*;

public class NSagaContextModel implements Serializable , Cloneable{
    private Deque<String> stackStepId = new ArrayDeque<>(); // IDs of nodes whose children are being iterated
    private Deque<String> stackStepGroup = new ArrayDeque<>(); // IDs of nodes whose children are being iterated
    private Deque<Integer> stackStepIndex = new ArrayDeque<>(); // index of next child to execute


    private Map<String, Object> values = new HashMap<>();
    private Deque<String> stepsToCompensate = new ArrayDeque<>();  // node IDs that must be compensated on failure
    private NSagaStatus status;          // RUNNING, FINISHED, FAILED, COMPENSATING
    // optional: timestamps
    private long startTime;
    private long endTime;
    private Throwable firstFailStepThrowable;
    private String firstFailStepId;
    private String firstFailStepName;

    private Object lastResult;

    public NSagaContextModel() {
    }

    public String getFirstFailStepId() {
        return firstFailStepId;
    }

    public String getFirstFailStepName() {
        return firstFailStepName;
    }

    public NSagaContextModel setFirstFailStepName(String firstFailStepName) {
        this.firstFailStepName = firstFailStepName;
        return this;
    }

    public NSagaContextModel setFirstFailStepId(String firstFailStepId) {
        this.firstFailStepId = firstFailStepId;
        return this;
    }

    public Throwable getFirstFailStepThrowable() {
        return firstFailStepThrowable;
    }

    public NSagaContextModel setFirstFailStepThrowable(Throwable firstFailStepThrowable) {
        this.firstFailStepThrowable = firstFailStepThrowable;
        return this;
    }

    public Object getLastResult() {
        return lastResult;
    }

    public NSagaContextModel setLastResult(Object lastResult) {
        this.lastResult = lastResult;
        return this;
    }

    public Deque<String> getStackStepId() {
        return stackStepId;
    }

    public NSagaContextModel setStackStepId(Deque<String> stackStepId) {
        this.stackStepId = stackStepId;
        return this;
    }

    public Deque<String> getStackStepGroup() {
        return stackStepGroup;
    }

    public NSagaContextModel setStackStepGroup(Deque<String> stackStepGroup) {
        this.stackStepGroup = stackStepGroup;
        return this;
    }

    public Deque<Integer> getStackStepIndex() {
        return stackStepIndex;
    }

    public NSagaContextModel setStackStepIndex(Deque<Integer> stackStepIndex) {
        this.stackStepIndex = stackStepIndex;
        return this;
    }

    public NSagaStatus getStatus() {
        return status;
    }

    public NSagaContextModel setStatus(NSagaStatus status) {
        this.status = status;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public NSagaContextModel setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEndTime() {
        return endTime;
    }

    public NSagaContextModel setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public NSagaContextModel setValues(Map<String, Object> values) {
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

    public Deque<String> getStepsToCompensate() {
        return stepsToCompensate;
    }

    public NSagaContextModel setStepsToCompensate(Deque<String> stepsToCompensate) {
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
        copy.status=status;
        copy.startTime=startTime;
        copy.endTime=endTime;
        copy.firstFailStepThrowable = firstFailStepThrowable;
        copy.lastResult=lastResult;
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
                ", firstFailStepThrowable=" + firstFailStepThrowable +
                ", lastResult=" + lastResult +
                '}';
    }
}
