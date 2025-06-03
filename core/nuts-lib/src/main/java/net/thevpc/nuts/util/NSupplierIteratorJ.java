package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.Iterator;
import java.util.function.Supplier;

public class NSupplierIteratorJ<T> extends NIteratorBase<T> {

    private final Supplier<Iterator<T>> from;
    private NIterator<T> iterator;
    private NEDesc name;

    public NSupplierIteratorJ(Supplier<Iterator<T>> from, NEDesc name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public NElement describe() {
        return NElements.ofObjectBuilder()
                .name("Supplier")
                .set("template",
                        NEDesc.describeResolveOr(from, () -> {
                            NElement t = name.get();
                            return NElements.ofObjectBuilder().name("Compiled")
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
            iterator = it == null ? null : NIterator.of(it).withDesc(name);
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
