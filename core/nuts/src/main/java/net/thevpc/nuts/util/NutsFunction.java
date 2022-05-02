package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;

import java.util.function.Function;

public interface NutsFunction<T, V> extends Function<T, V>, NutsDescribable {
    static <T, V> NutsFunction<T, V> of(Function<T, V> o, String descr) {
        return NutsDescribables.ofFunction(o, session -> NutsElements.of(session).ofString(descr));
    }

    static <T, V> NutsFunction<T, V> of(Function<T, V> o, NutsElement descr) {
        return NutsDescribables.ofFunction(o, e -> descr);
    }

    static <T, V> NutsFunction<T, V> of(Function<T, V> o, Function<NutsSession, NutsElement> descr) {
        return NutsDescribables.ofFunction(o, descr);
    }

}
