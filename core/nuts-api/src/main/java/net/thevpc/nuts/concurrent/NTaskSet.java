package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A {@code NTaskSet} allows you to submit, track, and coordinate multiple asynchronous tasks
 * in a single logical set. Tasks can be submitted as {@link Future}, {@link CompletableFuture},
 * {@link Supplier}, {@link Runnable}, {@link Callable}, or {@link NCallable}.
 *
 * <p>This interface supports:
 * <ul>
 *     <li>Waiting for all tasks to complete ({@link #join()})</li>
 *     <li>Retrieving the first completed task ({@link #first()} or {@link #firstOnly()})</li>
 *     <li>Accessing all results or errors individually ({@link #results()}, {@link #errors()})</li>
 *     <li>Cancelling all tasks ({@link #cancelAll(boolean)})</li>
 *     <li>Fluent chaining for convenient submission and execution</li>
 * </ul>
 *
 * <p>By default, tasks use the executor provided via {@link #executorService(ExecutorService)}
 * or {@link NConcurrent#executorService()} if none is set.
 *
 * <p>Example usage:
 * <pre>{@code
 * NTaskSet tasks = NTaskSet.of()
 *     .call(() -> doWork(1))
 *     .call(() -> doWork(2))
 *     .supply(() -> computeValue())
 *     .run(() -> cleanup());
 *
 * // Wait for all tasks and throw if any failed
 * tasks.requireAll();
 *
 * // Get all results
 * List<?> results = tasks.results();
 * }</pre>
 */
public interface NTaskSet {

    /**
     * Creates a new empty task set.
     *
     * @return a new {@code NTaskSet} instance
     */
    static NTaskSet of() {
        return NConcurrent.of().taskSet();
    }


    /**
     * Sets the default executor for tasks that do not explicitly provide one.
     * If no executor is set, {@link NConcurrent#executorService()} is used.
     *
     * @param executor the executor service to use
     * @return this task set for fluent chaining
     */
    NTaskSet executorService(ExecutorService executor);


    /**
     * Adds an existing {@link Future} to the task set.
     *
     * @param future the future to track
     * @return this task set
     */
    NTaskSet add(Future<?> future);

    /**
     * Adds an existing {@link Future} to the task set.
     *
     * @param taskId task id or null
     * @param future the future to track
     * @return this task set
     */
    NTaskSet add(String taskId, Future<?> future);

    /**
     * Adds an existing {@link CompletableFuture} to the task set.
     *
     * @param future the future to track
     * @return this task set
     */
    NTaskSet add(CompletableFuture<?> future);

    /**
     * Adds an existing {@link CompletableFuture} to the task set.
     *
     * @param taskId task id or null
     * @param future the future to track
     * @return this task set
     */
    NTaskSet add(String taskId,CompletableFuture<?> future);

    /**
     * Submits a {@link Supplier} to be executed asynchronously.
     *
     * @param supplier the supplier to execute
     * @return this task set
     */
    NTaskSet supply(Supplier<?> supplier);

    /**
     * @param taskId task id or null
     * Submits a {@link Supplier} to be executed asynchronously.
     *
     * @param supplier the supplier to execute
     * @return this task set
     */
    NTaskSet supply(String taskId,Supplier<?> supplier);

    /**
     * Submits a {@link Supplier} to be executed asynchronously using the provided executor.
     *
     * @param supplier the supplier to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet supply(Supplier<?> supplier, ExecutorService executor);

    /**
     * Submits a {@link Supplier} to be executed asynchronously using the provided executor.
     *
     * @param taskId task id or null
     * @param supplier the supplier to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet supply(String taskId, Supplier<?> supplier, ExecutorService executor);

    /**
     * Submits a {@link Runnable} to be executed asynchronously.
     *
     * @param task the runnable to execute
     * @return this task set
     */
    NTaskSet run(Runnable task);

    /**
     * Submits a {@link Runnable} to be executed asynchronously.
     *
     * @param taskId task id or null
     * @param task the runnable to execute
     * @return this task set
     */
    NTaskSet run(String taskId,Runnable task);

    /**
     * Submits a {@link Runnable} to be executed asynchronously using the provided executor.
     *
     * @param task the runnable to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet run(Runnable task, ExecutorService executor);

    /**
     * Submits a {@link Runnable} to be executed asynchronously using the provided executor.
     *
     * @param taskId task id or null
     * @param task the runnable to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet run(String taskId,Runnable task, ExecutorService executor);

    /**
     * Submits a {@link Callable} to be executed asynchronously.
     *
     * @param task the callable to execute
     * @return this task set
     */
    NTaskSet call(Callable<?> task);

    /**
     * Submits a {@link Callable} to be executed asynchronously.
     *
     * @param taskId task id or null
     * @param task the callable to execute
     * @return this task set
     */
    NTaskSet call(String taskId,Callable<?> task);

    /**
     * Submits a {@link Callable} to be executed asynchronously using the provided executor.
     *
     * @param task the callable to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet call(Callable<?> task, ExecutorService executor);

    NTaskSet call(String taskId, Callable<?> task, ExecutorService exec);

    /**
     * Submits an {@link NCallable} to be executed asynchronously.
     *
     * @param task the NCallable to execute
     * @return this task set
     */
    NTaskSet call(NCallable<?> task);

    /**
     * Submits an {@link NCallable} to be executed asynchronously using the provided executor.
     *
     * @param task the NCallable to execute
     * @param executor the executor to use
     * @return this task set
     */
    NTaskSet call(NCallable<?> task, ExecutorService executor);

    /**
     * Waits for all tasks in this set to complete.
     *
     * @return this task set
     */
    NTaskSet join();

    /**
     * Returns the result of the first task that completes successfully.
     * Other tasks remain running.
     *
     * @param <T> the result type
     * @return the first completed result
     */
    <T> T first();

    /**
     * Returns the result of the first task that completes successfully.
     * Optionally cancels remaining tasks.
     *
     * @param <T> the result type
     * @param cancelOthers if true, cancels all other unfinished tasks
     * @return the first completed result
     */
    <T> T first(boolean cancelOthers);

    /**
     * Returns the result of the first task and cancels all other unfinished tasks.
     *
     * @param <T> the result type
     * @return the first completed result
     */
    <T> T firstOnly();

    /**
     * Returns all tracked futures, cast to the given type.
     *
     * @param <T> the result type
     * @param type the class type to cast
     * @return a list of futures
     */
    <T> List<CompletableFuture<T>> futures(Class<T> type);


    /**
     * Returns all tracked futures as a list of {@link CompletableFuture}.
     *
     * @return unmodifiable list of futures
     */
    List<CompletableFuture<?>> futures();

    /**
     * Returns results of all tasks.
     * Failed tasks will return null for their result.
     *
     * @return list of results
     */
    <T> List<NTaskResult<T>> results();

    /**
     * Returns results of all tasks cast to the given type.
     * Failed tasks will return null for their result.
     *
     * @param <T> the result type
     * @param type the class type
     * @return list of results
     */
    <T> List<NTaskResult<T>> results(Class<T> type);

    /**
     * Returns a list of all exceptions thrown by tasks.
     *
     * @return list of errors
     */
    List<Throwable> errors();

    /**
     * Returns true if all tasks in this set are done.
     *
     * @return true if all tasks completed
     */
    boolean isDone();

    /**
     * Returns true if any task in this set failed.
     *
     * @return true if there was at least one error
     */
    boolean hasError();

    /**
     * Cancels all tasks in this set.
     *
     * @param mayInterrupt true if running threads should be interrupted
     * @return this task set
     */
    NTaskSet cancelAll(boolean mayInterrupt);

    /**
     * Clears all tracked tasks from this set.
     *
     * @return this task set
     */
    NTaskSet clear();

    /**
     * Waits for all tasks to complete successfully. If any task fails, the first
     * encountered exception is thrown.
     *
     * @return this task set
     * @throws CompletionException if any task failed
     */
    NTaskSet requireAll() throws CompletionException;

    /**
     * Blocking version: returns the first result matching the predicate.
     * @param predicate the match condition
     * @param cancelOthers if true, cancels remaining tasks once match is found
     * @param <T> type of result
     * @return NOptional of the first match, or empty if none match
     */
    <T> NOptional<NTaskResult<T>> firstMatch(Predicate<NTaskResult<T>> predicate, boolean cancelOthers) ;

    /**
     * Non-blocking async version: returns CompletableFuture of first matching result
     * @param predicate the match condition
     * @param cancelOthers if true, cancels remaining tasks once match is found
     * @param <T> type of result
     * @return CompletableFuture of NOptional matching result
     */
    <T> CompletableFuture<NOptional<NTaskResult<T>>> firstMatchAsync(Predicate<NTaskResult<T>> predicate, boolean cancelOthers) ;

}
