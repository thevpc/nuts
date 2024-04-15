package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;

public class NIterableFromJavaIterable<T> implements NIterable<T> {
    private final Iterable<T> base;
    private final NSession session;

    public NIterableFromJavaIterable(Iterable<T> base,NSession session) {
        this.base = base;
        this.session = session;
    }

    @Override
    public NIterator<T> iterator() {
        return NIterator.of(base.iterator(),session);
    }

    @Override
    public String toString() {
        return "NamedIterable";
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.describeResolveOr(base, session, () -> NElements.of(session).ofObject().build())
                .asObject().get(session);
    }
}
