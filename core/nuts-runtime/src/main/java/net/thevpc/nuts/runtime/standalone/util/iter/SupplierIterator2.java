package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.util.NutsIterator;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierIterator2<T> extends NutsIteratorBase<T> {

    private final Supplier<Iterator<T>> from;
    private NutsIterator<T> iterator;
    private Function<NutsSession, NutsElement> name;

    public SupplierIterator2(Supplier<Iterator<T>> from, Function<NutsSession, NutsElement> name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofObject()
                .set("type", "Supplier")
                .set("template",
                        NutsDescribables.resolveOr(from, session, () -> {
                            NutsElement t = name.apply(session);
                            return NutsElements.of(session).ofObject().set("type", "compiled")
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
            iterator = it == null ? null : NutsIterator.of(it, name);
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
