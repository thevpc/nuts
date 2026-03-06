package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;

public class NIterableFromJavaIterable<T> implements NIterable<T> {
    private final Iterable<T> base;

    public NIterableFromJavaIterable(Iterable<T> base) {
        this.base = base;
    }

    @Override
    public NIterator<T> iterator() {
        return NIterator.of(base.iterator());
    }

    @Override
    public String toString() {
        return "NamedIterable";
    }

    @Override
    public NElement describe() {
        return NDescribables.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get();
    }
}
