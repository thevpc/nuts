/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

/**
 * @author thevpc
 */
public class LazyIterator<T> extends IterInfoNodeAware2Base<T> {

    private Iterable<T> iterable;
    private Iterator<T> iterator;

    public LazyIterator() {
    }

    public LazyIterator(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("Lazy", IterInfoNode.resolveOrString("iterable", iterable, session));
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            if (iterable == null) {
                iterator = this.iterator();
            } else {
                iterator = iterable.iterator();
            }
            if (iterator == null) {
                return false;
            }
        }
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    protected Iterator<T> iterator() {
        throw new UnsupportedOperationException("no implemented");
    }

    @Override
    public String toString() {
        return "LazyIterator("
                + (iterator != null ? iterator.toString() : iterable != null ? iterable.toString() : "null")
                + ")";
    }
}
