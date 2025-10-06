package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents runtime load metrics of a worker host.
 * <p>
 * These metrics are used by {@link NWorkBalancer} strategies to evaluate
 * the load, responsiveness, and capacity pressure of each worker when
 * selecting where to execute jobs.
 * </p>
 * <p>
 * Metrics may include CPU load, memory usage, latency, or any custom
 * metrics defined by the user. All metrics are optional; strategies
 * may ignore metrics that are unavailable (NaN or negative values).
 * </p>
 * <p>
 * Typical usage:
 * <pre>{@code
 * NWorkBalancerHostLoadMetrics metrics = new NWorkBalancerHostLoadMetrics()
 *     .setHostLoadFactor(0.75f)
 *     .setHostCpuLoad(0.65f)
 *     .setHostMemoryLoad(0.55f)
 *     .setHostLatency(120L);
 * }</pre>
 * </p>
 *
 * @since 0.8.7
 */
public class NWorkBalancerHostLoadMetrics implements NCopiable, Cloneable {
    private float hostLoadFactor;

    /**
     * Returns host CPU load (optional).
     */
    private float hostCpuLoad = Float.NaN;

    /**
     * Returns host memory load (optional).
     */
    private float hostMemoryLoad = Float.NaN;

    /**
     * Returns host latency in milliseconds (optional).
     */
    private long hostLatency = -1;

    private Map<String, NElement> customMetrics;


    /**
     * Returns the overall load factor of the host (0.0–1.0).
     */
    public float getHostLoadFactor() {
        return hostLoadFactor;
    }

    public NWorkBalancerHostLoadMetrics setHostLoadFactor(float hostLoadFactor) {
        this.hostLoadFactor = hostLoadFactor;
        return this;
    }

    /**
     * Returns the CPU load of the host, if available.
     */
    public float getHostCpuLoad() {
        return hostCpuLoad;
    }

    public NWorkBalancerHostLoadMetrics setHostCpuLoad(float hostCpuLoad) {
        this.hostCpuLoad = hostCpuLoad;
        return this;
    }

    /**
     * Returns the memory load of the host, if available.
     */
    public float getHostMemoryLoad() {
        return hostMemoryLoad;
    }

    public NWorkBalancerHostLoadMetrics setHostMemoryLoad(float hostMemoryLoad) {
        this.hostMemoryLoad = hostMemoryLoad;
        return this;
    }

    /**
     * Returns the measured latency of the host in milliseconds, if available.
     */
    public long getHostLatency() {
        return hostLatency;
    }

    public NWorkBalancerHostLoadMetrics setHostLatency(long hostLatency) {
        this.hostLatency = hostLatency;
        return this;
    }

    /**
     * Returns custom user-defined metrics.
     */
    public Map<String, NElement> getCustomMetrics() {
        return customMetrics;
    }

    public NWorkBalancerHostLoadMetrics setCustomMetrics(Map<String, NElement> customMetrics) {
        this.customMetrics = customMetrics;
        return this;
    }

    @Override
    public NWorkBalancerHostLoadMetrics copy() {
        return clone();
    }

    @Override
    protected NWorkBalancerHostLoadMetrics clone() {
        try {
            NWorkBalancerHostLoadMetrics copy = (NWorkBalancerHostLoadMetrics) super.clone();
            copy.customMetrics = customMetrics == null ? null : new HashMap<>(customMetrics);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NWorkBalancerHostLoadMetrics that = (NWorkBalancerHostLoadMetrics) o;
        return Float.compare(hostLoadFactor, that.hostLoadFactor) == 0 && Float.compare(hostCpuLoad, that.hostCpuLoad) == 0 && Float.compare(hostMemoryLoad, that.hostMemoryLoad) == 0 && hostLatency == that.hostLatency && Objects.equals(customMetrics, that.customMetrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostLoadFactor, hostCpuLoad, hostMemoryLoad, hostLatency, customMetrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("hostLoadFactor:").append(hostLoadFactor);

        if (!Float.isNaN(hostCpuLoad)) {
            sb.append(",hostCpuLoad:").append(hostCpuLoad);
        }
        if (!Float.isNaN(hostMemoryLoad)) {
            sb.append(",hostMemoryLoad:").append(hostMemoryLoad);
        }
        if (hostLatency >= 0) {
            sb.append(",hostLatency:").append(hostLatency);
        }
        if (customMetrics != null && !customMetrics.isEmpty()) {
            sb.append(",customMetrics:{");
            boolean first = true;
            for (Map.Entry<String, NElement> e : customMetrics.entrySet()) {
                if (!first) sb.append(",");
                sb.append(e.getKey()).append(":").append(e.getValue());
                first = false;
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }
}
