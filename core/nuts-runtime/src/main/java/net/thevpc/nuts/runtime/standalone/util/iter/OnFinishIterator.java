package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsDescribables;

import java.util.Iterator;

class OnFinishIterator<T> extends NutsIteratorBase<T> {

    private final Iterator<T> base;
    private final Runnable r;

    public OnFinishIterator(Iterator<T> base, Runnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return NutsDescribables.resolveOrDestruct(base,elems)
                .asSafeObject(true).builder()
                .set("onFinish", NutsDescribables.resolveOrToString(r,elems))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        boolean n = base.hasNext();
        if (!n) {
            r.run();
        }
        return n;
    }

    @Override
    public T next() {
        return base.next();
    }
}
