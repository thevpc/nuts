package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.Iterator;
import java.util.function.Supplier;

public class NIteratorAdapter<T> extends NIteratorBase<T> {
    private final Iterator<T> base;
    private final Supplier<NElement> info;

    public NIteratorAdapter(Iterator<T> base, Supplier<NElement> info) {
        this.base = base;
        this.info = info;
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
            a= NElements.of().ofObjectBuilder().set("name",a).build();
        }
        return NEDesc.describeResolveOrDestructAsObject(base)
                .builder()
                        .addAll(a.asObject().get()).build();
    }
}
