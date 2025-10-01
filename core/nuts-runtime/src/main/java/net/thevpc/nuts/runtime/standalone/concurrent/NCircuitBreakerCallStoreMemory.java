package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCircuitBreakerCallModel;
import net.thevpc.nuts.concurrent.NCircuitBreakerCallStore;

import java.util.HashMap;
import java.util.Map;

public class NCircuitBreakerCallStoreMemory implements NCircuitBreakerCallStore {
    private Map<String, NCircuitBreakerCallModel> map = new HashMap<>();
    @Override
    public NCircuitBreakerCallModel load(String id) {
        return map.get(id);
    }

    @Override
    public void save(NCircuitBreakerCallModel value) {
        this.map.put(value.getId(), value);
    }
}
