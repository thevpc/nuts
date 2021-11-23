package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.runtime.standalone.util.nfo.NutsIteratorBase;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class EmptyIterator<E> extends NutsIteratorBase<E> {

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
    public NutsElement describe(NutsElements elems) {
        return elems.ofString("empty");
    }
}
