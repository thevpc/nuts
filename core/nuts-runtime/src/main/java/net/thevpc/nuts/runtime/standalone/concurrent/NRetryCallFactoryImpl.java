package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NRetryCall;
import net.thevpc.nuts.concurrent.NRetryCallFactory;
import net.thevpc.nuts.concurrent.NRetryCallStore;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.UUID;

public class NRetryCallFactoryImpl implements NRetryCallFactory {

    private final NRetryCallStore store;

    public NRetryCallFactoryImpl(NRetryCallStore store) {
        this.store = store;
    }

    public NRetryCallStore getStore() {
        return store;
    }

    public NRetryCallFactory withStore(NRetryCallStore store) {
        if (store == this.store) {
            return this;
        }
        return new NRetryCallFactoryImpl(store);
    }

    public <T> NRetryCall<T> of(NCallable<T> callable) {
        return of(callable);
    }

    public <T> NRetryCall<T> of(String id, NCallable<T> callable) {
        if (NBlankable.isBlank(id)) {
            id = UUID.randomUUID().toString();
        }
        return new NRetryCallImpl<>(id, callable,
                store == null ? NConcurrent.of().memoryRetryCallFactory().getStore() : store);
    }


}
