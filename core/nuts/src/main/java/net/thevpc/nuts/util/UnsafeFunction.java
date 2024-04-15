package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.Objects;
import java.util.function.Function;

public interface UnsafeFunction<T, V> {

    V apply(T t) throws Exception;

    default <V2> UnsafeFunction<V2, V> compose(Function<? super V2, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V2 v) -> apply(before.apply(v));
    }

    default <V2> UnsafeFunction<T, V2> andThen(Function<? super V, ? extends V2> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> UnsafeFunction<T, T> identity() {
        return t -> t;
    }

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("unsafe function");
    }
}
