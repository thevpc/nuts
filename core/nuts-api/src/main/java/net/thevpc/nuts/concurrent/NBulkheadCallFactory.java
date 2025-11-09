package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.8
 */
public interface NBulkheadCallFactory {
    static NBulkheadCallFactory of() {
        return NConcurrent.of().bulkheadCallFactory();
    }

    static NBulkheadCallFactory of(NBulkheadCallStore store) {
        return NConcurrent.of().bulkheadCallFactory().withStore(store);
    }

    <T> NBulkheadCall<T> bulkheadCall(NCallable<T> callable);

    <T> NBulkheadCall<T> bulkheadCall(String id, NCallable<T> callable);

    NBulkheadCallFactory withStore(NBulkheadCallStore store);
    NBulkheadCallFactory withBackend(NBulkheadCallBackend backend);

    NBulkheadCallStore getStore();
    NBulkheadCallBackend getBackend();
}
