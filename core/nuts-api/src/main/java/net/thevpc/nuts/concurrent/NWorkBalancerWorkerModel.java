package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representation of a {@link NWorkBalancerWorker}.
 * <p>
 * Holds the worker's name, weight, host load metrics provider, and optional metadata.
 * Primarily used as a basis for persistence, for storing and restoring worker configurations.
 * Can also serve as a blueprint to create {@link NWorkBalancerWorker} instances.
 * </p>
 *
 * @since 0.8.7
 */
public class NWorkBalancerWorkerModel implements Serializable, Cloneable, NCopiable {
    /** Unique name of the worker */
    private String name;

    /** Optional configuration or metadata for the worker */
    private Map<String, NElement> options;

    /** Relative weight for worker selection in strategies */
    private float weight;

    /** Provides host load metrics for this worker */
    private NWorkBalancerHostLoadMetricProvider hostLoadMetricsProvider;

    public NWorkBalancerWorkerModel() {
    }

    /**
     * Returns the weight of the worker.
     * <p>
     * Used by strategies to influence worker selection probability.
     * </p>
     *
     * @return weight value
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Sets the worker weight.
     *
     * @param weight the weight value
     * @return this model for chaining
     */
    public NWorkBalancerWorkerModel setWeight(float weight) {
        this.weight = weight;
        return this;
    }


    /**
     * Returns the worker's unique name.
     *
     * @return worker name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the worker's name.
     *
     * @param name the worker name
     * @return this model for chaining
     */
    public NWorkBalancerWorkerModel setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the host load metrics provider for this worker.
     *
     * @return the metrics provider
     */
    public NWorkBalancerHostLoadMetricProvider getHostLoadMetricsProvider() {
        return hostLoadMetricsProvider;
    }

    /**
     * Sets the host load metrics provider for this worker.
     *
     * @param hostLoadMetricsProvider the metrics provider
     * @return this model for chaining
     */
    public NWorkBalancerWorkerModel setHostLoadMetricsProvider(NWorkBalancerHostLoadMetricProvider hostLoadMetricsProvider) {
        this.hostLoadMetricsProvider = hostLoadMetricsProvider;
        return this;
    }


    /**
     * Returns the worker's options map.
     *
     * @return options map (may be null)
     */
    public Map<String, NElement> getOptions() {
        return options;
    }

    /**
     * Sets the worker's options map.
     *
     * @param options options map
     * @return this model for chaining
     */
    public NWorkBalancerWorkerModel setOptions(Map<String, NElement> options) {
        this.options = options;
        return this;
    }

    @Override
    public NWorkBalancerWorkerModel copy() {
        return clone();
    }

    @Override
    protected NWorkBalancerWorkerModel clone() {
        NWorkBalancerWorkerModel copy = null;
        try {
            copy = (NWorkBalancerWorkerModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        copy.name = this.name;
        copy.options = this.options == null ? null : new HashMap<>(this.options);
        return copy;
    }

    @Override
    public String toString() {
        return "NWorkBalancerWorkerModel{" +
                "id='" + name + '\'' +
                ", options=" + options +
                ", loadSupplier=" + hostLoadMetricsProvider +
                '}';
    }
}
