package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitedValue;
import net.thevpc.nuts.concurrent.NRateLimitedValueModel;
import net.thevpc.nuts.concurrent.NRateLimitedValueStore;

import java.util.HashMap;
import java.util.Map;

class NRateLimitedValueStoreMemory implements NRateLimitedValueStore {
    Map<String, NRateLimitedValueModel> values = new HashMap<>();

    @Override
    public NRateLimitedValueModel load(String id) {
        if (id == null) {
            id = "";
        }
        return values.get(id);
    }

    @Override
    public void save(NRateLimitedValueModel model) {
        values.put(model.getId(), model);
    }
}
