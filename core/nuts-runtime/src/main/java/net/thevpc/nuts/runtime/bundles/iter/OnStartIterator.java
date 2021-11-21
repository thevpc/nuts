package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

class OnStartIterator<T> extends IterInfoNodeAware2Base<T> {

    private final Iterator<T> from;
    private final Runnable r;
    private boolean started=false;

    public OnStartIterator(Iterator<T> from, Runnable r) {
        this.from = from;
        this.r = r;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("OnStart",
                IterInfoNode.resolveOrNull("base", from, session),
                IterInfoNode.resolveOrString("op", r, session)
        );
    }

    @Override
    public boolean hasNext() {
        if(!started){
            r.run();
            started=true;
        }
        return from.hasNext();
    }

    @Override
    public T next() {
        return from.next();
    }
}
