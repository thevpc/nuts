package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.8
 */
public interface NBulkheadCallStore {
    NBulkheadCallModel load(String id);

    void save(NBulkheadCallModel model);
}
