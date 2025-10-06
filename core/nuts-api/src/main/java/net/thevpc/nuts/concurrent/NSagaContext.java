package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaContext {
    <T> T getVar(String name);
    NSagaContext setVar(String name, Object value);
}
