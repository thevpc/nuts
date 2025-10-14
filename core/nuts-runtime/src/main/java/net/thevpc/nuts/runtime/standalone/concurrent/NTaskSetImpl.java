package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NTaskSet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NTaskSetImpl implements NTaskSet {
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();
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
        if(future==null){
            return this;
        }
        if (future instanceof CompletableFuture) {
            return add((CompletableFuture<?>) future);
        }
        return add(CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        },pickExecutor(executor)));
    }

    @Override
    public NTaskSet add(CompletableFuture<?> future) {
        if(future==null){
           return this;
        }
        futures.add(future);
        return this;
    }

    @Override
    public NTaskSet supply(Supplier<?> supplier) {
        return supply(supplier, executor);
    }

    @Override
    public NTaskSet supply(Supplier<?> supplier, ExecutorService exec) {
        CompletableFuture<?> f = CompletableFuture.supplyAsync(supplier, pickExecutor(exec));
        return add(f);
    }

    @Override
    public NTaskSet run(Runnable task) {
        return run(task, executor);
    }

    @Override
    public NTaskSet run(Runnable task, ExecutorService exec) {
        if(task==null){
            return this;
        }
        CompletableFuture<?> f = CompletableFuture.runAsync(task, pickExecutor(exec));
        return add(f);
    }

    @Override
    public NTaskSet call(Callable<?> task) {
        return call(task, executor);
    }

    @Override
    public NTaskSet call(Callable<?> task, ExecutorService exec) {
        if(task==null){
            return this;
        }
        CompletableFuture<?> f = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, pickExecutor(exec));
        return add(f);
    }

    @Override
    public NTaskSet call(NCallable<?> task) {
        return call(task, executor);
    }

    @Override
    public NTaskSet call(NCallable<?> task, ExecutorService exec) {
        if(task==null){
            return this;
        }
        return call((Callable<?>) task::call, exec);
    }

    @Override
    public NTaskSet join() {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return this;
    }

    @Override
    public <T> T first() {
        return first(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T first(boolean cancelOthers) {
        CompletableFuture<?> any = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
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
                .map(f -> (CompletableFuture<T>) f)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletableFuture<?>> futures() {
        return Collections.unmodifiableList(futures);
    }

    @Override
    public List<?> results() {
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> results(Class<T> type) {
        return futures.stream()
                .map(f -> {
                    try {
                        return (T) f.get();
                    } catch (Exception e) {
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<Throwable> errors() {
        return futures.stream()
                .map(f -> {
                    try {
                        f.get();
                        return null;
                    } catch (ExecutionException e) {
                        return e.getCause();
                    } catch (Exception e) {
                        return e;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDone() {
        return futures.stream().allMatch(CompletableFuture::isDone);
    }

    @Override
    public boolean hasError() {
        return !errors().isEmpty();
    }

    @Override
    public NTaskSet cancelAll(boolean mayInterrupt) {
        futures.forEach(f -> f.cancel(mayInterrupt));
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
}
