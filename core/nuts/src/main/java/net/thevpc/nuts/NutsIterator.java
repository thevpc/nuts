package net.thevpc.nuts;

import java.util.Iterator;
import java.util.function.Function;

public interface NutsIterator<T> extends Iterator<T>, NutsDescribable {
    static <T> NutsIterator<T> of(Iterator<T> o, String descr) {
        return NutsDescribables.ofIterator(o, e -> e.ofString(descr));
    }

    static <T> NutsIterator<T> of(Iterator<T> o, NutsElement descr) {
        return NutsDescribables.ofIterator(o, e -> descr);
    }

    static <T> NutsIterator<T> of(Iterator<T> o, Function<NutsElements, NutsElement> descr) {
        return NutsDescribables.ofIterator(o, descr);
    }

    static <T> NutsIterator<T> ofEmpty(NutsSession session) {
        return (NutsIterator) NutsStream.ofEmpty(session).iterator();
    }
}
