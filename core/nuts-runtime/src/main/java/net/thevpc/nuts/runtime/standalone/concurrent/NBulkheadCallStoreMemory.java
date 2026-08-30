package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 0.8.8
 */
public class NBulkheadCallStoreMemory implements NBulkheadCallStore {

    // Map of bulkhead ID to its semaphore
    private final ConcurrentHashMap<String, NBulkheadCallModel> values = new ConcurrentHashMap<>();

    @Override
    public NBulkheadCallModel load(String id) {
        return values.get(id);
    }

    @Override
    public void save(NBulkheadCallModel value) {
        synchronized (values) {
            if (value.id() != null) {
                values.put(value.id(), value.copy());
            } else {
                values.remove(value.id());
            }
        }
    }

    @Override
    public boolean delete(String id) {
        synchronized (values) {
            return values.remove(id) != null;
        }
    }
}
