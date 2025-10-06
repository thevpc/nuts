package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.8.7
 */
public class NSagaNodeModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private String name;
    private NSagaNodeType type;           // STEP / IF / WHILE
    private NSagaStep stepCall;             // for STEP nodes: NSagaStep class name
    private NSagaCondition stepCondition;        // for IF / WHILE nodes: NSagaCondition class name
    private List<NSagaNodeModel> children = new ArrayList<>();
    private List<NSagaNodeModel> elseIfBranches = new ArrayList<>();   // only for IF nodes
    private List<NSagaNodeModel> otherwiseBranch = new ArrayList<>();  // only for IF nodes
    private NSagaNodeStatus status = NSagaNodeStatus.PENDING;
    private NCompensationStrategy compensationStrategy = NCompensationStrategy.ABORT;

    public NSagaNodeModel() {
    }

    public String getId() {
        return id;
    }

    public NSagaNodeModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public NSagaNodeModel setName(String name) {
        this.name = name;
        return this;
    }

    public NSagaNodeType getType() {
        return type;
    }

    public NSagaNodeModel setType(NSagaNodeType type) {
        this.type = type;
        return this;
    }

    public NSagaStep getStepCall() {
        return stepCall;
    }

    public NSagaNodeModel setStepCall(NSagaStep stepCall) {
        this.stepCall = stepCall;
        return this;
    }

    public NSagaCondition getStepCondition() {
        return stepCondition;
    }

    public NSagaNodeModel setStepCondition(NSagaCondition stepCondition) {
        this.stepCondition = stepCondition;
        return this;
    }

    public List<NSagaNodeModel> getChildren() {
        return children;
    }

    public NSagaNodeModel setChildren(List<NSagaNodeModel> children) {
        this.children = children;
        return this;
    }

    public NSagaNodeModel addChild(NSagaNodeModel child) {
        this.children.add(child);
        return this;
    }

    public List<NSagaNodeModel> getElseIfBranches() {
        return elseIfBranches;
    }

    public NSagaNodeModel setElseIfBranches(List<NSagaNodeModel> elseIfBranches) {
        this.elseIfBranches = elseIfBranches;
        return this;
    }

    public List<NSagaNodeModel> getOtherwiseBranch() {
        return otherwiseBranch;
    }

    public NSagaNodeModel setOtherwiseBranch(List<NSagaNodeModel> otherwiseBranch) {
        this.otherwiseBranch = otherwiseBranch;
        return this;
    }

    public NSagaNodeStatus getStatus() {
        return status;
    }

    public NSagaNodeModel setStatus(NSagaNodeStatus status) {
        this.status = status;
        return this;
    }

    public NCompensationStrategy getCompensationStrategy() {
        return compensationStrategy;
    }

    public NSagaNodeModel setCompensationStrategy(NCompensationStrategy compensationStrategy) {
        this.compensationStrategy = compensationStrategy;
        return this;
    }

    @Override
    public NSagaNodeModel copy() {
        return clone();
    }

    @Override
    public NSagaNodeModel clone() {
        NSagaNodeModel copy = new NSagaNodeModel();
        copy.id = this.id;
        copy.name = this.name;
        copy.type = this.type;
        copy.stepCall = this.stepCall;
        copy.stepCondition = this.stepCondition;
        copy.status = this.status;
        copy.compensationStrategy = this.compensationStrategy;

        // clone children recursively
        for (NSagaNodeModel c : this.children) copy.children.add(c.clone());
        for (NSagaNodeModel c : this.elseIfBranches) copy.elseIfBranches.add(c.clone());
        for (NSagaNodeModel c : this.otherwiseBranch) copy.otherwiseBranch.add(c.clone());

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
