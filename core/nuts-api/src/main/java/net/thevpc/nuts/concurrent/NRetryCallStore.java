package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NRetryCallStore {
    NRetryCallModel load(String id);

    void save(NRetryCallModel value);
}
