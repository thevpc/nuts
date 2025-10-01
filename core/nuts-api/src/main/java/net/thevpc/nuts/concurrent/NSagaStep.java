package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaStep {
    Object call(NSagaContext context);
    void undo(NSagaContext context);
}
