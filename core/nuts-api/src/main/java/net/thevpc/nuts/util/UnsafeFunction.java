package net.thevpc.nuts.util;

import java.util.function.Function;

/**
 * UnsafeFunction interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NJdkExtension("Missing from java.util.function — JDK only provides Function that are safe")
@FunctionalInterface
public interface UnsafeFunction<T, V> {

    /**
     * Apply.
     *
     * @param t t
     * @return apply result
     * @throws Exception if execution fails
     */
    V apply(T t) throws Exception;

    /**
     * Compose.
     *
     * @param before before
     * @return compose result
     */
    default <V2> UnsafeFunction<V2, V> compose(Function<? super V2, ? extends T> before) {
        NAssert.requireNamedNonNull(before);
      /**
       * Return.
       *
       * @param apply(before.apply(v) apply(before.apply(v)
       */
        return (V2 v) -> apply(before.apply(v));
    }

    /**
     * And then.
     *
     * @param after after
     * @return and then result
     */
    default <V2> UnsafeFunction<T, V2> andThen(Function<? super V, ? extends V2> after) {
        NAssert.requireNamedNonNull(after);
      /**
       * Return.
       *
       * @param after.apply(apply(t) after.apply(apply(t)
       */
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Identity.
     *
     * @return identity result
     */
    static <T> UnsafeFunction<T, T> identity() {
        return t -> t;
    }

}
