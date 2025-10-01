package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NCancelException;
import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.NIllegalStateException;
import net.thevpc.nuts.NScopedValue;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

public class NRetryCallImpl<T> implements NRetryCall<T> {
    private NBeanContainer beanContainer;
    private NRetryCallStore store;
    private NRetryCallModel model;

    public NRetryCallImpl(String id, NCallable<T> callable,NBeanContainer beanContainer, NRetryCallStore store) {
        this.beanContainer = beanContainer;
        this.store = store;
        this.model = new NRetryCallModel(id);
        this.model.setCaller(callable);
        reload();
    }

    public void reload() {
        synchronized (this) {
            String oldId=model.getId();
            NCallable<?> oldCaller = model.getCaller();
            NScopedValue<NBeanContainer> c = NBeanContainer.current();
            NBeanContainer currContainer = beanContainer == null ? c.get() : beanContainer;
            NCallable<NRetryCallModel> cc = () -> {
                NRetryCallModel m = store.load(oldId);
                if (m == null) {
                    NAssert.requireNonNull(oldCaller,"caller");
                    m = new NRetryCallModel(oldId);
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
            model = c == null ? cc.call() : c.callWith(currContainer, cc);
        }
    }

    @Override
    public NRetryCall<T> setMaxRetries(int maxRetries) {
        model.setMaxRetries(maxRetries);
        store.save(model);
        return this;
    }

    @Override
    public NRetryCall<T> setRetryPeriod(IntFunction<Duration> retryPeriod) {
        model.setRetryPeriod(retryPeriod);
        store.save(model);
        return this;
    }

    @Override
    public NRetryCall<T> setRecover(NCallable<T> recover) {
        model.setRecover(recover);
        store.save(model);
        return this;
    }

    @Override
    public NRetryCall<T> setHandler(Handler<T> handler) {
        model.setHandler(handler);
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
        synchronized (this) {
            String id = model.getId();
            model = store.load(id);
            // prevent parallel execution
            if (model.getStatus() == Status.RUNNING || model.getStatus() == Status.HANDLING) {
                throw new NIllegalStateException(NMsg.ofC("Call [%s] is already running or handling.", id));
            }

            // if already fully completed, return the result immediately
            if (model.getStatus() == Status.HANDLED || model.getStatus() == Status.SUCCEEDED) {
                return (T) model.getResult();
            }
            if (model.getStatus() == Status.CANCELLED) {
                throw new NCancelException(NMsg.ofC("Call %s cancelled", id));
            }

            int maxRetries = Math.max(1, model.getMaxRetries());
            int attempts = model.getFailedAttempts();

            while (attempts < maxRetries) {
                try {
                    // prepare for a new attempt
                    model.setStatus(Status.RUNNING);
                    store.save(model);

                    // execute the main callable
                    T result = (T) model.getCaller().call();
                    model.setResult(result);
                    model.setStatus(Status.SUCCEEDED);
                    store.save(model);

                    // proceed to handler if present
                    return handleResultAndFinish(result);

                } catch (Exception ex) {
                    attempts++;
                    model.setFailedAttempts(attempts);
                    model.setThrowable(ex);
                    model.setStatus(Status.FAILED_ATTEMPT);
                    store.save(model);

                    if (attempts >= maxRetries) {
                        // final failure
                        model.setStatus(Status.FAILED);
                        store.save(model);

                        // try recover if available
                        NCallable<T> recover = (NCallable<T>) model.getRecover();
                        if (recover != null) {
                            try {
                                T recovered = recover.call();
                                model.setResult(recovered);
                                model.setStatus(Status.SUCCEEDED);
                                store.save(model);
                                return handleResultAndFinish(recovered);
                            } catch (Exception rex) {
                                model.setThrowable(rex);
                                store.save(model);
                                throw rex;
                            }
                        } else {
                            throw ex;
                        }
                    } else {
                        // retry delay if configured
                        Duration wait = model.getRetryPeriod() != null
                                ? model.getRetryPeriod().apply(attempts)
                                : Duration.ZERO;
                        if (!wait.isZero()) {
                            model.setStatus(Status.RETRYING);
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

            throw new NIllegalStateException(NMsg.ofC("Call [%s] ended in unexpected state: %s", id, model.getStatus()));

        }
    }

    private T handleResultAndFinish(T result) {
        Handler<T> handler = (Handler<T>) model.getHandler();
        if (handler != null) {
            try {
                model.setStatus(Status.HANDLING);
                store.save(model);

                handler.handle(newCallResult());

                model.setStatus(Status.HANDLED);
                store.save(model);
            } catch (Exception hx) {
                model.setThrowable(hx);
                model.setStatus(Status.HANDLER_FAILED);
                store.save(model);
                throw hx;
            }
        } else {
            // no handler → mark as handled anyway
            model.setStatus(Status.HANDLED);
            store.save(model);
        }
        return result;
    }

    private Result<T> newCallResult() {
        T result = (T) model.getResult();
        Status status = model.getStatus();
        return new Result<T>() {
            @Override
            public String id() {
                return model.getId();
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
                        return (T) result;
                    }
                    case FAILED: {
                        throw NExceptions.ofUncheckedException((Throwable) model.getThrowable());
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
}
