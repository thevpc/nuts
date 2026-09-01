package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCachedValueModel;
import net.thevpc.nuts.concurrent.NCachedValueStore;

import java.util.HashMap;
import java.util.Map;

public class NCachedValueStoreMemory implements NCachedValueStore {
    private final Map<String, NCachedValueModel> values = new HashMap<>();

    @Override
    public NCachedValueModel load(String id) {
        NCachedValueModel c = values.get(id);
        return c == null ? null : c.copy();
    }

    @Override
    public void save(NCachedValueModel value) {
        synchronized (values) {
            if (value != null) {
                values.put(value.id(), value.copy());
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
