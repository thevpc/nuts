package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.Iterator;
import java.util.function.Supplier;

public class NIteratorBaseFromJavaIterator<T> extends NIteratorBase<T> {
    private final Iterator<T> base;
    private Supplier<NElement> description;

    public NIteratorBaseFromJavaIterator(Iterator<T> base) {
        this.base = base;
    }

    @Override
    public NIterator<T> redescribe(Supplier<NElement> description) {
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
    public NElement describe() {
        NObjectElement b = NDescribableElementSupplier.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get();
        NElement a = description.get();
        if (b.isEmpty()) {
            return a;
        }
        if (a.isObject()) {
            return b.builder()
                    .addAll(a.asObject().get())
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
