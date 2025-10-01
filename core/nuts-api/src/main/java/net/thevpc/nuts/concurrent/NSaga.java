package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NCopiable;

/**
 * @since 0.8.7
 */
public interface NSaga<T> extends NCallable<T>, NCopiable {

    T callOrElse(NCallable<T> other);

    T getResult();

    <V> V getVar(String key);

    NSaga<T> setVar(String key, Object value);

    NSaga<T> copy();

    NSaga<T> reset();

    NSaga<T> newInstance();

    NSagaExecutionStatus status();

    boolean runStep(); // advances one step, returns false if no more steps
}
