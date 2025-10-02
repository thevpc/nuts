package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @since 0.8.7
 */
public class NWorkBalancerCallModelContext implements Serializable, Cloneable, NCopiable {
    private Map<String, NElement> globalValues;
    private Map<String, Map<String, NElement>> workerValues;

    private List<NWorkBalancerCallWorkerModel> zorkers;
    private NSagaCallContextModel context = new NSagaCallContextModel();

    public NWorkBalancerCallModelContext() {
    }

    public String getId() {
        return id;
    }

    public NWorkBalancerCallModelContext setId(String id) {
        this.id = id;
        return this;
    }

    public NSagaCallNodeModel getNode() {
        return node;
    }

    public NWorkBalancerCallModelContext setNode(NSagaCallNodeModel node) {
        this.node = node;
        return this;
    }

    public NSagaCallContextModel getContext() {
        return context;
    }

    public NWorkBalancerCallModelContext setContext(NSagaCallContextModel context) {
        this.context = context;
        return this;
    }

    @Override
    public NWorkBalancerCallModelContext copy() {
        return clone();
    }

    @Override
    public NWorkBalancerCallModelContext clone() {
        NWorkBalancerCallModelContext copy = new NWorkBalancerCallModelContext();
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
