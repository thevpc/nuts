package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;

/**
 * @since 0.8.7
 */
public class NSagaCallModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private NSagaCallNodeModel node;
    private NSagaCallContextModel context = new NSagaCallContextModel();

    public NSagaCallModel() {
    }

    public String getId() {
        return id;
    }

    public NSagaCallModel setId(String id) {
        this.id = id;
        return this;
    }

    public NSagaCallNodeModel getNode() {
        return node;
    }

    public NSagaCallModel setNode(NSagaCallNodeModel node) {
        this.node = node;
        return this;
    }

    public NSagaCallContextModel getContext() {
        return context;
    }

    public NSagaCallModel setContext(NSagaCallContextModel context) {
        this.context = context;
        return this;
    }

    @Override
    public NSagaCallModel copy() {
        return clone();
    }

    @Override
    public NSagaCallModel clone() {
        NSagaCallModel copy = new NSagaCallModel();
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
