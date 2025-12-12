package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NOnceValueModel;
import net.thevpc.nuts.concurrent.NOnceValueStore;

import java.util.HashMap;
import java.util.Map;

class NOnceValueStoreMemory implements NOnceValueStore {
    Map<String, NOnceValueModel> values = new HashMap<>();

    @Override
    public NOnceValueModel load(String id) {
        return values.get(id);
    }

    @Override
    public void save(NOnceValueModel value) {
        if (value.getValue() != null) {
            values.put(value.getId(), value);
        } else {
            values.remove(value.getId());
        }
    }

}
