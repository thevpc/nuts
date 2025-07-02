package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;

class NIteratorOnFinish<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final NRunnable r;

    public NIteratorOnFinish(Iterator<T> base, NRunnable r) {
        this.base = base;
        this.r = r;
    }

    @Override
    public NElement describe() {
        return NDescribableElementSupplier.describeResolveOrDestructAsObject(base)
                .builder()
                .set("onFinish", NDescribableElementSupplier.describeResolveOrToString(r))
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
