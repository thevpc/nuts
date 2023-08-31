package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.function.Function;

public interface NFunction<T, V> extends Function<T, V>, NDescribable {
    static <T, V> NFunction<T, V> of(Function<T, V> o, String descr) {
        return NDescribables.ofFunction(o, session -> NElements.of(session).ofString(descr));
    }

    static <T, V> NFunction<T, V> of(Function<T, V> o, NElement descr) {
        return NDescribables.ofFunction(o, e -> descr);
    }

    static <T, V> NFunction<T, V> of(Function<T, V> o, Function<NSession, NElement> descr) {
        return NDescribables.ofFunction(o, descr);
    }

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("custom function");
    }

}
