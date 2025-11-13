package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;

/**
 * Represents the complete model of a saga execution.
 * <p>
 * The {@code NSagaModel} encapsulates:
 * <ul>
 *     <li>The saga identifier ({@code id})</li>
 *     <li>The root saga node tree ({@link NSagaNodeModel}) describing the steps and workflow</li>
 *     <li>The saga execution context ({@link NSagaContextModel}) holding runtime state, variables, and status</li>
 * </ul>
 * <p>
 * This model serves as a snapshot of a saga's definition and current state. It
 * can be cloned or copied, making it suitable for branching, retrying, or
 * persisting saga executions.
 * <p>
 * Implements:
 * <ul>
 *     <li>{@link Serializable} – for persistence</li>
 *     <li>{@link Cloneable} – for shallow cloning</li>
 *     <li>{@link NCopiable} – for consistent copy semantics</li>
 * </ul>
 *
 * @since 0.8.7
 */
public class NSagaModel implements Serializable, Cloneable, NCopiable {
    /** The unique identifier of this saga execution */
    private String id;
    /** The root node representing the saga's workflow structure */
    private NSagaNodeModel node;
    /** The execution context containing runtime variables and status */
    private NSagaContextModel context = new NSagaContextModel();

    /**
     * Default constructor.
     */
    public NSagaModel() {
    }

    public String getId() {
        return id;
    }

    public NSagaModel setId(String id) {
        this.id = id;
        return this;
    }

    public NSagaNodeModel getNode() {
        return node;
    }

    public NSagaModel setNode(NSagaNodeModel node) {
        this.node = node;
        return this;
    }

    public NSagaContextModel getContext() {
        return context;
    }

    public NSagaModel setContext(NSagaContextModel context) {
        this.context = context;
        return this;
    }

    @Override
    public NSagaModel copy() {
        return clone();
    }

    @Override
    public NSagaModel clone() {
        NSagaModel copy = new NSagaModel();
        copy.id = this.id;
        copy.context = this.context.clone();
        copy.node =this.node.clone();
        return copy;
    }

    @Override
    public String toString() {
        return "NSagaModel{" +
                "id='" + id + '\'' +
                ", context=" + context +
                ", nodes=" + node +
                '}';
    }
}
