package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;

import java.time.Duration;
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
            String oldId=model.getId();
            NCallable<?> oldCaller = model.getCaller();
            NCallable<NCircuitBreakerCallModel> cc = () -> {
                NCircuitBreakerCallModel m = store.load(oldId);
                if (m == null) {
                    NAssert.requireNonNull(oldCaller,"caller");
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
    public NCircuitBreakerCall<T> setFailureThreshold(int failureThreshold) {
        model.setFailureThreshold(failureThreshold);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> setSuccessThreshold(int successThreshold) {
        model.setSuccessThreshold(successThreshold);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> setSuccessRetryPeriod(IntFunction<Duration> retryPeriod) {
        model.setSuccessRetryPeriod(retryPeriod);
        store.save(model);
        return this;
    }

    @Override
    public NCircuitBreakerCall<T> setFailureRetryPeriod(IntFunction<Duration> retryPeriod) {
        model.setFailureRetryPeriod(retryPeriod);
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
                    long openDelay = model.getFailureRetryPeriod() != null
                            ? model.getFailureRetryPeriod().apply(model.getFailureCount()).toMillis()
                            : 5000; // fallback default

                    if (now - model.getOpenTimestamp() >= openDelay) {
                        model.setStatus(Status.HALF_OPEN);
                        model.setSuccessCount(0);
                    } else if(useFallback && model.getLastValidResult() != null){
                        return (T) model.getLastValidResult();
                    } else {
                        // optionally return last valid result instead of throwing
                        throw new IllegalStateException("Circuit is OPEN, wait " + (openDelay - (now - model.getOpenTimestamp())) + "ms");
                    }
                    break;

                case HALF_OPEN:
                    long successDelay = model.getSuccessRetryPeriod() != null
                            ? model.getSuccessRetryPeriod().apply(model.getSuccessCount()).toMillis()
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
                if (useFallback && model.getLastValidResult() != null) {
                    return (T) model.getLastValidResult();
                }
                throw ex;
            }
        }
    }

    private void onSuccess(Object result) {
        switch (model.getStatus()) {
            case HALF_OPEN:
                model.setSuccessCount(model.getSuccessCount() + 1);
                if (model.getSuccessCount() >= model.getSuccessThreshold()) {
                    model.setStatus(Status.CLOSED);
                    model.setFailureCount(0);
                }
                break;
            case CLOSED:
                model.setFailureCount(0); // reset on success
                break;
        }
        model.setLastValidResult(result);
    }

    private void onFailure(Throwable ex) {
        model.setThrowable(ex);
        switch (model.getStatus()) {
            case HALF_OPEN:
            case CLOSED:
                model.setFailureCount(model.getFailureCount() + 1);
                if (model.getFailureCount() >= model.getFailureThreshold()) {
                    model.setStatus(Status.OPEN);
                    model.setOpenTimestamp(System.currentTimeMillis());
                }
                break;
            case OPEN:
                // already open, do nothing
                break;
        }
    }


}
