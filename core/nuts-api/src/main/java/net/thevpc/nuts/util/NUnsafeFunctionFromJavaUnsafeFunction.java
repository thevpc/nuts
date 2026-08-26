package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

/**
 * NUnsafeFunctionFromJavaUnsafeFunction class.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NImmutable
public class NUnsafeFunctionFromJavaUnsafeFunction<T, V> implements NUnsafeFunction<T, V>{
    private final UnsafeFunction<T, V> base;
    private final Supplier<NElement> description;

    /**
     * N unsafe function from java unsafe function.
     *
     * @param base base
     * @param description description
     * @return n unsafe function from java unsafe function result
     */
    public NUnsafeFunctionFromJavaUnsafeFunction(UnsafeFunction<T, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunction<T, V> withDescription(Supplier<NElement> description) {
        return new NUnsafeFunctionFromJavaUnsafeFunction<>(base, description);
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribe(description, NDescribables.ofLateToString(this));
    }
}
