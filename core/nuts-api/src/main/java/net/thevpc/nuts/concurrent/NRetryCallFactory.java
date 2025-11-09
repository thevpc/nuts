package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NRetryCallFactory {
    NRetryCallStore getStore();

    NRetryCallFactory withStore(NRetryCallStore store);

    <T> NRetryCall<T> of(NCallable<T> callable);

    <T> NRetryCall<T> of(String id, NCallable<T> callable);

}
