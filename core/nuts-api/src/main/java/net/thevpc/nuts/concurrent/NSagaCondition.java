package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCondition {
    boolean test(NSagaContext context);
}
