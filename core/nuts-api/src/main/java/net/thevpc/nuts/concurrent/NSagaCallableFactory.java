package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCallableFactory {
    NSagaStore getStore();

    NSagaCallableFactory withStore(NSagaStore store);

    NSagaCallableBuilder ofBuilder();

}
