package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.NSession;

import java.util.Iterator;

class OnStartIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final Runnable r;
    private boolean started=false;

    public OnStartIterator(Iterator<T> base, Runnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NElement describe(NSession session) {
        return NDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("onStart", NDescribables.resolveOrToString(r, session))
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
