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
        this.model.setCaller(callable);
        reload();
    }

    public void reload() {
        synchronized (this) {
            String oldId = model.getId();
            NCallable<?> oldCaller = model.getCaller();


            NBulkheadCallModel m = store.load(oldId);
            if (m == null) {
                NAssert.requireNonNull(oldCaller, "caller");
                m = new NBulkheadCallModel(oldId);
                m.setMaxConcurrent(1);
                m.setCaller(oldCaller);
                store.save(m);
            } else {
                if (oldCaller != null) {
                    m.setCaller(oldCaller);
                    if(m.getMaxConcurrent()<=0){
                        m.setMaxConcurrent(1);
                    }
                    store.save(m);
                }else{
                    if(m.getMaxConcurrent()<=0){
                        m.setMaxConcurrent(1);
                        store.save(m);
                    }
                }
            }
            model= m;
        }
    }

    @Override
    public NBulkheadCall<T> setMaxConcurrent(int maxConcurrent) {
        maxConcurrent=Math.max(1, maxConcurrent);
        int old = model.getMaxConcurrent();
        if(old!=maxConcurrent) {
            model.setMaxConcurrent(maxConcurrent);
            store.save(model);
        }
        return this;
    }

    @Override
    public NBulkheadCall<T> setPermitExpiry(NDuration expiry) {
        NDuration old = model.getPermitExpiry();
        if(Objects.equals(old,expiry)){
            model.setPermitExpiry(expiry);
            store.save(model);
        }
        return this;
    }

    @Override
    public int getMaxConcurrent() {
        return model.getMaxConcurrent();
    }

    @Override
    public int getActiveCalls() {
        return backend.getMetrics(model.getId()).getActiveCalls();
    }

    @Override
    public int getAvailableSlots() {
        return backend.getMetrics(model.getId()).getAvailableSlots();
    }

    @Override
    public boolean isFull() {
        return backend.getMetrics(model.getId()).isFull();
    }

    // ==================== Core Execution Methods ====================

    @Override
    public NOptional<T> tryCall() {
        // Step 1: Try to acquire a permit from the backend
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.getId(), Math.max(1,model.getMaxConcurrent()));

        // Step 2: If no permit available, return empty (rejected)
        if (permitOpt.isEmpty()) {
            return NOptional.ofEmpty();
        }

        // Step 3: We got a permit! Execute the callable
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            T result = ((NCallable<T>)model.getCaller()).call();
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
                backend.tryAcquire(model.getId(), model.getMaxConcurrent(), null);

        if (permitOpt.isEmpty()) {
            throw new NInterruptedException(NMsg.ofC("Failed to acquire bulkhead permit"));
        }

        // Step 2: Execute with permit
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.getCaller()).call();
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
                backend.tryAcquire(model.getId(), model.getMaxConcurrent(), timeout);

        // Step 2: If timeout expired, throw exception
        if (permitOpt.isEmpty()) {
            throw new NTimeoutException(NMsg.ofC("Timeout waiting for bulkhead permit after %s", timeout));
        }

        // Step 3: Execute with permit
        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.getCaller()).call();
        } finally {
            backend.release(permit);
        }
    }

    @Override
    public T callOrElse(NDuration timeout, NCallable<T> fallback) throws NInterruptedException {
        NOptional<NBulkheadCallBackend.NBulkheadPermit> permitOpt =
                backend.tryAcquire(model.getId(), model.getMaxConcurrent(), timeout);

        if (permitOpt.isEmpty()) {
            // Timeout expired, use fallback
            return fallback.call();
        }

        NBulkheadCallBackend.NBulkheadPermit permit = permitOpt.get();
        try {
            return ((NCallable<T>)model.getCaller()).call();
        } finally {
            backend.release(permit);
        }
    }


    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .set("type", "NBulkheadCall")
                .set("id", model.getId())
                .set("maxConcurrent", model.getMaxConcurrent())
                .set("backend", backend.getClass().getSimpleName())
                .set("metrics", backend.getMetrics(model.getId()).describe())
                .build();
    }
}
