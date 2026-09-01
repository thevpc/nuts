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

    /**
     * Creates a saga step with no undo action.
     *
     * @param call the execution function
     * @return the saga step
     */
    static NSagaStep of(java.util.function.Function<NSagaContext, Object> call) {
        return new NSagaStep() {
            @Override
            public Object call(NSagaContext context) {
                return call == null ? null : call.apply(context);
            }

            @Override
            public void undo(NSagaContext context) {
            }
        };
    }

    /**
     * Creates a saga step with execution and compensation actions.
     *
     * @param call the execution function
     * @param undo the compensation action
     * @return the saga step
     */
    static NSagaStep of(java.util.function.Function<NSagaContext, Object> call, java.util.function.Consumer<NSagaContext> undo) {
        return new NSagaStep() {
            @Override
            public Object call(NSagaContext context) {
                return call == null ? null : call.apply(context);
            }

            @Override
            public void undo(NSagaContext context) {
                if (undo != null) {
                    undo.accept(context);
                }
            }
        };
    }
}
