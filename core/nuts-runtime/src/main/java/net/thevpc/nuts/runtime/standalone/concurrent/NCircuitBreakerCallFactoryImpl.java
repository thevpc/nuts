package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NCircuitBreakerCall;
import net.thevpc.nuts.concurrent.NCircuitBreakerCallFactory;
import net.thevpc.nuts.concurrent.NCircuitBreakerCallStore;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.UUID;

public class NCircuitBreakerCallFactoryImpl implements NCircuitBreakerCallFactory {

    private final NBeanContainer beanContainer;
    private final NCircuitBreakerCallStore store;

    public NCircuitBreakerCallFactoryImpl(NCircuitBreakerCallStore store, NBeanContainer beanContainer) {
        this.beanContainer = beanContainer;
        this.store = store;
    }

    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }


    public NCircuitBreakerCallFactory withBeanContainer(NBeanContainer container) {
        if (container == this.beanContainer) {
            return this;
        }
        return new NCircuitBreakerCallFactoryImpl(store, container);
    }

    public NCircuitBreakerCallStore getStore() {
        return store;
    }

    public NCircuitBreakerCallFactory withStore(NCircuitBreakerCallStore store) {
        if (store == this.store) {
            return this;
        }
        return new NCircuitBreakerCallFactoryImpl(store, beanContainer);
    }

    public <T> NCircuitBreakerCall<T> of(NCallable<T> callable) {
        return of(callable);
    }

    public <T> NCircuitBreakerCall<T> of(String id, NCallable<T> callable) {
        if (NBlankable.isBlank(id)) {
            id = UUID.randomUUID().toString();
        }
        return new NCircuitBreakerCallImpl<>(id, callable,
                beanContainer, store == null ? NConcurrent.of().memoryCircuitBreakerCallFactory().getStore() : store);
    }


}
