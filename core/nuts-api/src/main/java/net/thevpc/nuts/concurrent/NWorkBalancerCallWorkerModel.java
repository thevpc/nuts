package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.8.7
 */
public class NWorkBalancerCallWorkerModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private NCallable<?> callable;
    private Map<String, NElement> options;

    public NWorkBalancerCallWorkerModel() {
    }

    public String getId() {
        return id;
    }

    public NWorkBalancerCallWorkerModel setId(String id) {
        this.id = id;
        return this;
    }

    public NCallable<?> getCallable() {
        return callable;
    }

    public NWorkBalancerCallWorkerModel setCallable(NCallable<?> callable) {
        this.callable = callable;
        return this;
    }

    public Map<String, NElement> getOptions() {
        return options;
    }

    public NWorkBalancerCallWorkerModel setOptions(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    @Override
    public NWorkBalancerCallWorkerModel copy() {
        return clone();
    }

    @Override
    public NWorkBalancerCallWorkerModel clone() {
        NWorkBalancerCallWorkerModel copy = new NWorkBalancerCallWorkerModel();
        copy.id = this.id;
        copy.callable = this.callable;
        copy.options =this.options==null?null:new HashMap<>(this.options);
        return copy;
    }

    @Override
    public String toString() {
        return "NWorkBalancerCallWorkerModel{" +
                "id='" + id + '\'' +
                ", callable=" + callable +
                ", options=" + options +
                '}';
    }
}
