package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCancelException;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
    public NRetryCall<T> retryPeriod(NDuration period) {
        return retryPeriod(NRetryPeriodFunction.ofFixedPeriod(period));
    }

    @Override
    public NRetryCall<T> retryPeriod(NRetryPeriodFunction retryPeriod) {
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
    public NRetryCall<T> handler(NRetryHandler<T> handler) {
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
    public void close(){
        synchronized (store) {
            String oldId = model.id();
            store.delete(oldId);
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
                                throw NException.ofUncheckedException(ie);
                            }
                        }
                    }
                }
            }

            throw new NIllegalStateException(NMsg.ofC("Call [%s] ended in unexpected state: %s", id, model.status()));

        }
    }

    private T handleResultAndFinish(T result) {
        NRetryHandler<T> handler = (NRetryHandler<T>) model.handler();
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

    private NRetryResult<T> newCallResult() {
        T result = (T) model.result();
        Status status = model.status();
        return new NRetryResult<T>() {
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
                        throw NException.ofUncheckedException((Throwable) model.error());
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
    public Future<NRetryResult<T>> callFuture() {
        ExecutorService executor = NConcurrent.executorService(); // or your own
        return executor.submit(() -> {
            T result = call();
            return newCallResult();
        });
    }

}
