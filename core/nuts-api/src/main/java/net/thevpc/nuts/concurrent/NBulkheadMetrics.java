package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.util.NGetter;

/**
 * Immutable snapshot of runtime metrics for a bulkhead.
 * <p>
 * A bulkhead limits concurrent access to a shared resource, and this class
 * provides insight into its usage and performance over time. Metrics include
 * concurrency limits, active call counts, rejections, and timing statistics.
 * <p>
 * Instances of this class are typically produced by
 * {@link NBulkheadCallBackend#getMetrics(String)}.
 *
 * @since 0.8.8
 */

public class NBulkheadMetrics implements NDescribable {
    private final String bulkheadId;
    private final int maxConcurrent;
    private final int activeCalls;
    private final int availableSlots;
    private final long totalAcquired;
    private final long totalReleased;
    private final long totalRejected;
    private final long maxWaitTimeMillis;
    private final long maxHoldTimeMillis;

    /**
     * Creates a new metrics snapshot for a bulkhead.
     *
     * @param bulkheadId        unique identifier of the bulkhead
     * @param maxConcurrent     configured maximum number of concurrent calls
     * @param activeCalls       number of currently active calls
     * @param availableSlots    number of free slots available for new calls
     * @param totalAcquired     cumulative number of successfully acquired permits
     * @param totalReleased     cumulative number of released permits
     * @param totalRejected     cumulative number of rejected acquisitions
     * @param maxWaitTimeMillis maximum observed wait time in milliseconds
     * @param maxHoldTimeMillis maximum observed permit hold time in milliseconds
     */
    public NBulkheadMetrics(
            String bulkheadId,
            int maxConcurrent,
            int activeCalls,
            int availableSlots,
            long totalAcquired,
            long totalReleased,
            long totalRejected,
            long maxWaitTimeMillis,
            long maxHoldTimeMillis
    ) {
        this.bulkheadId = bulkheadId;
        this.maxConcurrent = maxConcurrent;
        this.activeCalls = activeCalls;
        this.availableSlots = availableSlots;
        this.totalAcquired = totalAcquired;
        this.totalReleased = totalReleased;
        this.totalRejected = totalRejected;
        this.maxWaitTimeMillis = maxWaitTimeMillis;
        this.maxHoldTimeMillis = maxHoldTimeMillis;
    }

    /** @return unique identifier of the bulkhead */
    @NGetter
    public String bulkheadId() { return bulkheadId; }

    /** @return maximum number of concurrent calls allowed */
    @NGetter
    public int maxConcurrent() { return maxConcurrent; }

    /** @return number of currently active calls */
    @NGetter
    public int activeCalls() { return activeCalls; }

    /** @return number of free slots available for new calls */
    @NGetter
    public int availableSlots() { return availableSlots; }

    /** @return total number of successfully acquired permits */
    @NGetter
    public long totalAcquired() { return totalAcquired; }

    /** @return total number of released permits */
    @NGetter
    public long totalReleased() { return totalReleased; }

    /** @return total number of rejected permit requests */
    @NGetter
    public long totalRejected() { return totalRejected; }

    /** @return maximum observed wait time in milliseconds */
    @NGetter
    public long maxWaitTimeMillis() { return maxWaitTimeMillis; }

    /** @return maximum observed hold time in milliseconds */
    @NGetter
    public long maxHoldTimeMillis() { return maxHoldTimeMillis; }

    /**
     * Computes the ratio of rejected calls to total attempted calls.
     *
     * @return rejection rate between {@code 0.0} and {@code 1.0}
     */
    @NGetter
    public double rejectionRate() {
        long total = totalAcquired + totalRejected;
        return total == 0 ? 0.0 : (double) totalRejected / total;
    }

    /**
     * Indicates whether the bulkhead is fully saturated (no available slots).
     *
     * @return {@code true} if {@link #availableSlots()} equals zero
     */
    @NGetter
    public boolean isFull() {
        return availableSlots == 0;
    }

    /**
     * Provides a structured representation of this metrics snapshot as an {@link NElement}.
     *
     * @return an element-based description of all metrics
     */
    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .set("bulkheadId", bulkheadId)
                .set("maxConcurrent", maxConcurrent)
                .set("activeCalls", activeCalls)
                .set("availableSlots", availableSlots)
                .set("totalAcquired", totalAcquired)
                .set("totalReleased", totalReleased)
                .set("totalRejected", totalRejected)
                .set("rejectionRate", rejectionRate())
                .set("maxWaitTimeMillis", maxWaitTimeMillis)
                .set("maxHoldTimeMillis", maxHoldTimeMillis)
                .build();
    }
}
