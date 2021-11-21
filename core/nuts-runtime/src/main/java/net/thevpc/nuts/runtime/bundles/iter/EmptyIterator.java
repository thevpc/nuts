package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class EmptyIterator<E> extends IterInfoNodeAware2Base<E> {

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
    public IterInfoNode info(NutsSession session) {
        return IterInfoNode.ofLiteral("Empty", null, null);
    }
}
