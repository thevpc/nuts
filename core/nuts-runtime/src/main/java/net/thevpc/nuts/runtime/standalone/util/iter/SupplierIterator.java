package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;

import java.util.function.Supplier;

public class SupplierIterator<T> extends NutsIteratorBase<T> {

    private final Supplier<NutsIterator<T>> from;
    private NutsIterator<T> iterator;
    private NutsObjectElement name;

    public SupplierIterator(Supplier<NutsIterator<T>> from, NutsObjectElement name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofObject()
                .set("type","Supplier")
                .set("template",
                        NutsDescribables.resolveOr(from, session,()-> NutsElements.of(session).ofObject().set("type","compiled").addAll(name).build())
                )
                .build();
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            iterator = from.get();
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
