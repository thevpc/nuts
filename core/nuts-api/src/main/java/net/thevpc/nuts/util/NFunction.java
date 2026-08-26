package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * NFunction interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NFunction<T, V> extends Function<T, V>, NRedescribable<NFunction<T, V>> {
    /**
     * Creates a new instance of of.
     *
     * @param o o
     * @return of result
     */
    static <T, V> NFunction<T, V> of(Function<T, V> o) {
        /**
         * Creates a new instance of of.
         *
         * @param o o
         * @param null null
         * @return of result
         */
        return of(o, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param o o
     * @param defaultDescription default description
     * @return of result
     */
    static <T, V> NFunction<T, V> of(Function<T, V> o, NElement defaultDescription) {
        if (o == null) {
            return null;
        }
        if (o instanceof NFunction) {
            return (NFunction<T, V>) o;
        }
        return new NFunctionFromJavaFunction<>(o, defaultDescription);
    }

    /**
     * Creates a new instance of of unsafe.
     *
     * @param o o
     * @param onError on error
     * @return of unsafe result
     */
    static <T, V> NFunction<T, V> ofUnsafe(UnsafeFunction<T, V> o, Function<Exception, ? extends V> onError) {
        NAssert.requireNamedNonNull(o, "function");
        if (o instanceof NFunction) {
            return (NFunction<T, V>) o;
        }
        NUnsafeFunction<T, V> uf = NUnsafeFunction.of(o);
        return new NFunctionFromJavaFunction<>(new Function<T, V>() {
            @Override
            public V apply(T t) {
                try {
                    return uf.apply(t);
                } catch (Exception e) {
                    if (onError == null) {
                        return null;
                    }
                    return onError.apply(e);
                }
            }
        }, NElement.ofNamedTuple("unsafe", NDescribables.describeResolveOrSimplifyAsObject(o)));
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("function");
    }

    @Override
    default NFunction<T, V> withDescription(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NFunctionWithDescription<>(this, description);
    }
}
