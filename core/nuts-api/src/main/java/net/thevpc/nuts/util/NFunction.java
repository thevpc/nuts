package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.function.Function;

public interface NFunction<T, V> extends Function<T, V>, NElementDescribable<NFunction<T, V>> {
    static <T, V> NFunction<T, V> of(Function<T, V> o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NFunction) {
            return (NFunction<T, V>) o;
        }
        return new NFunctionFromJavaFunction<>(o);
    }

    static <T, V> NFunction<T, V> ofUnsafe(UnsafeFunction<T, V> o, Function<Exception, ? extends V> onError) {
        NAssert.requireNonNull(o, "function");
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
        });
    }

    default NElement describe() {
        return NElements.of().toElement("function");
    }

    @Override
    default NFunction<T, V> withDesc(NEDesc description) {
        if(description==null){
            return this;
        }
        return new NFunctionWithDescription<>(this,description);
    }
}
