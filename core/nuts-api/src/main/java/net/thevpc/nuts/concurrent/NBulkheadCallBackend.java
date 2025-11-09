package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;

/**
 * @since 0.8.8
 */
public interface NBulkheadCallBackend  {

    NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent);

    NOptional<NBulkheadPermit> tryAcquire(String bulkheadId, int maxConcurrent, NDuration timeout);

    void release(NBulkheadPermit permit);

    NBulkheadMetrics getMetrics(String bulkheadId);

    int cleanupExpired(String bulkheadId, NDuration expiryDuration);

    interface NBulkheadPermit {
        String getBulkheadId();
        String getPermitId();
        long getAcquiredAt();
    }
}
