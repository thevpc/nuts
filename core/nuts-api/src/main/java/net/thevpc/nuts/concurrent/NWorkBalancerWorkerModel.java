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
public class NWorkBalancerWorkerModel implements Serializable, Cloneable, NCopiable {
    private String id;
    private NCallable<?> callable;
    private Map<String, NElement> options;
    private NWorkBalancerHostLoadProvider loadSupplier;

    public NWorkBalancerWorkerModel() {
    }

    public String getId() {
        return id;
    }

    public NWorkBalancerHostLoadProvider getLoadSupplier() {
        return loadSupplier;
    }

    public NWorkBalancerWorkerModel setLoadSupplier(NWorkBalancerHostLoadProvider loadSupplier) {
        this.loadSupplier = loadSupplier;
        return this;
    }

    public NWorkBalancerWorkerModel setId(String id) {
        this.id = id;
        return this;
    }

    public NCallable<?> getCallable() {
        return callable;
    }

    public NWorkBalancerWorkerModel setCallable(NCallable<?> callable) {
        this.callable = callable;
        return this;
    }

    public Map<String, NElement> getOptions() {
        return options;
    }

    public NWorkBalancerWorkerModel setOptions(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    @Override
    public NWorkBalancerWorkerModel copy() {
        return clone();
    }

    @Override
    public NWorkBalancerWorkerModel clone() {
        NWorkBalancerWorkerModel copy = new NWorkBalancerWorkerModel();
        copy.id = this.id;
        copy.callable = this.callable;
        copy.options =this.options==null?null:new HashMap<>(this.options);
        return copy;
    }

    @Override
    public String toString() {
        return "NWorkBalancerWorkerModel{" +
                "id='" + id + '\'' +
                ", callable=" + callable +
                ", options=" + options +
                ", loadSupplier=" + loadSupplier +
                '}';
    }
}
