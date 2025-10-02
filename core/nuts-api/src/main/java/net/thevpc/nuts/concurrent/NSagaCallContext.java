package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCallContext {
    <T> T getVar(String name);
    NSagaCallContext setVar(String name, Object value);
}
