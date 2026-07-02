package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCancelException;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

public class NRetryCallImpl<T> implements NRetryCall<T> {
    private final NRetryCallStore store;
    private NRetryCallModel model;

    public NRetryCallImpl(String id, NCallable<T> callable, NRetryCallStore store) {
        this.store = store;
        this.model = new NRetryCallModel(id);
        this.model.caller(callable);
        reload();
    }

    public void reload() {
        synchronized (store) {
            String oldId = model.id();
            NCallable<?> oldCaller = model.caller();
            NRetryCallModel m = store.load(oldId);
            if (m == null) {
                NAssert.requireNamedNonNull(oldCaller, "caller");
                m = new NRetryCallModel(oldId);
                m.caller(oldCaller);
                store.save(m);
            } else {
                if (oldCaller != null) {
                    m.caller(oldCaller);
                    store.save(m);
                }
            }
            model = m;
        }
    }

    @Override
    public NRetryCall<T> maxRetries(int maxRetries) {
        synchronized (store) {
            maxRetries = Math.max(1, maxRetries);
            int old = model.maxRetries();
            if (old != maxRetries) {
                model.maxRetries(maxRetries);
                store.save(model);
            }
        }
        return this;
    }

    @Override
    public NRetryCall<T> multipliedRetryPeriod(NDuration basePeriod, double multiplier) {
        return retryPeriod(_retryMultipliedPeriod(basePeriod, multiplier));
    }

    @Override
    public NRetryCall<T> exponentialRetryPeriod(NDuration basePeriod, double multiplier) {
        return retryPeriod(_retryExponentialPeriod(basePeriod, multiplier));
    }

    @Override
    public NRetryCall<T> retryPeriod(NDuration period) {
        return retryPeriod(_retryFixedPeriods(period));
    }

    @Override
    public NRetryCall<T> retryPeriods(NDuration... periods) {
        return retryPeriod(_retryFixedPeriods(periods));
    }

    @Override
    public NRetryCall<T> retryPeriod(IntFunction<NDuration> retryPeriod) {
        synchronized (store) {
            model.retryPeriod(retryPeriod);
            store.save(model);
        }
        return this;
    }

    @Override
    public NRetryCall<T> recover(NCallable<T> recover) {
        synchronized (store) {
            model.recover(recover);
            store.save(model);
        }
        return this;
    }

    @Override
    public NRetryCall<T> handler(Handler<T> handler) {
        synchronized (store) {
            model.handler(handler);
            store.save(model);
        }
        return this;
    }

    @Override
    public T callOrElse(NCallable<T> recover) {
        try {
            return call();
        } catch (Exception ex) {
            if (recover != null) {
                return recover.call();
            }
            return null;
        }
    }

    @Override
    public T call() {
        synchronized (this) {
            String id = model.id();
            model = store.load(id);
            // prevent parallel execution
            if (model.status() == Status.RUNNING || model.status() == Status.HANDLING) {
                throw new NIllegalStateException(NMsg.ofC("Call [%s] is already running or handling.", id));
            }

            // if already fully completed, return the result immediately
            if (model.status() == Status.HANDLED || model.status() == Status.SUCCEEDED) {
                return (T) model.result();
            }
            if (model.status() == Status.CANCELLED) {
                throw new NCancelException(NMsg.ofC("Call %s cancelled", id));
            }

            int maxRetries = Math.max(1, model.maxRetries());
            int attempts = model.failedAttempts();

            while (attempts < maxRetries) {
                try {
                    // prepare for a new attempt
                    model.status(Status.RUNNING);
                    store.save(model);

                    // execute the main callable
                    T result = (T) model.caller().call();
                    model.result(result);
                    model.status(Status.SUCCEEDED);
                    store.save(model);

                    // proceed to handler if present
                    return handleResultAndFinish(result);

                } catch (Exception ex) {
                    attempts++;
                    model.failedAttempts(attempts);
                    model.error(ex);
                    model.status(Status.FAILED_ATTEMPT);
                    store.save(model);

                    if (attempts >= maxRetries) {
                        // final failure
                        model.status(Status.FAILED);
                        store.save(model);

                        // try recover if available
                        NCallable<T> recover = (NCallable<T>) model.recover();
                        if (recover != null) {
                            try {
                                T recovered = recover.call();
                                model.result(recovered);
                                model.status(Status.SUCCEEDED);
                                store.save(model);
                                return handleResultAndFinish(recovered);
                            } catch (Exception rex) {
                                model.error(rex);
                                store.save(model);
                                throw rex;
                            }
                        } else {
                            throw ex;
                        }
                    } else {
                        // retry delay if configured
                        NDuration wait = model.retryPeriod() != null
                                ? model.retryPeriod().apply(attempts)
                                : NDuration.ZERO;
                        if (!wait.isZero()) {
                            model.status(Status.RETRYING);
                            store.save(model);
                            try {
                                Thread.sleep(wait.toMillis());
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw NExceptions.ofUncheckedException(ie);
                            }
                        }
                    }
                }
            }

            throw new NIllegalStateException(NMsg.ofC("Call [%s] ended in unexpected state: %s", id, model.status()));

        }
    }

