package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

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
        return NEDesc.describeResolveOr(base, () -> NElements.ofObjectBuilder().build())
                .asObject().get();
    }
}
