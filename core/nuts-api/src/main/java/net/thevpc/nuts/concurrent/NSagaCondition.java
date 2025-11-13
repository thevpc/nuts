package net.thevpc.nuts.concurrent;

/**
 * Represents a condition used in a saga workflow to determine whether
 * a particular step, branch, or loop should be executed.
 * <p>
 * Conditions are evaluated dynamically at runtime against the current
 * {@link NSagaContext}, allowing decisions to be based on saga variables,
 * status, or external inputs.
 * <p>
 * Typically used in combination with {@link NSagaCallableBuilder} methods
 * such as {@code thenIf}, {@code elseIf}, and {@code thenWhile}.
 *
 * @since 0.8.7
 */
public interface NSagaCondition {
    /**
     * Evaluates this condition against the provided saga context.
     *
     * @param context the current {@link NSagaContext} containing saga state and variables
     * @return {@code true} if the condition is met; {@code false} otherwise
     */
    boolean test(NSagaContext context);
}
