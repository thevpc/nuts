package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

class OnFinishIterator<T> extends NutsIteratorBase<T> {

    private final Iterator<T> base;
    private final Runnable r;

    public OnFinishIterator(Iterator<T> base, Runnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("onFinish", NutsDescribables.resolveOrToString(r, session))
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
