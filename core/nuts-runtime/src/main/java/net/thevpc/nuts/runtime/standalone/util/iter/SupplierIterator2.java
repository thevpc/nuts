package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.nfo.NutsIteratorBase;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierIterator2<T> extends NutsIteratorBase<T> {

    private final Supplier<Iterator<T>> from;
    private NutsIterator<T> iterator;
    private Function<NutsElements, NutsElement> name;

    public SupplierIterator2(Supplier<Iterator<T>> from, Function<NutsElements, NutsElement> name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return elems.ofObject()
                .set("type", "Supplier")
                .set("template",
                        NutsDescribables.resolveOr(from, elems, () -> {
                            NutsElement t = name.apply(elems);
                            return elems.ofObject().set("type", "compiled")
                                    .addAll(t == null ? null : t.toObject())
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
