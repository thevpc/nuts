package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NCopiable;

/**
 * @since 0.8.7
 */
public interface NSagaCall<T> extends NCallable<T>, NCopiable {

    T callOrElse(NCallable<T> other);

    T getResult();

    <V> V getVar(String key);

    NSagaCall<T> setVar(String key, Object value);

    NSagaCall<T> copy();

    NSagaCall<T> reset();

    NSagaCall<T> newInstance();

    NSagaCallStatus status();

    boolean runStep(); // advances one step, returns false if no more steps
}
