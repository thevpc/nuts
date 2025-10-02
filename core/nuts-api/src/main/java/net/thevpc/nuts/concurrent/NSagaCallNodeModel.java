package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.8.7
 */
public class NSagaCallNodeModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private String name;
    private NSagaCallNodeType type;           // STEP / IF / WHILE
    private NSagaCallStep stepCall;             // for STEP nodes: NSagaStep class name
    private NSagaCallCondition stepCondition;        // for IF / WHILE nodes: NSagaCondition class name
    private List<NSagaCallNodeModel> children = new ArrayList<>();
    private List<NSagaCallNodeModel> elseIfBranches = new ArrayList<>();   // only for IF nodes
    private List<NSagaCallNodeModel> otherwiseBranch = new ArrayList<>();  // only for IF nodes
    private NSagaCallNodeStatus status = NSagaCallNodeStatus.PENDING;
    private NCompensationStrategy compensationStrategy = NCompensationStrategy.ABORT;

    public NSagaCallNodeModel() {
    }

    public String getId() {
        return id;
    }

    public NSagaCallNodeModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public NSagaCallNodeModel setName(String name) {
        this.name = name;
        return this;
    }

    public NSagaCallNodeType getType() {
        return type;
    }

    public NSagaCallNodeModel setType(NSagaCallNodeType type) {
        this.type = type;
        return this;
    }

    public NSagaCallStep getStepCall() {
        return stepCall;
    }

    public NSagaCallNodeModel setStepCall(NSagaCallStep stepCall) {
        this.stepCall = stepCall;
        return this;
    }

    public NSagaCallCondition getStepCondition() {
        return stepCondition;
    }

    public NSagaCallNodeModel setStepCondition(NSagaCallCondition stepCondition) {
        this.stepCondition = stepCondition;
        return this;
    }

    public List<NSagaCallNodeModel> getChildren() {
        return children;
    }

    public NSagaCallNodeModel setChildren(List<NSagaCallNodeModel> children) {
        this.children = children;
        return this;
    }

    public NSagaCallNodeModel addChild(NSagaCallNodeModel child) {
        this.children.add(child);
        return this;
    }

    public List<NSagaCallNodeModel> getElseIfBranches() {
        return elseIfBranches;
    }

    public NSagaCallNodeModel setElseIfBranches(List<NSagaCallNodeModel> elseIfBranches) {
        this.elseIfBranches = elseIfBranches;
        return this;
    }

    public List<NSagaCallNodeModel> getOtherwiseBranch() {
        return otherwiseBranch;
    }

    public NSagaCallNodeModel setOtherwiseBranch(List<NSagaCallNodeModel> otherwiseBranch) {
        this.otherwiseBranch = otherwiseBranch;
        return this;
    }

    public NSagaCallNodeStatus getStatus() {
        return status;
    }

    public NSagaCallNodeModel setStatus(NSagaCallNodeStatus status) {
        this.status = status;
        return this;
    }

    public NCompensationStrategy getCompensationStrategy() {
        return compensationStrategy;
    }

    public NSagaCallNodeModel setCompensationStrategy(NCompensationStrategy compensationStrategy) {
        this.compensationStrategy = compensationStrategy;
        return this;
    }

    @Override
    public NSagaCallNodeModel copy() {
        return clone();
    }

    @Override
    public NSagaCallNodeModel clone() {
        NSagaCallNodeModel copy = new NSagaCallNodeModel();
        copy.id = this.id;
        copy.name = this.name;
        copy.type = this.type;
        copy.stepCall = this.stepCall;
        copy.stepCondition = this.stepCondition;
        copy.status = this.status;
        copy.compensationStrategy = this.compensationStrategy;

        // clone children recursively
        for (NSagaCallNodeModel c : this.children) copy.children.add(c.clone());
        for (NSagaCallNodeModel c : this.elseIfBranches) copy.elseIfBranches.add(c.clone());
        for (NSagaCallNodeModel c : this.otherwiseBranch) copy.otherwiseBranch.add(c.clone());

        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NSagaNodeModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", compensationStrategy=" + compensationStrategy
        );
        if (stepCall != null) {
            sb.append(", stepCall=" + stepCall);
        }
        if (stepCondition != null) {
            sb.append(", stepCondition=" + stepCondition);
        }
        if (children != null && !children.isEmpty()) {
            sb.append(", children=" + children);
        }
        if (elseIfBranches != null && !elseIfBranches.isEmpty()) {
            sb.append(", elseIfBranches=" + elseIfBranches);
        }
        if (otherwiseBranch != null && !otherwiseBranch.isEmpty()) {
            sb.append(", otherwiseBranch=" + otherwiseBranch);
        }
        sb.append('}');
        return sb.toString();
    }
}
