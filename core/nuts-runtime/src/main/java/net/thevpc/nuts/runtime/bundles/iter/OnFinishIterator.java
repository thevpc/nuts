package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

class OnFinishIterator<T> extends IterInfoNodeAware2Base<T> {

    private final Iterator<T> from;
    private final Runnable r;

    public OnFinishIterator(Iterator<T> from, Runnable r) {
        this.from = from;
        this.r = r;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("OnFinish",
                IterInfoNode.resolveOrNull("base", from, session),
                IterInfoNode.resolveOrString("op", r, session)
        );
    }

    @Override
    public boolean hasNext() {
        boolean n = from.hasNext();
        if (!n) {
            r.run();
        }
        return n;
    }

    @Override
    public T next() {
        return from.next();
    }
}
