package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;

import java.util.HashMap;
import java.util.Map;

class NSagaStoreMemory implements NSagaStore {
    Map<String, NSagaModel> values = new HashMap<>();

    @Override
    public NSagaModel load(String id) {
        if (id == null) {
            id = "";
        }
        return values.get(id);
    }

    @Override
    public void save(NSagaModel model) {
        values.put(model.getId(), model);
    }
}
