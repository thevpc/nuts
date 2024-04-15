package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;

import java.util.List;
import java.util.function.Consumer;

public abstract class NIteratorDelegate<T> extends NIteratorBase<T> {
    public abstract NIterator<T> baseIterator();

    @Override
    public boolean hasNext() {
        return baseIterator().hasNext();
    }

    @Override
    public T next() {
        return baseIterator().next();
    }

    @Override
    public NElement describe(NSession session) {
        return baseIterator().describe(session);
    }

    @Override
    public void remove() {
        baseIterator().remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        baseIterator().forEachRemaining(action);
    }

    @Override
    public List<T> toList() {
        return baseIterator().toList();
    }
}