    private T handleResultAndFinish(T result) {
        Handler<T> handler = (Handler<T>) model.handler();
        if (handler != null) {
            try {
                model.status(Status.HANDLING);
                store.save(model);

                handler.handle(newCallResult());

                model.status(Status.HANDLED);
                store.save(model);
            } catch (Exception hx) {
                model.error(hx);
                model.status(Status.HANDLER_FAILED);
                store.save(model);
                throw hx;
            }
        } else {
            // no handler → mark as handled anyway
            model.status(Status.HANDLED);
            store.save(model);
        }
        return result;
    }

    private Result<T> newCallResult() {
        T result = (T) model.result();
        Status status = model.status();
        return new Result<T>() {
            @Override
            public String id() {
                return model.id();
            }

            @Override
            public NRetryCall<T> value() {
                return NRetryCallImpl.this;
            }

            @Override
            public boolean isValid() {
                return status == Status.SUCCEEDED;
            }

            @Override
            public boolean isError() {
                return status == Status.FAILED;
            }

            @Override
            public T result() {
                switch (status) {
                    case SUCCEEDED: {
                        return result;
                    }
                    case FAILED: {
                        throw NExceptions.ofUncheckedException((Throwable) model.error());
                    }
                    case RUNNING: {
                        throw new NIllegalStateException(NMsg.ofC("still running"));
                    }
                    case QUEUED: {
                        throw new NIllegalStateException(NMsg.ofC("still queued"));
                    }
                    case CREATED: {
                        throw new NIllegalStateException(NMsg.ofC("still created"));
                    }
                    case CANCELLED: {
                        throw new NCancelException(NMsg.ofC("cancelled"));
                    }
                    case FAILED_ATTEMPT: {
                        throw new NIllegalStateException(NMsg.ofC("still failed attempt"));
                    }
                    case RETRYING: {
                        throw new NIllegalStateException(NMsg.ofC("still retrying"));
                    }
                }
                return result;
            }
        };
    }

    @Override
    public void callAsync() {

    }

    @Override
    public Future<Result<T>> callFuture() {
        ExecutorService executor = NConcurrent.of().executorService(); // or your own
        return executor.submit(() -> {
            T result = call();
            return newCallResult();
        });
    }


    private IntFunction<NDuration> _retryFixedPeriods(NDuration... periods) {
        List<NDuration> all = new ArrayList<>();
        if (periods == null) {
            all.add(NDuration.ofMillis(0));
        } else {
            for (NDuration period : periods) {
                if (period != null) {
                    all.add(period);
                } else {
                    all.add(NDuration.ofMillis(0));
                }
            }
        }
        return new IntFunction<NDuration>() {
            @Override
            public NDuration apply(int i) {
                if (i < all.size()) {
                    return all.get(i);
                }
                return all.get(all.size() - 1);
            }
        };
    }

    private IntFunction<NDuration> _retryMultipliedPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return _retryFixedPeriods(NDuration.ofMillis(0));
        }
        return new IntFunction<NDuration>() {
            @Override
            public NDuration apply(int iteration) {
                return base.mul(multiplier * iteration);
            }
        };
    }

    private IntFunction<NDuration> _retryExponentialPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return _retryFixedPeriods(NDuration.ofMillis(0));
        }
        return new IntFunction<NDuration>() {
            @Override
            public NDuration apply(int iteration) {
                return base.mul(Math.pow(multiplier, iteration));
            }
        };
    }
}
