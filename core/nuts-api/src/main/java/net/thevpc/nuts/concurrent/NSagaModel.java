package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;
import net.thevpc.nuts.util.NToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

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
    @NSetter
    public NSagaModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Node.
     *
     * @return node result
     */
    @NGetter
    public NSagaNodeModel node() {
        return node;
    }

    /**
     * Node.
     *
     * @param node node
     * @return node result
     */
    @NSetter
    public NSagaModel node(NSagaNodeModel node) {
        this.node = node;
        return this;
    }

    /**
     * Context.
     *
     * @return context result
     */
    @NGetter
    public NSagaContextModel context() {
        return context;
    }

    /**
     * Context.
     *
     * @param context context
     * @return context result
     */
    @NSetter
    public NSagaModel context(NSagaContextModel context) {
        this.context = context;
        return this;
    }

    @Override
    public NSagaModel copy() {
        /**
         * Clone.
         *
         * @return clone result
         */
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSagaModel that = (NSagaModel) o;
        return Objects.equals(id, that.id) && Objects.equals(node, that.node) && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, node, context);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("id", id)
                .add("context", context)
                .add("node", node)
                .build();
    }

}
