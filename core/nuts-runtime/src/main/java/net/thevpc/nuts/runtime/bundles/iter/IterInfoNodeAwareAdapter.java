package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

class IterInfoNodeAwareAdapter<T> extends IterInfoNodeAware2Base<T> {
    private final Iterator<T> base;
    public IterInfoNodeAwareAdapter(Iterator<T> base, IterInfoNode info) {
        this.base = base;
        super.attachInfo(info);
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return super.info(attachedInfo.getType(),IterInfoNode.resolveOrString("base", base, session));
    }
}
