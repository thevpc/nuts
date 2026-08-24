package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NMsg;

import java.util.function.Function;

/**
 * NFunctionFromJavaFunction class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NFunctionFromJavaFunction<T, V> implements NFunction<T, V> {
    private final Function<? super T, V> base;
    private final NElement description;

    /**
     * N function from java function.
     *
     * @param base base
     * @param description description
     * @return n function from java function result
     */
    public NFunctionFromJavaFunction(Function<? super T, V> base, NElement description) {
        this.base = base;
        this.description = description;
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
        return description == null ? NElement.of(NMsg.ofC("function %s", base).toString())
                : description;
    }
}
