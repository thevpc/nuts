package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.time.NDuration;

import java.util.function.IntFunction;

/**
 * Represents a callable task protected by a circuit breaker.
 * <p>
 * An {@code NCircuitBreakerCall} wraps a computation (an {@link NCallable})
 * and monitors its execution to prevent repeated failures from overwhelming
 * the system. It supports configurable thresholds for failures and successes,
 * and allows custom retry periods.
 * </p>
 *
 * <p>
 * This interface combines three concerns:
 * <ul>
 *     <li>Callable execution semantics via {@link NCallable}</li>
 *     <li>Structured metadata and self-description via {@link NElementDescribable}</li>
 *     <li>Resilience control via the circuit breaker pattern</li>
 * </ul>
 * </p>
 *
 * <p>
 * The circuit breaker can be in one of three states:
 * <ul>
 *     <li>{@link Status#CLOSED} – executions are allowed normally.</li>
 *     <li>{@link Status#OPEN} – executions are blocked due to repeated failures.</li>
 *     <li>{@link Status#HALF_OPEN} – executions are allowed selectively to test recovery.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * NCircuitBreakerCall<String> task = NCircuitBreakerCall.of(() -> {
 *     return fetchDataFromRemoteService();
 * })
 * .setFailureThreshold(3)
 * .setSuccessThreshold(2)
 * .setFailureRetryPeriod(attempt -> NDuration.ofSeconds(5))
 * .setSuccessRetryPeriod(attempt -> NDuration.ofSeconds(1));
 *
 * String result = task.callOrElse(() -> "default");
 * }</pre>
 * </p>
 *
 * @param <T> the type of result produced by the callable
 * @see NCallable
 * @see NElementDescribable
 * @see NConcurrent#circuitBreakerCall(NCallable)
 * @since 0.8.7
 */
public interface NCircuitBreakerCall<T> extends NCallable<T>, NElementDescribable {

    /**
     * The current state of the circuit breaker.
     */
    public static enum Status {
        /** Executions are allowed normally. */
        CLOSED,
        /** Executions are blocked due to repeated failures. */
        OPEN,
        /** Executions are selectively allowed to test recovery. */
        HALF_OPEN
    }

    /**
     * Creates a new circuit-breaker call wrapping the given callable.
     *
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@code NCircuitBreakerCall} instance
     */
    static <T> NCircuitBreakerCall<T> of(NCallable<T> callable) {
        return NConcurrent.of().circuitBreakerCall(callable);
    }

    /**
     * Creates a new circuit-breaker call with a specific identifier.
     *
     * @param id an identifier for this circuit breaker
     * @param callable the underlying task
     * @param <T> the result type
     * @return a new {@code NCircuitBreakerCall} instance
     */
    static <T> NCircuitBreakerCall<T> of(String id, NCallable<T> callable) {
        return NConcurrent.of().circuitBreakerCall(id, callable);
    }

    /**
     * Sets the maximum number of consecutive failures before the circuit breaker opens.
     *
     * @param failureThreshold number of failures to trigger open state
     * @return this instance for fluent chaining
     */
    NCircuitBreakerCall<T> setFailureThreshold(int failureThreshold);


    /**
     * Sets the number of consecutive successful executions required to close a half-open circuit.
     *
     * @param successThreshold number of successes to transition to closed state
     * @return this instance for fluent chaining
     */
    NCircuitBreakerCall<T> setSuccessThreshold(int successThreshold);

    /**
     * Sets the retry period to apply after a failure.
     *
     * @param retryPeriod function mapping the failure attempt number to a {@link NDuration}
     * @return this instance for fluent chaining
     */
    NCircuitBreakerCall<T> setSuccessRetryPeriod(IntFunction<NDuration> retryPeriod);

    /**
     * Sets the retry period to apply after a successful execution in half-open state.
     *
     * @param retryPeriod function mapping the success attempt number to a {@link NDuration}
     * @return this instance for fluent chaining
     */
    NCircuitBreakerCall<T> setFailureRetryPeriod(IntFunction<NDuration> retryPeriod);

    /**
     * Executes the callable, returning the result or delegating to a recovery callable if an error occurs.
     * <p>
     * This method may block until a successful execution is achieved or the recovery callable completes.
     * </p>
     *
     * @param recover a callable to produce a fallback value in case of failure
     * @return the result of the main callable or the recovery callable
     */
    T callOrElse(NCallable<T> recover);

    /**
     * Executes the callable, returning the result or the last known successful value if an error occurs.
     *
     * @return the result of the main callable or the last successful value
     */
    T callOrLast();
}
