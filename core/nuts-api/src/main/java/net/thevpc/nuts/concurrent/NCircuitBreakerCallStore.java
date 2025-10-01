package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NCircuitBreakerCallStore {
    NCircuitBreakerCallModel load(String id);

    void save(NCircuitBreakerCallModel value);
}
