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
public class NWorkBalancerModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private Map<String, NElement> options = new HashMap<>();
    private String strategy;
    private List<NWorkBalancerWorkerModel> workers;
    private NWorkBalancerModelContext context = new NWorkBalancerModelContext();

    public NWorkBalancerModel() {
    }

    public String getStrategy() {
        return strategy;
    }

    public NWorkBalancerModel setStrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    public String getId() {
        return id;
    }

    public NWorkBalancerModel setId(String id) {
        this.id = id;
        return this;
    }

    public Map<String, NElement> getOptions() {
        return options;
    }

    public NWorkBalancerModel setOptions(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    public List<NWorkBalancerWorkerModel> getWorkers() {
        return workers;
    }

    public NWorkBalancerModel setWorkers(List<NWorkBalancerWorkerModel> workers) {
        this.workers = workers;
        return this;
    }

    public NWorkBalancerModelContext getContext() {
        return context;
    }

    public NWorkBalancerModel setContext(NWorkBalancerModelContext context) {
        this.context = context;
        return this;
    }

    @Override
    public NWorkBalancerModel copy() {
        return clone();
    }

    @Override
    public NWorkBalancerModel clone() {
        NWorkBalancerModel copy = null;
        try {
            copy = (NWorkBalancerModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        copy.options = this.options == null ? null : new HashMap<>(this.options);
        copy.workers = this.workers == null ? null : workers.stream().map(x -> x.copy()).collect(Collectors.toList());
        copy.context = this.context == null ? null : this.context.clone();
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
