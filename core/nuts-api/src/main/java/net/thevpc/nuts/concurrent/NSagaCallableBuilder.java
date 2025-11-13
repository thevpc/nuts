package net.thevpc.nuts.concurrent;

/**
 * Builder interface for constructing {@link NSagaCallable} instances.
 * <p>
 * This interface provides a fluent API to define a saga as a sequence of steps,
 * including conditional branching ({@link If}) and loops ({@link While}).
 * It supports named steps, nested conditions, and nested loops, allowing complex
 * saga workflows to be built in a readable, declarative style.
 */
public interface NSagaCallableBuilder {
    /**
     * Starts the saga definition and returns the root suite for adding steps.
     *
     * @return the root suite to define steps
     */
    Suite<NSagaCallableBuilder> start();

    /**
     * Builds and returns a fully constructed {@link NSagaCallable} instance
     * representing the saga workflow defined via the builder.
     *
     * @param <T> the result type of the saga callable
     * @return the constructed saga callable
     */
    <T> NSagaCallable<T> build();

    /**
     * Represents a suite of steps within a saga, supporting sequential execution.
     *
     * @param <P> the parent builder type returned by {@link #end()}
     */
    interface Suite<P>  {
        /**
         * Adds a named step to the saga sequence.
         *
         * @param name the step name
         * @param step the saga step to execute
         * @return this suite for method chaining
         */
        Suite<P> then(String name, NSagaStep step);

        /**
         * Adds a conditional branch to the saga sequence.
         * <p>
         * Returns an {@link If} interface to define the steps executed if the condition is true.
         *
         * @param name the step name
         * @param condition the condition to evaluate
         * @return an {@link If} interface to define conditional steps
         */
        If<Suite<P>> thenIf(String name, NSagaCondition condition);

        /**
         * Adds a while-loop branch to the saga sequence.
         * <p>
         * Returns a {@link While} interface to define the steps executed while the condition is true.
         *
         * @param name the loop name
         * @param condition the loop condition
         * @return a {@link While} interface to define loop steps
         */
        While<Suite<P>> thenWhile(String name, NSagaCondition condition);

        /**
         * Ends the current suite and returns to the parent builder level.
         *
         * @return the parent builder
         */
        P end();
    }

    /**
     * Represents a while-loop construct within a saga workflow.
     *
     * @param <P> the parent builder type returned by {@link #end()}
     */
    interface While<P>  {

        /**
         * Adds a named step inside the while loop.
         *
         * @param name the step name
         * @param step the saga step to execute
         * @return this while-loop interface for chaining
         */
        While<P> then(String name, NSagaStep step);

        /**
         * Adds a conditional branch inside the while loop.
         *
         * @param name the step name
         * @param condition the condition to evaluate
         * @return an {@link If} interface to define conditional steps
         */
        If<While<P>> thenIf(String name, NSagaCondition condition);

        /**
         * Adds a nested while loop inside this while loop.
         *
         * @param name the loop name
         * @param condition the loop condition
         * @return a {@link While} interface for defining nested loop steps
         */
        While<While<P>> thenWhile(String name, NSagaCondition condition);

        /**
         * Ends the current while loop and returns to the parent suite or builder.
         *
         * @return the parent builder or suite
         */
        P end();
    }

    /**
     * Represents a conditional (if/else) construct within a saga workflow.
     *
     * @param <P> the parent builder type returned by {@link #end()}
     */
    interface If<P>  {

        /**
         * Adds a named step to execute if the condition evaluates to true.
         *
         * @param name the step name
         * @param step the saga step to execute
         * @return this conditional interface for chaining
         */
        If<P> then(String name, NSagaStep step);

        /**
         * Adds a nested conditional branch inside the current conditional block.
         *
         * @param name the step name
         * @param condition the condition to evaluate
         * @return a nested {@link If} interface for defining conditional steps
         */
        If<If<P>> thenIf(String name, NSagaCondition condition);

        /**
         * Adds a while-loop branch inside the current conditional block.
         *
         * @param name the loop name
         * @param condition the loop condition
         * @return a {@link While} interface for defining loop steps
         */
        While<If<P>> thenWhile(String name, NSagaCondition condition);

        /**
         * Adds an else-if conditional branch following the current condition.
         *
         * @param name the step name
         * @param condition the condition to evaluate
         * @return this {@link If} interface for chaining
         */
        If<P> elseIf(String name, NSagaCondition condition);

        /**
         * Adds an otherwise branch, executed if none of the preceding conditions are true.
         *
         * @return this {@link If} interface for chaining
         */
        If<P> otherwise();

        /**
         * Ends the current conditional block and returns to the parent suite or builder.
         *
         * @return the parent builder or suite
         */
        P end();
    }
}
