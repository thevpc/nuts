package net.thevpc.nuts.runtime.standalone.util.nfo;

import net.thevpc.nuts.NutsDescribables;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;

import java.util.Iterator;
import java.util.function.Function;

public class NutsIteratorAdapter<T> extends NutsIteratorBase<T> {
    private final Iterator<T> base;
    private final Function<NutsElements, NutsElement> info;

    public NutsIteratorAdapter(Iterator<T> base, Function<NutsElements ,NutsElement> info) {
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
    public NutsElement describe(NutsElements elems) {
        NutsElement a = info.apply(elems);
        if(!a.isObject()){
            a=elems.ofObject().set("name",a).build();
        }
        return NutsDescribables.resolveOrDestruct(base,elems)
                        .toObject().builder()
                        .addAll(a.asObject()).build();
    }
}
