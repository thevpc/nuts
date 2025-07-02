package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;
import java.util.function.Supplier;

public class NIteratorAdapter<T> extends NIteratorBase<T> {
    private final Iterator<T> base;
    private final Supplier<NElement> info;

    public NIteratorAdapter(Iterator<T> base, Supplier<NElement> info) {
        this.base = base;
        this.info = info;
    }

    public Iterator<T> getBase() {
        return base;
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
    public NElement describe() {
        NElement a = info.get();
        if(!a.isObject()){
            a= NElement.ofObjectBuilder().set("name",a).build();
        }
        return NDescribableElementSupplier.describeResolveOrDestructAsObject(base)
                .builder()
                        .addAll(a.asObject().get().children().toArray(new NElement[0]))
                .build();
    }
}
