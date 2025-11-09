package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;

import java.util.UUID;
import java.util.function.Supplier;

public class NStableValueFactoryImpl implements NStableValueFactory {
    private final NStableValueStore store;

    public NStableValueFactoryImpl(NStableValueStore store) {
        this.store = store;
    }

    @Override
    public NStableValueFactory withStore(NStableValueStore store) {
        if (store == this.store) {
            return this;
        }
        return new  NStableValueFactoryImpl(store);
    }

    @Override
    public NStableValueStore getStore() {
        return store;
    }

    @Override
    public <T> NStableValue<T> of(Supplier<T> supplier) {
        return of(UUID.randomUUID().toString(), supplier);
    }

    @Override
    public <T> NStableValue<T> of(String id, Supplier<T> supplier) {
        String nid = NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id;
        return new NStableValueImpl<>(nid, NAssert.requireNonNull(supplier, "supplier"), store);
    }

    public NStableValueModel load(String id) {
        return store.load(id);
    }

    public void save(NStableValueModel rateLimitValue) {
        store.save(rateLimitValue);
    }
}
