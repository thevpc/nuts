package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;

/**
 * Backend interface for implementing a bulkhead pattern for controlling concurrent access
 * to resources or services.
 * <p>
 * A bulkhead limits the number of concurrent executions identified by a {@code bulkheadId}.
 * This interface provides methods to acquire and release permits, retrieve metrics,
 * and clean up expired permits.
 *
 * <p>Typical usage:
 * <pre>{@code
 * NBulkheadCallBackend backend = ...;
 * NOptional<NBulkheadCallBackend.NBulkheadPermit> permit = backend.tryAcquire("serviceA", 10);
 * if(permit.isPresent()){
 *     try {
 *         // execute protected code
 *     } finally {
 *         backend.release(permit.get());
 *     }
 * }
 * }</pre>
 *
 * @since 0.8.8
 */
public interface NBulkheadCallBackend  {

    /**
     * Attempts to acquire a permit for the given bulkhead ID with a maximum concurrency limit.
     *
     * @param bulkheadId the identifier of the bulkhead
     * @param maxConcurrent the maximum number of concurrent executions allowed
     * @return an {@link NOptional} containing the acquired permit if successful, empty otherwise
     */
    NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent);

    /**
     * Attempts to acquire a permit for the given bulkhead ID with a maximum concurrency limit,
     * waiting up to the specified timeout.
     *
     * @param bulkheadId the identifier of the bulkhead
     * @param maxConcurrent the maximum number of concurrent executions allowed
     * @param timeout the maximum duration to wait for a permit
     * @return an {@link NOptional} containing the acquired permit if successful, empty otherwise
     */
    NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent, NDuration timeout);

    /**
     * Releases a previously acquired permit, allowing another execution to proceed.
     *
     * @param permit the permit to release
     */
    void release(NBulkheadPermit permit);

    /**
     * Retrieves metrics for the specified bulkhead, such as current concurrency and permit usage.
     *
     * @param bulkheadId the identifier of the bulkhead
     * @return metrics for the bulkhead
     */
    NBulkheadMetrics getMetrics(String bulkheadId);


    /**
     * Cleans up expired permits for the specified bulkhead based on the given expiry duration.
     *
     * @param bulkheadId the identifier of the bulkhead
     * @param expiryDuration the duration after which a permit is considered expired
     * @return the number of permits that were cleaned up
     */
    int cleanupExpired(String bulkheadId, NDuration expiryDuration);


    /**
     * Represents a permit acquired from a bulkhead.
     * <p>
     * A permit is required to execute code protected by a bulkhead, and must be released
     * when the execution is complete.
     */
    interface NBulkheadPermit {
        /**
         * Returns the identifier of the bulkhead from which this permit was acquired.
         *
         * @return the bulkhead ID
         */
        String getBulkheadId();
        /**
         * Returns the unique identifier of this permit.
         *
         * @return the permit ID
         */
        String getPermitId();
        /**
         * Returns the timestamp (milliseconds since epoch) when this permit was acquired.
         *
         * @return the acquisition timestamp
         */
        long getAcquiredAt();
    }
}
