package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NBulkheadCallBackend;
import net.thevpc.nuts.concurrent.NBulkheadMetrics;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of bulkhead backend using Java's {@link Semaphore}.
 * <p>
 * This implementation is fast and thread-safe but does not persist state
 * across JVM restarts. For persistent bulkheads, use BulkheadBackendFileBasedImpl or BulkheadBackendRedisImpl.
 * </p>
 * <p>
 * This should be the default backend when no backend is explicitly configured.
 * </p>
 *
 * @since 0.8.7
 */
public class NBulkheadCallBackendAsSemaphore implements NBulkheadCallBackend {

    // Map of bulkhead ID to its semaphore
    private final ConcurrentHashMap<String, BulkheadState> bulkheads = new ConcurrentHashMap<>();

    @Override
    public NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent) {
        BulkheadState state = getOrCreateBulkhead(bulkheadId, maxConcurrent);

        if (state.semaphore.tryAcquire()) {
            state.activeCount.incrementAndGet();
            state.totalAcquired.incrementAndGet();

            return NOptional.of(new SemaphorePermitN(
                    bulkheadId,
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    Thread.currentThread().getId()
            ));
        }

        state.totalRejected.incrementAndGet();
        return NOptional.ofEmpty();
    }

    @Override
    public NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent, NDuration timeout) {
        BulkheadState state = getOrCreateBulkhead(bulkheadId, maxConcurrent);
        if(timeout==null){
            timeout=NDuration.ofMillis(Long.MAX_VALUE);
        }
        try {
            long startTime = System.currentTimeMillis();
            boolean acquired = state.semaphore.tryAcquire(
                    timeout.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (acquired) {
                state.activeCount.incrementAndGet();
                state.totalAcquired.incrementAndGet();

                long waitTime = System.currentTimeMillis() - startTime;
                state.updateMaxWaitTime(waitTime);

                return NOptional.of(new SemaphorePermitN(
                        bulkheadId,
                        UUID.randomUUID().toString(),
                        System.currentTimeMillis(),
                        Thread.currentThread().getId()
                ));
            }

            state.totalRejected.incrementAndGet();
            return NOptional.ofEmpty();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            state.totalRejected.incrementAndGet();
            return NOptional.ofEmpty();
        }
    }

    @Override
    public void release(NBulkheadPermit permit) {
        BulkheadState state = bulkheads.get(permit.getBulkheadId());
        if (state != null) {
            state.semaphore.release();
            state.activeCount.decrementAndGet();
            state.totalReleased.incrementAndGet();

            long duration = System.currentTimeMillis() - permit.getAcquiredAt();
            state.updateMaxHoldTime(duration);
        }
    }

    @Override
    public NBulkheadMetrics getMetrics(String bulkheadId) {
        BulkheadState state = bulkheads.get(bulkheadId);
        if (state == null) {
            return new NBulkheadMetrics(bulkheadId, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        return new NBulkheadMetrics(
                bulkheadId,
                state.maxConcurrent,
                state.activeCount.get(),
                state.semaphore.availablePermits(),
                state.totalAcquired.get(),
                state.totalReleased.get(),
                state.totalRejected.get(),
                state.maxWaitTime.get(),
                state.maxHoldTime.get()
        );
    }

    @Override
    public int cleanupExpired(String bulkheadId, NDuration expiryDuration) {
        // In-memory semaphores don't have "expired" permits
        // This is only relevant for persistent backends
        return 0;
    }

    /**
     * Gets or creates bulkhead state for the given ID.
     * If the bulkhead exists but has a different maxConcurrent, resizes it.
     */
    private BulkheadState getOrCreateBulkhead(String bulkheadId, int maxConcurrent) {
        return bulkheads.compute(bulkheadId, (id, existing) -> {
            if (existing == null) {
                return new BulkheadState(maxConcurrent);
            }

            // Handle dynamic resize if maxConcurrent changed
            if (existing.maxConcurrent != maxConcurrent) {
                existing.resize(maxConcurrent);
            }

            return existing;
        });
    }

    /**
     * Internal state for a single bulkhead.
     */
    private static class BulkheadState {
        private volatile Semaphore semaphore;
        private volatile int maxConcurrent;

        private final AtomicInteger activeCount = new AtomicInteger(0);
        private final AtomicLong totalAcquired = new AtomicLong(0);
        private final AtomicLong totalReleased = new AtomicLong(0);
        private final AtomicLong totalRejected = new AtomicLong(0);
        private final AtomicLong maxWaitTime = new AtomicLong(0);
        private final AtomicLong maxHoldTime = new AtomicLong(0);

        BulkheadState(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
            this.semaphore = new Semaphore(maxConcurrent, true); // fair semaphore
        }

        void resize(int newMaxConcurrent) {
            int delta = newMaxConcurrent - this.maxConcurrent;

            if (delta > 0) {
                // Increase capacity
                semaphore.release(delta);
            } else if (delta < 0) {
                // Decrease capacity (drain permits)
                semaphore.acquireUninterruptibly(-delta);
            }

            this.maxConcurrent = newMaxConcurrent;
        }

        void updateMaxWaitTime(long waitTime) {
            long current;
            do {
                current = maxWaitTime.get();
                if (waitTime <= current) break;
            } while (!maxWaitTime.compareAndSet(current, waitTime));
        }

        void updateMaxHoldTime(long holdTime) {
            long current;
            do {
                current = maxHoldTime.get();
                if (holdTime <= current) break;
            } while (!maxHoldTime.compareAndSet(current, holdTime));
        }
    }

    /**
     * Simple in-memory permit token.
     */
    private static class SemaphorePermitN implements NBulkheadPermit {
        private final String bulkheadId;
        private final String permitId;
        private final long acquiredAt;
        private final long threadId;

        SemaphorePermitN(String bulkheadId, String permitId, long acquiredAt, long threadId) {
            this.bulkheadId = bulkheadId;
            this.permitId = permitId;
            this.acquiredAt = acquiredAt;
            this.threadId = threadId;
        }

        @Override
        public String getBulkheadId() {
            return bulkheadId;
        }

        @Override
        public String getPermitId() {
            return permitId;
        }

        @Override
        public long getAcquiredAt() {
            return acquiredAt;
        }

        public long getThreadId() {
            return threadId;
        }
    }
}
