package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NRunnable;

import java.util.Iterator;

class OnStartIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final NRunnable r;
    private boolean started=false;

    public OnStartIterator(Iterator<T> base, NRunnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NElement describe() {
        return NEDesc.describeResolveOrDestructAsObject(base)
                .builder()
                .set("onStart", NEDesc.describeResolveOrToString(r))
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
