package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;

class NOnStartIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final NRunnable r;
    private boolean started=false;

    public NOnStartIterator(Iterator<T> base, NRunnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NElement describe() {
        return NDescribables.describeResolveOrDestructAsObject(base)
                .builder()
                .set("onStart", NDescribables.describeResolveOrToString(r))
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
