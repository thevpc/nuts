package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since 0.8.7
 */
public class NWorkBalancerCallModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private Map<String, NElement> options=new HashMap<>();

    private List<NWorkBalancerCallWorkerModel> workers;
    private NWorkBalancerCallModelContext context = new NWorkBalancerCallModelContext();

    public NWorkBalancerCallModel() {
    }

    public String getId() {
        return id;
    }

    public NWorkBalancerCallModel setId(String id) {
        this.id = id;
        return this;
    }


    public NWorkBalancerCallModelContext getContext() {
        return context;
    }

    public NWorkBalancerCallModel setContext(NWorkBalancerCallModelContext context) {
        this.context = context;
        return this;
    }

    @Override
    public NWorkBalancerCallModel copy() {
        return clone();
    }

    @Override
    public NWorkBalancerCallModel clone() {
        NWorkBalancerCallModel copy = new NWorkBalancerCallModel();
        copy.id = this.id;
        copy.options = this.options==null?null:new HashMap<>(this.options);
        copy.workers = this.workers==null?null:workers.stream().map(x->x.copy()).collect(Collectors.toList());
        copy.context = this.context.clone();
        return copy;
    }

    @Override
    public String toString() {
        return "NSagaModel{" +
                "id='" + id + '\'' +
                ", context=" + context +
                ", options=" + options +
                ", workers=" + workers +
                '}';
    }
}
