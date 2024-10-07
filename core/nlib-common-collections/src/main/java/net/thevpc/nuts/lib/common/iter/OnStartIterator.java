package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NRunnable;

import java.util.Iterator;

class OnStartIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final NRunnable r;
    private final NSession session;
    private boolean started=false;

    public OnStartIterator(Iterator<T> base, NSession session, NRunnable r) {
        this.base = base;
        this.r = r;
        this.session = session;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.describeResolveOrDestructAsObject(base, session)
                .builder()
                .set("onStart", NEDesc.describeResolveOrToString(r, session))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        if(!started){
            r.run(session);
            started=true;
        }
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }
}
