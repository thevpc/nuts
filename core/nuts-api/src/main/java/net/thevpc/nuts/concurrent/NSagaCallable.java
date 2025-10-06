package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

/**
 * @since 0.8.7
 */
public interface NSagaCallable<T> extends NCallable<T>, NCopiable {

    T callOrElse(NCallable<T> other);

    T getResult();

    <V> V getVar(String key);

    NSagaCallable<T> setVar(String key, Object value);

    NSagaCallable<T> copy();

    NSagaCallable<T> reset();

    NSagaCallable<T> newInstance();

    NSagaStatus status();

    boolean runStep(); // advances one step, returns false if no more steps
}
