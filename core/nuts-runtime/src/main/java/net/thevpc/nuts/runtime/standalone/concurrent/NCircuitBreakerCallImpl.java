package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.function.IntFunction;

public class NCircuitBreakerCallImpl<T> implements NCircuitBreakerCall<T> {
    private NBeanContainer beanContainer;
    private NCircuitBreakerCallStore store;
    private NCircuitBreakerCallModel model;

    public NCircuitBreakerCallImpl(String id, NCallable<T> callable, NBeanContainer beanContainer, NCircuitBreakerCallStore store) {
        this.beanContainer = beanContainer;
        this.store = store;
        this.model = new NCircuitBreakerCallModel(id);
        this.model.setCaller(callable);
        reload();
    }

    public void reload() {
        synchronized (this) {
            String oldId=model.id();
            NCallable<?> oldCaller = model.getCaller();
            NCallable<NCircuitBreakerCallModel> cc = () -> {
                NCircuitBreakerCallModel m = store.load(oldId);
                if (m == null) {
                    NAssert.requireNamedNonNull(oldCaller,"caller");
                    m = new NCircuitBreakerCallModel(oldId);
                    m.setCaller(oldCaller);
                    store.save(m);
                }else{
                    if(oldCaller!=null) {
                        m.setCaller(oldCaller);
                        store.save(m);
                    }
                }
                return m;
            };
            model = NBeanContainer.scopedStack().callWith(beanContainer, cc);
        }
    }

    @Override
    public NCircuitBreakerCall<T> failureThreshold(int failureThreshold) {
        model.failureThreshold(failureThreshold);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> successThreshold(int successThreshold) {
        model.successThreshold(successThreshold);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> successRetryPeriod(IntFunction<NDuration> retryPeriod) {
        model.successRetryPeriod(retryPeriod);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> failureRetryPeriod(IntFunction<NDuration> retryPeriod) {
        model.failureRetryPeriod(retryPeriod);
        store.save(model);
        return this;
    }

    @Override
    public T callOrElse(NCallable<T> recover) {
        try {
            return call();
        }catch (Exception ex) {
            if(recover!=null) {
                return recover.call();
            }
            return null;
        }
    }

    @Override
    public T call() {
        return call(false);
    }

    @Override
    public T callOrLast() {
        return call(true);
    }

    public T call(boolean useFallback) {
        synchronized (this) {
            long now = System.currentTimeMillis();

            switch (model.getStatus()) {
                case OPEN:
                    long openDelay = model.failureRetryPeriod() != null
                            ? model.failureRetryPeriod().apply(model.failureCount()).toMillis()
                            : 5000; // fallback default

                    if (now - model.openTimestamp() >= openDelay) {
                        model.setStatus(Status.HALF_OPEN);
                        model.successCount(0);
                    } else if(useFallback && model.lastValidResult() != null){
                        return (T) model.lastValidResult();
                    } else {
                        // optionally return last valid result instead of throwing
                        throw new IllegalStateException("Circuit is OPEN, wait " + (openDelay - (now - model.openTimestamp())) + "ms");
                    }
                    break;

                case HALF_OPEN:
                    long successDelay = model.successRetryPeriod() != null
                            ? model.successRetryPeriod().apply(model.successCount()).toMillis()
                            : 0;

                    if (successDelay > 0) {
                        try {
                            Thread.sleep(successDelay); // synchronous wait
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }
                    break;

                case CLOSED:
                    // nothing special, proceed
                    break;
            }

            try {
                T result = (T) model.getCaller().call();
                onSuccess(result);
                return result;
            } catch (Exception ex) {
                onFailure(ex);
                if (useFallback && model.lastValidResult() != null) {
                    return (T) model.lastValidResult();
                }
                throw ex;
            }
        }
    }

    private void onSuccess(Object result) {
        switch (model.getStatus()) {
            case HALF_OPEN:
                model.successCount(model.successCount() + 1);
                if (model.successCount() >= model.successThreshold()) {
                    model.setStatus(Status.CLOSED);
                    model.failureCount(0);
                }
                break;
            case CLOSED:
                model.failureCount(0); // reset on success
                break;
        }
        model.lastValidResult(result);
    }

    private void onFailure(Throwable ex) {
        model.setError(ex);
        switch (model.getStatus()) {
            case HALF_OPEN:
            case CLOSED:
                model.failureCount(model.failureCount() + 1);
                if (model.failureCount() >= model.failureThreshold()) {
                    model.setStatus(Status.OPEN);
                    model.openTimestamp(System.currentTimeMillis());
                }
                break;
            case OPEN:
                // already open, do nothing
                break;
        }
    }


}
