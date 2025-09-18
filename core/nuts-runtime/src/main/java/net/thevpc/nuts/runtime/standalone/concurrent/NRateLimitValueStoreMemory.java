package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitValueModel;
import net.thevpc.nuts.concurrent.NRateLimitValueStore;

import java.util.HashMap;
import java.util.Map;

class NRateLimitValueStoreMemory implements NRateLimitValueStore {
    Map<String, NRateLimitValueModel> values = new HashMap<>();

    @Override
    public NRateLimitValueModel load(String id) {
        if (id == null) {
            id = "";
        }
        return values.get(id);
    }

    @Override
    public void save(NRateLimitValueModel model) {
        values.put(model.getId(), model);
    }
}
