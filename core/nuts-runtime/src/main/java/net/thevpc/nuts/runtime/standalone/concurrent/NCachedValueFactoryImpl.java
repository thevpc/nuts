package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class NCachedValueFactoryImpl implements NCachedValueFactory {
    private NCachedValueStore store;

    public NCachedValueFactoryImpl(NCachedValueStore store) {
        this.store = store;
    }


    @Override
    public <T> NCachedValue<T> of(Supplier<T> supplier) {
        return of(UUID.randomUUID().toString(), supplier);
    }

    @Override
    public <T> NCachedValue<T> of(String id, Supplier<T> supplier) {
        String nid = NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id;
        return new NCachedValueImpl<>(nid, NAssert.requireNonNull(supplier, "supplier"), this);
    }

    public NCachedValueModel load(String id) {
        return store.load(id);
    }

    public void save(NCachedValueModel rateLimitValue) {
        store.save(rateLimitValue);
    }
}
