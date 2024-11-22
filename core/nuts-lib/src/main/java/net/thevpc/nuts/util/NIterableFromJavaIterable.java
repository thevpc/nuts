package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.*;

public class NIterableFromJavaIterable<T> implements NIterable<T> {
    private final Iterable<T> base;
    private final NSession session;

    public NIterableFromJavaIterable(Iterable<T> base,NSession session) {
        this.base = base;
        this.session = session;
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
        return NEDesc.describeResolveOr(base, () -> NElements.of().ofObject().build())
                .asObject().get();
    }
}
