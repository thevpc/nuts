package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaStore {
    NSagaModel load(String id);

    void save(NSagaModel value);
}
