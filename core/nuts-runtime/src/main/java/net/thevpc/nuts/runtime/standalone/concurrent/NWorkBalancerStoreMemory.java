package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NWorkBalancerStore;
import net.thevpc.nuts.concurrent.NWorkBalancerModel;

import java.util.HashMap;
import java.util.Map;

public class NWorkBalancerStoreMemory implements NWorkBalancerStore {
    private final Map<String, NWorkBalancerModel> values = new HashMap<>();

    @Override
    public NWorkBalancerModel load(String id) {
        if (id == null) {
            id = "";
        }
        NWorkBalancerModel v = values.get(id);
        return v == null ? null : v.copy();
    }

    @Override
    public void save(NWorkBalancerModel model) {
        synchronized (values) {
            model = model.copy();
            values.put(model.id(), model);
        }
    }

    @Override
    public boolean delete(String id) {
        synchronized (values) {
            return values.remove(id) != null;
        }
    }

}
