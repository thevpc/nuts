package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NSession;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class NIteratorEmpty<E> extends NIteratorBase<E> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new IllegalStateException();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public String toString() {
        return "EmptyIterator";
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofString("empty");
    }
}
