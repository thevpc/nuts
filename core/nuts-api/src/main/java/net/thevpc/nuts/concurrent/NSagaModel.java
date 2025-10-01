package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;

/**
 * @since 0.8.7
 */
public class NSagaModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private NSagaNodeModel node;
    private NSagaContextModel context = new NSagaContextModel();

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
