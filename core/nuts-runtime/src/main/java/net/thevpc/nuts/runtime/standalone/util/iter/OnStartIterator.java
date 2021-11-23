package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.runtime.standalone.util.nfo.NutsIteratorBase;
import net.thevpc.nuts.NutsDescribables;

import java.util.Iterator;

class OnStartIterator<T> extends NutsIteratorBase<T> {

    private final Iterator<T> base;
    private final Runnable r;
    private boolean started=false;

    public OnStartIterator(Iterator<T> base, Runnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return NutsDescribables.resolveOrDestruct(base,elems)
                .toObject().builder()
                .set("onStart", NutsDescribables.resolveOrToString(r,elems))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        if(!started){
            r.run();
            started=true;
        }
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }
}
