package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.util.NIterator;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierIterator2<T> extends NIteratorBase<T> {

    private final Supplier<Iterator<T>> from;
    private NIterator<T> iterator;
    private Function<NSession, NElement> name;

    public SupplierIterator2(Supplier<Iterator<T>> from, Function<NSession, NElement> name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofObject()
                .set("type", "Supplier")
                .set("template",
                        NDescribables.resolveOr(from, session, () -> {
                            NElement t = name.apply(session);
                            return NElements.of(session).ofObject().set("type", "compiled")
                                    .addAll(t == null ? null : t.asObject().orNull())
                                    .build();
                        })
                )
                .build();
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            Iterator<T> it = from.get();
            iterator = it == null ? null : NIterator.of(it, name);
            if (iterator == null) {
                return false;
            }
        }
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        if (iterator != null) {
            iterator.remove();
        }
    }

    @Override
    public String toString() {
        if (name == null) {
            return "supplier(" + from + ")";
        }
        return String.valueOf(name);
    }
}
