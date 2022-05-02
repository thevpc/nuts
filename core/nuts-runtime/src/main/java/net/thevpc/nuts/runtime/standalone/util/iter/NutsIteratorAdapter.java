package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.NutsSession;

import java.util.Iterator;
import java.util.function.Function;

public class NutsIteratorAdapter<T> extends NutsIteratorBase<T> {
    private final Iterator<T> base;
    private final Function<NutsSession, NutsElement> info;

    public NutsIteratorAdapter(Iterator<T> base, Function<NutsSession, NutsElement> info) {
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
    public NutsElement describe(NutsSession session) {
        NutsElement a = info.apply(session);
        if(!a.isObject()){
            a= NutsElements.of(session).ofObject().set("name",a).build();
        }
        return NutsDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                        .addAll(a.asObject().get(session)).build();
    }
}
