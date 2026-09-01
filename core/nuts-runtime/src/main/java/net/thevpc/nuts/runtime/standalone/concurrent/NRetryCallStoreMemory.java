package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRetryCallStore;
import net.thevpc.nuts.concurrent.NRetryCallModel;
import net.thevpc.nuts.util.NAssert;

import java.util.HashMap;
import java.util.Map;

public class NRetryCallStoreMemory implements NRetryCallStore {
    private final Map<String, NRetryCallModel> map = new HashMap<>();

    @Override
    public NRetryCallModel load(String id) {
        NRetryCallModel d;
        synchronized (map) {
            d = map.get(id);
            if (d != null) {
                d = d.copy();
            }
        }
        return d;
    }

    @Override
    public void save(NRetryCallModel value) {
        NAssert.requireNamedNonNull(value, "value");
        value = value.copy();
        synchronized (map) {
            this.map.put(value.id(), value);
        }
    }

    @Override
    public boolean delete(String id) {
        synchronized (map) {
            return map.remove(id) != null;
        }
    }
}
