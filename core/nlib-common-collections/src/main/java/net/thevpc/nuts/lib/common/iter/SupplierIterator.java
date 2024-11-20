package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NIterator;

import java.util.function.Supplier;

public class SupplierIterator<T> extends NIteratorBase<T> {

    private final Supplier<NIterator<T>> from;
    private NIterator<T> iterator;
    private NObjectElement name;

    public SupplierIterator(Supplier<NIterator<T>> from, NObjectElement name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NElement describe() {
        return NElements.of().ofObject()
                .set("type","Supplier")
                .set("template",
                        NEDesc.describeResolveOr(from, ()-> NElements.of().ofObject().set("type","compiled").addAll(name).build())
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
