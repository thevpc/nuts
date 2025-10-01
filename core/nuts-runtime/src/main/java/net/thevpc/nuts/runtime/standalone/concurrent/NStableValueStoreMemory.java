package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NStableValueModel;
import net.thevpc.nuts.concurrent.NStableValueStore;

import java.util.HashMap;
import java.util.Map;

class NStableValueStoreMemory implements NStableValueStore {
    Map<String, NStableValueModel> values = new HashMap<>();

    @Override
    public NStableValueModel load(String id) {
        return values.get(id);
    }

    @Override
    public void save(NStableValueModel value) {
        if (value.getValue() != null) {
            values.put(value.getId(), value);
        } else {
            values.remove(value.getId());
        }
    }

}
