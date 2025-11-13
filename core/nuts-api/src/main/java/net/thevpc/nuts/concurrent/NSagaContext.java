package net.thevpc.nuts.concurrent;

/**
 * Represents the context of a saga execution.
 * <p>
 * The saga context acts as a key-value store for variables and state information
 * that are shared across the steps of a saga. It allows saga steps and conditions
 * to read and modify the execution state dynamically.
 * <p>
 * Typically used by {@link NSagaStep} implementations, {@link NSagaCondition}s,
 * and the saga runtime to track intermediate results, flags, or shared data.
 *
 * @since 0.8.7
 */
public interface NSagaContext {

    /**
     * Retrieves the value of a variable stored in the saga context.
     *
     * @param name the variable name
     * @param <T> the expected type of the variable
     * @return the value of the variable, or {@code null} if not present
     */
    <T> T getVar(String name);

    /**
     * Sets a variable in the saga context.
     * <p>
     * Variables stored in the context can be accessed by subsequent saga steps
     * or conditions. Setting a variable with the same name as an existing one
     * overwrites the previous value.
     *
     * @param name the variable name
     * @param value the value to store
     * @return this {@code NSagaContext} instance for method chaining
     */
    NSagaContext setVar(String name, Object value);
}
