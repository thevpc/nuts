package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NBlankable;

import java.util.UUID;

public class NBulkheadCallFactoryImpl implements NBulkheadCallFactory {
    private NBulkheadCallBackend backend;
    private NBulkheadCallStore store;

    public NBulkheadCallFactoryImpl(NBulkheadCallBackend backend, NBulkheadCallStore store) {
        this.backend = backend;
        this.store = store;
    }

    @Override
    public <T> NBulkheadCall<T> bulkheadCall(NCallable<T> callable) {
        return bulkheadCall(null, callable);
    }

    @Override
    public <T> NBulkheadCall<T> bulkheadCall(String id, NCallable<T> callable) {
        return new NBulkheadCallImpl<>(
                NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id,
                callable,
                store, backend
        );
    }

    @Override
    public NBulkheadCallFactory withStore(NBulkheadCallStore store) {
        return new NBulkheadCallFactoryImpl(backend, store == null ? this.store : store);
    }

    @Override
    public NBulkheadCallFactory withBackend(NBulkheadCallBackend backend) {
        return new NBulkheadCallFactoryImpl(backend == null ? this.backend : backend, store);
    }

    @Override
    public NBulkheadCallBackend getBackend() {
        return backend;
    }

    @Override
    public NBulkheadCallStore getStore() {
        return store;
    }


}
