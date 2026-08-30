package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;
import net.thevpc.nuts.util.NToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a node within a saga workflow model.
 * <p>
 * Each node can be of type {@link NSagaNodeType}, such as STEP, IF, or WHILE.
 * The node contains all information necessary to execute or evaluate it,
 * including its children, conditional branches, and compensation strategy.
 * <p>
 * This class serves as the **internal model** of a saga node and is used
 * by {@link NSagaModel} to define the workflow structure. It is serializable,
 * cloneable, and supports deep copying via {@link #clone()} and {@link #copy()}.
 * <p>
 * Node features include:
 * <ul>
 *     <li><b>id</b>: unique identifier of the node</li>
 *     <li><b>name</b>: human-readable name of the node</li>
 *     <li><b>type</b>: type of node ({@link NSagaNodeType})</li>
 *     <li><b>stepCall</b>: for STEP nodes, the actual {@link NSagaStep} to execute</li>
 *     <li><b>stepCondition</b>: for IF or WHILE nodes, the condition ({@link NSagaCondition}) to evaluate</li>
 *     <li><b>children</b>: list of child nodes to execute sequentially</li>
 *     <li><b>elseIfBranches</b>: list of conditional branches for IF nodes</li>
 *     <li><b>otherwiseBranch</b>: list of nodes to execute if no IF conditions match</li>
 *     <li><b>status</b>: execution status of this node ({@link NSagaNodeStatus})</li>
 *     <li><b>compensationStrategy</b>: strategy for handling failures ({@link NCompensationStrategy})</li>
 * </ul>
 * <p>
 * Cloning a node recursively clones all children and branches, preserving the
 * structure of the workflow. This allows independent execution or simulation
 * of workflow branches.
 *
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

    /**
     * N saga node model.
     *
     * @return n saga node model result
     */
    public NSagaNodeModel() {
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
    public NSagaNodeModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    public String name() {
        return name;
    }

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    public NSagaNodeModel name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Type.
     *
     * @return type result
     */
    @NGetter
    public NSagaNodeType type() {
        return type;
    }

    /**
     * Type.
     *
     * @param type type
     * @return type result
     */
    @NSetter
    public NSagaNodeModel type(NSagaNodeType type) {
        this.type = type;
        return this;
    }

    /**
     * Step call.
     *
     * @return step call result
     */
    @NGetter
    public NSagaStep stepCall() {
        return stepCall;
    }

    /**
     * Step call.
     *
     * @param stepCall step call
     * @return step call result
     */
    @NSetter
    public NSagaNodeModel stepCall(NSagaStep stepCall) {
        this.stepCall = stepCall;
        return this;
    }

    /**
     * Step condition.
     *
     * @return step condition result
     */
    @NGetter
    public NSagaCondition stepCondition() {
        return stepCondition;
    }

    /**
     * Step condition.
     *
     * @param stepCondition step condition
     * @return step condition result
     */
    @NSetter
    public NSagaNodeModel stepCondition(NSagaCondition stepCondition) {
        this.stepCondition = stepCondition;
        return this;
    }

    /**
     * Children.
     *
     * @return children result
     */
    @NGetter
    public List<NSagaNodeModel> children() {
        return children;
    }

    /**
     * Children.
     *
     * @param children children
     * @return children result
     */
    @NSetter
    public NSagaNodeModel children(List<NSagaNodeModel> children) {
        this.children = children;
        return this;
    }

    /**
     * Adds the specified child.
     *
     * @param child child
     * @return add child result
     */
    public NSagaNodeModel addChild(NSagaNodeModel child) {
        this.children.add(child);
        return this;
    }

    /**
     * Else if branches.
     *
     * @return else if branches result
     */
    @NGetter
    public List<NSagaNodeModel> elseIfBranches() {
        return elseIfBranches;
    }

    /**
     * Else if branches.
     *
     * @param elseIfBranches else if branches
     * @return else if branches result
     */
    @NSetter
    public NSagaNodeModel elseIfBranches(List<NSagaNodeModel> elseIfBranches) {
        this.elseIfBranches = elseIfBranches;
        return this;
    }

    /**
     * Otherwise branch.
     *
     * @return otherwise branch result
     */
    @NGetter
    public List<NSagaNodeModel> otherwiseBranch() {
        return otherwiseBranch;
    }

    /**
     * Otherwise branch.
     *
     * @param otherwiseBranch otherwise branch
     * @return otherwise branch result
     */
    @NSetter
    public NSagaNodeModel otherwiseBranch(List<NSagaNodeModel> otherwiseBranch) {
        this.otherwiseBranch = otherwiseBranch;
        return this;
    }

    /**
     * Status.
     *
     * @return status result
     */
    @NGetter
    public NSagaNodeStatus status() {
        return status;
    }

    /**
     * Status.
     *
     * @param status status
     * @return status result
     */
    @NSetter
    public NSagaNodeModel status(NSagaNodeStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Compensation strategy.
     *
     * @return compensation strategy result
     */
    @NGetter
    public NCompensationStrategy compensationStrategy() {
        return compensationStrategy;
    }

    /**
     * Compensation strategy.
     *
     * @param compensationStrategy compensation strategy
     * @return compensation strategy result
     */
    @NSetter
    public NSagaNodeModel compensationStrategy(NCompensationStrategy compensationStrategy) {
        this.compensationStrategy = compensationStrategy;
        return this;
    }

    @Override
    public NSagaNodeModel copy() {
        /**
         * Clone.
         *
         * @return clone result
         */
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSagaNodeModel that = (NSagaNodeModel) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type && Objects.equals(stepCall, that.stepCall) && Objects.equals(stepCondition, that.stepCondition) && Objects.equals(children, that.children) && Objects.equals(elseIfBranches, that.elseIfBranches) && Objects.equals(otherwiseBranch, that.otherwiseBranch) && status == that.status && compensationStrategy == that.compensationStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, stepCall, stepCondition, children, elseIfBranches, otherwiseBranch, status, compensationStrategy);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("id", id)
                .add("name", name)
                .add("type", type)
                .add("status", status)
                .add("compensationStrategy", compensationStrategy)
                .add("stepCall", stepCall)
                .add("stepCondition", stepCondition)
                .add("children", children)
                .add("elseIfBranches", elseIfBranches)
                .add("otherwiseBranch", otherwiseBranch)
                .build();
    }

}
