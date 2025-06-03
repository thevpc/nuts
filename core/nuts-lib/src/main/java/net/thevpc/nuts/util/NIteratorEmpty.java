package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

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
    public NElement describe() {
        return NElements.ofString("empty");
    }
}
