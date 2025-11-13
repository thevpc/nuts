package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NTaskResult;
import net.thevpc.nuts.concurrent.NTaskSet;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NTaskSetImpl implements NTaskSet {
    private final List<IdAndFuture> futures = new CopyOnWriteArrayList<>();
    private ExecutorService executor;

    public NTaskSetImpl() {
    }

    @Override
    public NTaskSet executorService(ExecutorService executor) {
        this.executor = executor;
        return this;
    }


    @Override
    public NTaskSet add(Future<?> future) {
        return add(null, future);
    }

    @Override
    public NTaskSet add(String taskId, Future<?> future) {
        if (future == null) {
            return this;
        }
        if (future instanceof CompletableFuture) {
            return add(taskId, (CompletableFuture<?>) future);
        }
        return add(taskId, CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, pickExecutor(executor)));
    }

    @Override
    public NTaskSet add(CompletableFuture<?> future) {
        return add(null, future);
    }

    @Override
    public NTaskSet add(String taskId, CompletableFuture<?> future) {
        if (future == null) {
            return this;
        }
        futures.add(new IdAndFuture(taskId, future));
        return this;
    }

    @Override
    public NTaskSet supply(Supplier<?> supplier) {
        return supply(null, supplier, executor);
    }

    @Override
    public NTaskSet supply(String taskId, Supplier<?> supplier) {
        return supply(taskId, supplier, executor);
    }

    @Override
    public NTaskSet supply(Supplier<?> supplier, ExecutorService exec) {
        return supply(null, supplier, exec);
    }

    @Override
    public NTaskSet supply(String taskId, Supplier<?> supplier, ExecutorService executor) {
        CompletableFuture<?> f = CompletableFuture.supplyAsync(supplier, pickExecutor(executor));
        return add(taskId, f);
    }

    @Override
    public NTaskSet run(Runnable task) {
        return run(null, task, executor);
    }

    @Override
    public NTaskSet run(String taskId, Runnable task) {
        return run(taskId, task, executor);
    }

    @Override
    public NTaskSet run(Runnable task, ExecutorService exec) {
        return run(null, task, exec);
    }

    @Override
    public NTaskSet run(String taskId, Runnable task, ExecutorService executor) {
        if (task == null) {
            return this;
        }
        CompletableFuture<?> f = CompletableFuture.runAsync(task, pickExecutor(executor));
        return add(taskId, f);
    }

    @Override
    public NTaskSet call(Callable<?> task) {
        return call(null,task, executor);
    }

    @Override
    public NTaskSet call(String taskId,Callable<?> task) {
        return call(taskId,task, executor);
    }

    @Override
    public NTaskSet call(Callable<?> task, ExecutorService exec) {
        return call(null, task, exec);
    }

    @Override
    public NTaskSet call(String taskId, Callable<?> task, ExecutorService exec) {
        if (task == null) {
            return this;
        }
        CompletableFuture<?> f = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, pickExecutor(exec));
        return add(taskId, f);
    }

    @Override
    public NTaskSet call(NCallable<?> task) {
        return call(task, executor);
    }

    @Override
    public NTaskSet call(NCallable<?> task, ExecutorService exec) {
        if (task == null) {
            return this;
        }
        return call((Callable<?>) task::call, exec);
    }

    @Override
    public NTaskSet join() {
        CompletableFuture.allOf(futures.stream().map(x->x.future).toArray(CompletableFuture[]::new)).join();
        return this;
    }

    @Override
    public <T> T first() {
        return first(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T first(boolean cancelOthers) {
        CompletableFuture<?> any = CompletableFuture.anyOf(futures.stream().map(x->x.future).toArray(CompletableFuture[]::new));
        T result = (T) any.join();
        if (cancelOthers) {
            cancelAll(true);
        }
        return result;
    }

    @Override
    public <T> T firstOnly() {
        return first(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<CompletableFuture<T>> futures(Class<T> type) {
        return futures.stream()
                .map(f -> (CompletableFuture<T>) f.future)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletableFuture<?>> futures() {
        return (List) futures.stream()
                .map(f -> (CompletableFuture) f.future)
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<NTaskResult<T>> results() {
        return futures.stream()
                .map(f -> f.<T>get()).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<NTaskResult<T>> results(Class<T> type) {
        return futures.stream()
                .map(f -> f.<T>get()).collect(Collectors.toList());
    }

    @Override
    public List<Throwable> errors() {
        return this.<Object>results().stream().filter(x->x.isError())
                .map(x->x.getError())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDone() {
        return futures.stream().allMatch(x->x.future.isDone());
    }

    @Override
    public boolean hasError() {
        return !errors().isEmpty();
    }

    @Override
    public NTaskSet cancelAll(boolean mayInterrupt) {
        futures.forEach(f -> f.future.cancel(mayInterrupt));
        return this;
    }

    private ExecutorService pickExecutor(ExecutorService exec) {
        if (exec != null) return exec;
        if (executor != null) return executor;
        return NConcurrent.of().executorService();
    }

    @Override
    public NTaskSet clear() {
        futures.clear();
        return this;
    }

    public NTaskSet requireAll() {
        join();
        List<Throwable> errors = errors();
        if (!errors.isEmpty()) {
            Throwable first = errors.get(0);
            if (first instanceof RuntimeException) {
                throw (RuntimeException) first;
            }
            throw new CompletionException(first);
        }
        return this;
    }


    public <T> NOptional<NTaskResult<T>> firstMatch(Predicate<NTaskResult<T>> predicate, boolean cancelOthers) {
        Objects.requireNonNull(predicate);

        CompletableFuture<NTaskResult<T>> resultFuture = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(futures.size());

        for (IdAndFuture cf : futures) {
            @SuppressWarnings("unchecked")
            CompletableFuture<T> typed = (CompletableFuture<T>) cf.future;

            typed.whenComplete((r, ex) -> {
                if (!resultFuture.isDone()) {
                    NTaskResult<T> tr = (ex == null)
                            ? NTaskResult.ofSuccess(cf.id,r)
                            : NTaskResult.ofError(cf.id,(ex instanceof CompletionException && ((CompletionException) ex).getCause()!=null) ? ((CompletionException) ex).getCause() : ex);

                    boolean match = false;
                    try {
                        match = predicate.test(tr);
                    } catch (Exception e) {
                        resultFuture.complete(NTaskResult.ofError(cf.id,e));
                        if (cancelOthers) cancelAll(true);
                        return;
                    }

                    if (match) {
                        resultFuture.complete(tr);
                        if (cancelOthers) cancelAll(true);
                        return;
                    }

                    // No match: check if all tasks are completed
                    if (remaining.decrementAndGet() == 0) {
                        resultFuture.complete(null); // signal no match
                    }
                }
            });
        }

        try {
            NTaskResult<T> res = resultFuture.get();
            return res == null ? NOptional.ofEmpty() : NOptional.of(res);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NOptional.ofEmpty();
        } catch (ExecutionException e) {
            return NOptional.of(NTaskResult.ofError(null, e.getCause()));
        }
    }


    public <T> CompletableFuture<NOptional<NTaskResult<T>>> firstMatchAsync(Predicate<NTaskResult<T>> predicate, boolean cancelOthers) {
        Objects.requireNonNull(predicate);
        CompletableFuture<NTaskResult<T>> resultFuture = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(futures.size());

        for (IdAndFuture cf : futures) {
            @SuppressWarnings("unchecked")
            CompletableFuture<T> typed = (CompletableFuture<T>) cf.future;

            typed.whenComplete((r, ex) -> {
                if (!resultFuture.isDone()) {
                    NTaskResult<T> tr = (ex == null)
                            ? NTaskResult.ofSuccess(cf.id,r)
                            : NTaskResult.ofError(cf.id,ex);

                    boolean match = false;
                    try {
                        match = predicate.test(tr);
                    } catch (Exception e) {
                        resultFuture.completeExceptionally(e);
                        if (cancelOthers) cancelAll(true);
                        return;
                    }

                    if (match) {
                        resultFuture.complete(tr);
                        if (cancelOthers) cancelAll(true);
                        return;
                    }

                    if (remaining.decrementAndGet() == 0) {
                        resultFuture.complete(null);
                    }
                }
            });
        }

        return resultFuture.thenApply(res -> res == null ? NOptional.<NTaskResult<T>>ofEmpty() : NOptional.of(res));
    }

    private static class IdAndFuture {
        String id;
        CompletableFuture<?> future;

        public IdAndFuture(String id, CompletableFuture<?> future) {
            this.id = NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id;
            this.future = future;
        }

        public <T> NTaskResult<T> get(){
            try {
                return NTaskResult.<T>ofSuccess(id,(T)future.get());
            } catch (ExecutionException e) {
                if(e.getCause()!=null){
                    return NTaskResult.<T>ofError(id,e.getCause());
                }
                return NTaskResult.<T>ofError(id,e);
            } catch (Exception e) {
                return NTaskResult.<T>ofError(id,e);
            }
        }
    }

}
