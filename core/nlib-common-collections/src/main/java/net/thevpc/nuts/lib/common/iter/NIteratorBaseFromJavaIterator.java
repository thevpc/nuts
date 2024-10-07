package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterator;

import java.util.Iterator;

public class NIteratorBaseFromJavaIterator<T> extends NIteratorBase<T> {
    private final Iterator<T> base;
    private NEDesc description;

    public NIteratorBaseFromJavaIterator(Iterator<T> base) {
        this.base = base;
    }

    @Override
    public NIterator<T> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }

    @Override
    public String toString() {
        return "NamedIterator";
    }

    @Override
    public NElement describe(NSession session) {
        NObjectElement b = NEDesc.describeResolveOr(base, session, () -> NElements.of(session).ofObject().build())
                .asObject().get(session);
        NElement a = description.apply(session);
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .addAll(a.asObject().get(session))
                    .build()
                    ;
        } else {
            return b.builder()
                    .set("name", a)
                    .build()
                    ;
        }
    }
}
