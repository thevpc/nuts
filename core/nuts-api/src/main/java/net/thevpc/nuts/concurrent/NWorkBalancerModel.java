package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /**
     * N work balancer model.
     *
     * @return n work balancer model result
     */
    public NWorkBalancerModel() {
    }

    /**
     * Strategy.
     *
     * @return strategy result
     */
    @NGetter
    public String strategy() {
        return strategy;
    }

    /**
     * Strategy.
     *
     * @param strategy strategy
     * @return strategy result
     */
    public NWorkBalancerModel strategy(String strategy) {
        this.strategy = strategy;
        return this;
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
    @NGetter
    public NWorkBalancerModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Options.
     *
     * @return options result
     */
    @NGetter
    public Map<String, NElement> options() {
        return options;
    }

    /**
     * Options.
     *
     * @param options options
     * @return options result
     */
    @NSetter
    public NWorkBalancerModel options(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    /**
     * Workers.
     *
     * @return workers result
     */
    @NGetter
    public List<NWorkBalancerWorkerModel> workers() {
        return workers;
    }

    /**
     * Workers.
     *
     * @param workers workers
     * @return workers result
     */
    @NSetter
    public NWorkBalancerModel workers(List<NWorkBalancerWorkerModel> workers) {
        this.workers = workers;
        return this;
    }

    /**
     * Context.
     *
     * @return context result
     */
    @NGetter
    public NWorkBalancerModelContext context() {
        return context;
    }

    /**
     * Context.
     *
     * @param context context
     * @return context result
     */
    @NSetter
    public NWorkBalancerModel context(NWorkBalancerModelContext context) {
        this.context = context;
        return this;
    }

    @Override
    public NWorkBalancerModel copy() {
        /**
         * Clone.
         *
         * @return clone result
         */
        return clone();
    }

    @Override
    public NWorkBalancerModel clone() {
        NWorkBalancerModel copy = null;
        try {
            copy = (NWorkBalancerModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
        copy.options = this.options == null ? null : new HashMap<>(this.options);
        copy.workers = this.workers == null ? null : workers.stream().map(x -> x.copy()).collect(Collectors.toList());
        copy.context = this.context == null ? null : this.context.clone();
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NWorkBalancerModel that = (NWorkBalancerModel) o;
        return Objects.equals(id, that.id) && Objects.equals(options, that.options) && Objects.equals(strategy, that.strategy) && Objects.equals(workers, that.workers) && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, options, strategy, workers, context);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true)
                .add("id", id)
                .add("context", context)
                .add("strategy", strategy)
                .add("options", options)
                .add("workers", workers)
                .build();
    }
}
