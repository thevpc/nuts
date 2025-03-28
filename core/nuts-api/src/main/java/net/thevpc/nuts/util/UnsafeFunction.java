package net.thevpc.nuts.util;

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

}
