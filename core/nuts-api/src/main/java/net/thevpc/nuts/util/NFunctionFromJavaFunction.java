package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

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

    @Override
    public NElement describe() {
        return NElements.of().toElement(NMsg.ofC("function %s", base).toString());
    }
}
