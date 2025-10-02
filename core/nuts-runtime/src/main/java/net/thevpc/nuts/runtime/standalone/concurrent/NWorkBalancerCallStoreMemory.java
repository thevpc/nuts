package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerCallStore;
import net.thevpc.nuts.concurrent.NWorkBalancerModel;

import java.util.HashMap;
import java.util.Map;

public class NWorkBalancerCallStoreMemory implements NWorkBalancerCallStore {
    Map<String, NWorkBalancerModel> values = new HashMap<>();

    @Override
    public NWorkBalancerModel load(String id) {
        if (id == null) {
            id = "";
        }
        return values.get(id);
    }

    @Override
    public void save(NWorkBalancerModel model) {
        values.put(model.getId(), model);
    }
}
