package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCallStore {
    NSagaCallModel load(String id);

    void save(NSagaCallModel value);
}
