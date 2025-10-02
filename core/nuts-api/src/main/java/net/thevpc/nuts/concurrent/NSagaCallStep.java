package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCallStep {
    Object call(NSagaCallContext context);
    void undo(NSagaCallContext context);
}
