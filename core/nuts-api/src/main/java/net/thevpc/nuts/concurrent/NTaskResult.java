package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NExceptions;

/**
 * Represents the result of a task execution, which can either be a successful result
 * or an error. Each result is associated with a unique task identifier.
 *
 * @param <T> the type of the result value
 */
public class NTaskResult<T> {

    /** The unique identifier of the task. */
    private final String taskId;
    /** The result of the task if it completed successfully; null if there was an error. */
    private final T result;

    /** The exception thrown during task execution if the task failed; null if successful. */
    private final Throwable exception;

    /**
     * Private constructor to create a task result.
     *
     * @param taskId the unique identifier of the task
     * @param result the result of the task if successful
     * @param exception the exception thrown if the task failed
     */
    private NTaskResult(String taskId, T result, Throwable exception) {
        this.taskId = taskId;
        this.result = result;
        this.exception = exception;
    }

    /**
     * Returns the unique identifier of the task associated with this result.
     *
     * @return the task ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Creates a successful task result.
     *
     * @param taskId the unique identifier of the task
     * @param result the result of the task
     * @param <T> the type of the result
     * @return a new {@code NTaskResult} representing a successful task
     */
    public static <T> NTaskResult<T> ofSuccess(String taskId, T result) {
        return new NTaskResult<>(taskId, result, null);
    }

    /**
     * Creates a task result representing an error.
     *
     * @param taskId the unique identifier of the task
     * @param exception the exception thrown during task execution
     * @param <T> the type of the result
     * @return a new {@code NTaskResult} representing a failed task
     */
    public static <T> NTaskResult<T> ofError(String taskId, Throwable exception) {
        return new NTaskResult<>(taskId, null, exception);
    }

    /**
     * Checks whether the task completed successfully.
     *
     * @return {@code true} if the task succeeded; {@code false} otherwise
     */
    public boolean isSuccess() {
        return exception == null;
    }

    /**
     * Checks whether the task execution resulted in an error.
     *
     * @return {@code true} if the task failed; {@code false} otherwise
     */
    public boolean isError() {
        return exception != null;
    }

    /**
     * Returns the result of the task if it completed successfully.
     * <p>
     * If the task failed, this method throws an unchecked exception wrapping
     * the original exception.
     *
     * @return the result of the task
     * @throws RuntimeException if the task failed
     */
    public T getResult() {
        if (isError()) {
            throw NExceptions.ofUncheckedException(exception);
        }
        return result;
    }

    /**
     * Returns the exception that occurred during task execution.
     *
     * @return the exception if the task failed; {@code null} if the task succeeded
     */
    public Throwable getError() {
        return exception;
    }
}
