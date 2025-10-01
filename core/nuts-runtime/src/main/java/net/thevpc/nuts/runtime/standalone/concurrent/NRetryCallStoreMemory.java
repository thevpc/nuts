package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRetryCallStore;
import net.thevpc.nuts.concurrent.NRetryCallModel;

import java.util.HashMap;
import java.util.Map;

public class NRetryCallStoreMemory implements NRetryCallStore {
    private Map<String, NRetryCallModel> map = new HashMap<>();
    @Override
    public NRetryCallModel load(String id) {
        return map.get(id);
    }

    @Override
    public void save(NRetryCallModel value) {
        this.map.put(value.getId(), value);
    }
}
