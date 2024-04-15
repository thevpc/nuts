package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.util.NFunction;

import java.util.function.Function;

public class NFunctionFromJavaFunction<T, V> implements NFunction<T, V> {
    private final Function<? super T, V> base;

    public NFunctionFromJavaFunction(Function<? super T, V> base) {
        this.base = base;
    }

    @Override
    public V apply(T f) {
        return base.apply(f);
    }

    @Override
    public String toString() {
        return "Function{" + base + '}';
    }

}
