package net.thevpc.nuts.concurrent;

/**
 * Store interface for persisting and retrieving {@link NCircuitBreakerCall} state.
 * <p>
 * An {@code NCircuitBreakerCallStore} provides a mechanism to load and save
 * the state of circuit-breaker calls. This allows circuit breakers to
 * maintain continuity across application restarts or distributed environments.
 * </p>
 *
 * <p>
 * Typical usage:
 * <pre>{@code
 * NCircuitBreakerCallStore store = ...;
 * NCircuitBreakerCallModel model = store.load("taskId");
 *
 * // Update state
 * model.setLastExecutionTimestamp(System.currentTimeMillis());
 * store.save(model);
 * }</pre>
 * </p>
 *
 * <p>
 * Implementations can persist state in memory, databases, files, or distributed
 * caches depending on the needs of the application.
 * </p>
 *
 * @since 0.8.7
 */
public interface NCircuitBreakerCallStore {
    /**
     * Loads the state of a circuit-breaker call by its identifier.
     *
     * @param id the unique identifier of the circuit-breaker call
     * @return the corresponding {@link NCircuitBreakerCallModel}, or {@code null} if not found
     */
    NCircuitBreakerCallModel load(String id);


    /**
     * Saves the state of a circuit-breaker call.
     *
     * @param value the model containing the state to persist
     */
    void save(NCircuitBreakerCallModel value);
}
