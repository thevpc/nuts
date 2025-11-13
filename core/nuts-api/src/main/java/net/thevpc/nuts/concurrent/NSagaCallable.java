package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

/**
 * Represents a saga-style callable that supports stepwise execution, variable storage,
 * result retrieval, and copy/reset functionality.
 * <p>
 * A saga is a long-running process or transaction that can be executed in multiple
 * steps, potentially with compensation or rollback capabilities. This interface extends
 * {@link NCallable} to provide a standard call mechanism and {@link NCopiable} to
 * allow creating copies of the saga instance.
 *
 * @param <T> the type of the result produced by this saga callable
 */
public interface NSagaCallable<T> extends NCallable<T>, NCopiable {

    /**
     * Executes this saga callable and returns its result.
     * <p>
     * If this saga fails or cannot produce a result, the provided {@code other} callable
     * is executed as a fallback.
     *
     * @param other a fallback callable to execute if this saga fails
     * @return the result of this saga or the fallback callable
     */
    T callOrElse(NCallable<T> other);

    /**
     * Returns the result produced by this saga callable.
     * <p>
     * May return {@code null} if the saga has not completed or produced a result yet.
     *
     * @return the result of this saga
     */
    T getResult();

    /**
     * Retrieves a variable stored in the saga context by its key.
     * <p>
     * Saga instances can maintain internal variables that persist across steps.
     *
     * @param key the variable name
     * @param <V> the expected type of the variable
     * @return the value of the variable, or {@code null} if not set
     */
    <V> V getVar(String key);

    /**
     * Sets a variable in the saga context.
     *
     * @param key the variable name
     * @param value the value to associate with the key
     * @return this saga instance for method chaining
     */
    NSagaCallable<T> setVar(String key, Object value);

    /**
     * Returns a copy of this saga callable.
     * <p>
     * The copy should be independent, allowing separate execution or state modifications.
     *
     * @return a new copy of this saga callable
     */
    NSagaCallable<T> copy();

    /**
     * Resets the saga to its initial state, discarding any progress or variables.
     *
     * @return this saga instance reset to its initial state
     */
    NSagaCallable<T> reset();

    /**
     * Creates a new instance of this saga callable.
     * <p>
     * Unlike {@link #copy()}, this may return a fresh instance with its own internal state,
     * depending on the implementation.
     *
     * @return a new instance of the saga callable
     */
    NSagaCallable<T> newInstance();

    /**
     * Returns the current status of the saga.
     *
     * @return the {@link NSagaStatus} representing the state of the saga
     */
    NSagaStatus status();

    /**
     * Executes the next step of the saga.
     * <p>
     * Each call should advance the saga by one step. Returns {@code false} if there are
     * no more steps to execute.
     *
     * @return {@code true} if a step was executed; {@code false} if the saga has completed
     */
    boolean runStep(); // advances one step, returns false if no more steps
}
