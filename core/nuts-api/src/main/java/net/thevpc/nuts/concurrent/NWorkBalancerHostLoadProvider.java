package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;

import java.util.Map;

/**
 * Provides host load and metrics for a worker.
 * Used by strategies to calculate host pressure and choose workers.
 *
 * @since 0.8.7
 */
public interface NWorkBalancerHostLoadProvider {
    /**
     * Returns a normalized composite load factor (0..1) used by strategies, possibly computed from CPU, memory, latency, or custom metrics.
     */
    float resolveHostLoad();

    /** Returns host CPU load (optional). */
    default float resolveHostCpuLoad(){
        return Float.NaN;
    }

    /** Returns host memory load (optional). */
    default float resolveHostMemoryLoad(){
        return Float.NaN;
    }

    /** Returns host latency in milliseconds (optional). */
    default long resolveHostLatency(){
        return -1;
    }

    /**
     * Returns a map of custom metrics. Called by the balancer, the returned
     * metrics are automatically put into the strategy context variables.
     */
    Map<String, NElement> resolveMetrics();
}
