package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCachedValueModel;
import net.thevpc.nuts.concurrent.NCachedValueStore;

import java.util.HashMap;
import java.util.Map;

class NCachedValueStoreMemory implements NCachedValueStore {
    private final Map<String, NCachedValueModel> values = new HashMap<>();

    @Override
    public NCachedValueModel load(String id) {
        return values.get(id);
    }

    @Override
    public void save(NCachedValueModel value) {
        if (value != null) {
            values.put(value.id(), value);
        } else {
            values.remove(value.id());
        }
    }


    @Override
    public boolean delete(String id) {
        synchronized (values) {
            return values.remove(id) != null;
        }
    }
}
