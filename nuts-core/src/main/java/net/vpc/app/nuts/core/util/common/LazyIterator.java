/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.common;

import java.util.Iterator;
import net.vpc.app.nuts.NutsUnsupportedOperationException;

/**
 *
 * @author vpc
 */
public class LazyIterator<T> implements Iterator<T> {

    private Iterable<T> iterable;
    private Iterator<T> iterator;

    public LazyIterator() {
    }

    public LazyIterator(Iterable<T> iterable) {
        this.iterable = iterable;
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

    protected Iterator<T> iterator() {
        throw new NutsUnsupportedOperationException(null, "No implemented");
    }

    @Override
    public void remove() {
        iterator.remove();
    }

}
