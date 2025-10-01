package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NRetryCall;
import net.thevpc.nuts.concurrent.NRetryCallFactory;
import net.thevpc.nuts.concurrent.NRetryCallStore;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NCallable;

import java.util.UUID;

public class NRetryCallFactoryImpl implements NRetryCallFactory {

    private final NBeanContainer beanContainer;
    private final NRetryCallStore store;

    public NRetryCallFactoryImpl(NRetryCallStore store, NBeanContainer beanContainer) {
        this.beanContainer = beanContainer;
        this.store = store;
    }

    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }


    public NRetryCallFactory withBeanContainer(NBeanContainer container) {
        if (container == this.beanContainer) {
            return this;
        }
        return new NRetryCallFactoryImpl(store, container);
    }

    public NRetryCallStore getStore() {
        return store;
    }

    public NRetryCallFactory withStore(NRetryCallStore store) {
        if (store == this.store) {
            return this;
        }
        return new NRetryCallFactoryImpl(store, beanContainer);
    }

    public <T> NRetryCall<T> of(NCallable<T> callable) {
        return of(callable);
    }

    public <T> NRetryCall<T> of(String id, NCallable<T> callable) {
        if (NBlankable.isBlank(id)) {
            id = UUID.randomUUID().toString();
        }
        return new NRetryCallImpl<>(id, callable,
                beanContainer, store == null ? NConcurrent.of().memoryRetryCallFactory().getStore() : store);
    }


}
