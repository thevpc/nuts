package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.NSession;

import java.util.Iterator;

class OnFinishIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final Runnable r;

    public OnFinishIterator(Iterator<T> base, Runnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NElement describe(NSession session) {
        return NDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("onFinish", NDescribables.resolveOrToString(r, session))
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
