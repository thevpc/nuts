package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NOnceValueModel;
import net.thevpc.nuts.concurrent.NOnceValueStore;

import java.util.HashMap;
import java.util.Map;

public class NOnceValueStoreMemory implements NOnceValueStore {
    private final Map<String, NOnceValueModel> values = new HashMap<>();

    @Override
    public NOnceValueModel load(String id) {
        return values.get(id);
    }

    @Override
    public void save(NOnceValueModel value) {
        synchronized (values) {
            if (value.value() != null) {
                values.put(value.id(), value);
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
