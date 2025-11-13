package net.thevpc.nuts.concurrent;

/**
 * Represents a single step in a saga workflow.
 * <p>
 * A saga step encapsulates the execution logic for a particular operation
 * and defines how to compensate (undo) that operation if the saga fails
 * or a rollback is required.
 * <p>
 * Implementations should be stateless if possible, as the context carries
 * execution state and variables.
 *
 * @since 0.8.7
 */
public interface NSagaStep {

    /**
     * Executes the logic of this saga step.
     *
     * @param context the {@link NSagaContext} holding saga variables and state
     * @return the result of the step execution, which may be stored in the saga context
     */
    Object call(NSagaContext context);
    /**
     * Compensates or undoes the effects of this saga step.
     * <p>
     * This method is called if a previous step fails and the saga requires
     * rollback according to the compensation strategy.
     *
     * @param context the {@link NSagaContext} holding saga variables and state
     */
    void undo(NSagaContext context);
}
