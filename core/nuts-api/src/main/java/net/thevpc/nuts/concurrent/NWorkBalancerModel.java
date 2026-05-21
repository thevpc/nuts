package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model representation of a {@link NWorkBalancer}.
 * <p>
 * Holds the ID, strategy, worker configurations, options, and runtime context.
 * Primarily used as a basis for persistence, storing and restoring
 * {@link NWorkBalancer} configurations.
 * Can also be used to create or reconstruct {@link NWorkBalancer} instances.
 * </p>
 *
 * <p>
 * The {@link #copy()} and {@link #clone()} methods provide safe deep copies,
 * mainly useful for persistence or snapshotting purposes.
 * </p>
 *
 * @since 0.8.7
 */
public class NWorkBalancerModel implements Serializable, Cloneable, NCopiable {

    /**
     * Unique identifier of this work balancer.
     * Used for persistence and retrieval from a store.
     */
    private String id;

    /**
     * Arbitrary key/value options for this work balancer.
     * Can store any configuration needed for custom strategies or workers.
     */
    private Map<String, NElement> options = new HashMap<>();

    /**
     * Name of the strategy used by this work balancer.
     * Should correspond to a registered strategy name or a default strategy.
     */
    private String strategy;

    /**
     * List of worker configurations participating in this work balancer.
     * Each {@link NWorkBalancerWorkerModel} holds name, weight, options,
     * and host load metric provider.
     */
    private List<NWorkBalancerWorkerModel> workers;

    /**
     * Contextual metadata for this work balancer.
     * Stores runtime context, counters, or any additional state needed for persistence.
     */
    private NWorkBalancerModelContext context = new NWorkBalancerModelContext();

    public NWorkBalancerModel() {
    }

    @NGetter
    public String strategy() {
        return strategy;
    }

    public NWorkBalancerModel strategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    @NGetter
    public String id() {
        return id;
    }

    @NGetter
    public NWorkBalancerModel id(String id) {
        this.id = id;
        return this;
    }

    @NGetter
    public Map<String, NElement> options() {
        return options;
    }

    @NSetter
    public NWorkBalancerModel options(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    @NGetter
    public List<NWorkBalancerWorkerModel> workers() {
        return workers;
    }

    @NSetter
    public NWorkBalancerModel workers(List<NWorkBalancerWorkerModel> workers) {
        this.workers = workers;
        return this;
    }

    @NGetter
    public NWorkBalancerModelContext context() {
        return context;
    }

    @NSetter
    public NWorkBalancerModel context(NWorkBalancerModelContext context) {
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
        return "NWorkBalancerModel{" +
                "id='" + id + '\'' +
                ", context=" + context +
                ", options=" + options +
                ", workers=" + workers +
                '}';
    }
}
