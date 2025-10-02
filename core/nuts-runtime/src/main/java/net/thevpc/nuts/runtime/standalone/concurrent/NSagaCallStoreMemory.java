package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.HashMap;
import java.util.Map;

class NSagaCallStoreMemory implements NSagaCallStore {
    Map<String, NSagaCallModel> values = new HashMap<>();

    @Override
    public NSagaCallModel load(String id) {
        if (id == null) {
            id = "";
        }
        return values.get(id);
    }

    @Override
    public void save(NSagaCallModel model) {
        values.put(model.getId(), model);
    }
}
