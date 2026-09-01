package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCircuitBreakerCallModel;
import net.thevpc.nuts.concurrent.NCircuitBreakerCallStore;
import net.thevpc.nuts.util.NAssert;

import java.util.HashMap;
import java.util.Map;

public class NCircuitBreakerCallStoreMemory implements NCircuitBreakerCallStore {
    private final Map<String, NCircuitBreakerCallModel> values = new HashMap<>();
    @Override
    public NCircuitBreakerCallModel load(String id) {
        NCircuitBreakerCallModel d;
        synchronized (values) {
            d = values.get(id);
        }
        return d==null?null:d.copy();
    }

    @Override
    public void save(NCircuitBreakerCallModel value) {
        NAssert.requireNamedNonNull(value, "value");
        synchronized (values) {
            this.values.put(value.id(), value.copy());
        }
    }

    @Override
    public boolean delete(String id) {
        synchronized (values) {
            return values.remove(id) != null;
        }
    }

}
