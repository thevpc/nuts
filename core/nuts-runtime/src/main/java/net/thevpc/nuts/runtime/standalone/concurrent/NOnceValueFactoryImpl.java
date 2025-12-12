package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;

import java.util.UUID;
import java.util.function.Supplier;

public class NOnceValueFactoryImpl implements NOnceValueFactory {
    private final NOnceValueStore store;

    public NOnceValueFactoryImpl(NOnceValueStore store) {
        this.store = store;
    }

    @Override
    public NOnceValueFactory withStore(NOnceValueStore store) {
        if (store == this.store) {
            return this;
        }
        return new NOnceValueFactoryImpl(store);
    }

    @Override
    public NOnceValueStore getStore() {
        return store;
    }

    @Override
    public <T> NOnceValue<T> of(Supplier<T> supplier) {
        return of(UUID.randomUUID().toString(), supplier);
    }

    @Override
    public <T> NOnceValue<T> of(String id, Supplier<T> supplier) {
        String nid = NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id;
        return new NOnceValueImpl<>(nid, NAssert.requireNonNull(supplier, "supplier"), store);
    }

    public NOnceValueModel load(String id) {
        return store.load(id);
    }

    public void save(NOnceValueModel rateLimitValue) {
        store.save(rateLimitValue);
    }
}
