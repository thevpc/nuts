package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.6
 */
public interface NCachedValueStore {
    NCachedValueModel load(String id);

    void save(NCachedValueModel model);
}
