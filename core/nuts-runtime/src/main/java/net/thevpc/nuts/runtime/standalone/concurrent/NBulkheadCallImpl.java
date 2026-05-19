package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

public class NBulkheadCallImpl<T> implements NBulkheadCall<T> {
    private NBulkheadCallModel model;
    private NBulkheadCallBackend backend;
    private NBulkheadCallStore store;

    public NBulkheadCallImpl(String id, NCallable<T> callable, NBulkheadCallStore store, NBulkheadCallBackend backend) {
        this.model = new NBulkheadCallModel(id);
        this.store = store;
        this.backend = backend;
        this.model.caller(callable);
        reload();
    }

    public void reload() {
        synchronized (this) {
            String oldId = model.id();
            NCallable<?> oldCaller = model.caller();


            NBulkheadCallModel m = store.load(oldId);
            if (m == null) {
                NAssert.requireNamedNonNull(oldCaller, "caller");
                m = new NBulkheadCallModel(oldId);
                m.maxConcurrent(1);
                m.caller(oldCaller);
                store.save(m);
            } else {
                if (oldCaller != null) {
                    m.caller(oldCaller);
                    if(m.maxConcurrent()<=0){
                        m.maxConcurrent(1);
                    }
                    store.save(m);
                }else{
                    if(m.maxConcurrent()<=0){
                        m.maxConcurrent(1);
                        store.save(m);
                    }
                }
            }
            model= m;
        }
    }

    @Override
    public NBulkheadCall<T> maxConcurrent(int maxConcurrent) {
        maxConcurrent=Math.max(1, maxConcurrent);
        int old = model.maxConcurrent();
        if(old!=maxConcurrent) {
            model.maxConcurrent(maxConcurrent);
            store.save(model);
        }
        return this;
    }

    @Override
    public NBulkheadCall<T> permitExpiry(NDuration expiry) {
        NDuration old = model.permitExpiry();
        if(Objects.equals(old,expiry)){
            model.permitExpiry(expiry);
            store.save(model);
        }
        return this;
    }

    @Override
    public int maxConcurrent() {
        return model.maxConcurrent();
    }

    @Override
    public int activeCalls() {
        return backend.getMetrics(model.id()).activeCalls();
    }

    @Override
    public int availableSlots() {
        return backend.getMetrics(model.id()).availableSlots();
    }

    @Override
    public boolean isFull() {
        return backend.getMetrics(model.id()).isFull();
    }

    // ==================== Core Execution Methods ====================

    @Override
    public NOptional<T> tryCall() {
        // Step 1: Try to acquire a permit from the backend
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.id(), Math.max(1,model.maxConcurrent()));

        // Step 2: If no permit available, return empty (rejected)
        if (permitOpt.isEmpty()) {
            return NOptional.ofEmpty();
        }

        // Step 3: We got a permit! Execute the callable
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            T result = ((NCallable<T>)model.caller()).call();
            return NOptional.of(result);
        } finally {
            // Step 4: Always release the permit
            backend.release(permit);
        }
    }

    @Override
    public T tryCallOrElse(NCallable<T> fallback) {
        NOptional<T> result = tryCall();
        if (result.isPresent()) {
            return result.get();
        }
        // Bulkhead was full, execute fallback instead
        return fallback.call();
    }

    @Override
    public T callBlocking() throws NInterruptedException {
        // Step 1: Acquire permit, waiting indefinitely
        // Note: tryAcquire with null timeout = wait forever
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.id(), model.maxConcurrent(), null);

        if (permitOpt.isEmpty()) {
            throw new NInterruptedException(NMsg.ofC("Failed to acquire bulkhead permit"));
        }

        // Step 2: Execute with permit
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.caller()).call();
        } finally {
            backend.release(permit);
        }
    }

    @Override
    public T call() {
        // NCallable interface - non-blocking version
        return tryCall().orElseThrow(() ->
                new NConcurrencyLimitException(NMsg.ofC("Bulkhead is full"))
        );
    }

    @Override
    public T callBlocking(NDuration timeout) throws NInterruptedException {
        // Step 1: Try to acquire permit with timeout
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.id(), model.maxConcurrent(), timeout);

        // Step 2: If timeout expired, throw exception
        if (permitOpt.isEmpty()) {
            throw new NTimeoutException(NMsg.ofC("Timeout waiting for bulkhead permit after %s", timeout));
        }

        // Step 3: Execute with permit
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.caller()).call();
        } finally {
            backend.release(permit);
        }
    }

    @Override
    public T callOrElse(NDuration timeout, NCallable<T> fallback) throws NInterruptedException {
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.id(), model.maxConcurrent(), timeout);

        if (permitOpt.isEmpty()) {
            // Timeout expired, use fallback
            return fallback.call();
        }

        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.caller()).call();
        } finally {
            backend.release(permit);
        }
    }


    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .set("type", "NBulkheadCall")
                .set("id", model.id())
                .set("maxConcurrent", model.maxConcurrent())
                .set("backend", backend.getClass().getSimpleName())
                .set("metrics", backend.getMetrics(model.id()).describe())
                .build();
    }
}
