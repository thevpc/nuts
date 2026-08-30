package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NAssert;

import java.util.HashMap;
import java.util.Map;

class NSagaStoreMemory implements NSagaStore {
    private final Map<String, NSagaModel> values = new HashMap<>();

    @Override
    public NSagaModel load(String id) {
        if (id == null) {
            id = "";
        }
        synchronized (values) {
            NSagaModel d = values.get(id);
            return d==null?null:d.copy();
        }
    }

    @Override
    public void save(NSagaModel model) {
        NAssert.requireNamedNonNull(model, "model");
        synchronized (values) {
            values.put(model.id(), model.copy());
        }
    }

    @Override
    public boolean delete(String id) {
        synchronized (values) {
            return values.remove(id)!=null;
        }
    }
}
