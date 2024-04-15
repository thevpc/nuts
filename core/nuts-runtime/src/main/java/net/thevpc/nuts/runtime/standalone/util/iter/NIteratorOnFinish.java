package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NRunnable;

import java.util.Iterator;

class NIteratorOnFinish<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final NRunnable r;
    private final NSession session;

    public NIteratorOnFinish(Iterator<T> base, NSession session,NRunnable r) {
        this.base = base;
        this.r = r;
        this.session = session;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.describeResolveOrDestructAsObject(base, session)
                .builder()
                .set("onFinish", NEDesc.describeResolveOrToString(r, session))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        boolean n = base.hasNext();
        if (!n) {
            r.run(session);
        }
        return n;
    }

    @Override
    public T next() {
        return base.next();
    }
}
