package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;

/**
 * @since 0.8.8
 */
public class NBulkheadMetrics implements NElementDescribable {
    private final String bulkheadId;
    private final int maxConcurrent;
    private final int activeCalls;
    private final int availableSlots;
    private final long totalAcquired;
    private final long totalReleased;
    private final long totalRejected;
    private final long maxWaitTimeMillis;
    private final long maxHoldTimeMillis;

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

    // Getters...
    public String getBulkheadId() { return bulkheadId; }
    public int getMaxConcurrent() { return maxConcurrent; }
    public int getActiveCalls() { return activeCalls; }
    public int getAvailableSlots() { return availableSlots; }
    public long getTotalAcquired() { return totalAcquired; }
    public long getTotalReleased() { return totalReleased; }
    public long getTotalRejected() { return totalRejected; }
    public long getMaxWaitTimeMillis() { return maxWaitTimeMillis; }
    public long getMaxHoldTimeMillis() { return maxHoldTimeMillis; }

    public double getRejectionRate() {
        long total = totalAcquired + totalRejected;
        return total == 0 ? 0.0 : (double) totalRejected / total;
    }

    public boolean isFull() {
        return availableSlots == 0;
    }

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
                .set("rejectionRate", getRejectionRate())
                .set("maxWaitTimeMillis", maxWaitTimeMillis)
                .set("maxHoldTimeMillis", maxHoldTimeMillis)
                .build();
    }
}
