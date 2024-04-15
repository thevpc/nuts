package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NSession;

import java.util.Iterator;
import java.util.function.Function;

public class NIteratorAdapter<T> extends NIteratorBase<T> {
    private final Iterator<T> base;
    private final Function<NSession, NElement> info;

    public NIteratorAdapter(Iterator<T> base, Function<NSession, NElement> info) {
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
    public NElement describe(NSession session) {
        NElement a = info.apply(session);
        if(!a.isObject()){
            a= NElements.of(session).ofObject().set("name",a).build();
        }
        return NEDesc.describeResolveOrDestructAsObject(base, session)
                .builder()
                        .addAll(a.asObject().get(session)).build();
    }
}
