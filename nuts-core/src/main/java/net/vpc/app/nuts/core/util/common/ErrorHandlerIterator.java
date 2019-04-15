/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.common;

import java.util.Iterator;

/**
 *
 * @author vpc
 */
public class ErrorHandlerIterator<T> implements Iterator<T> {

    private ErrorHandlerIteratorType type;
    private Iterator<T> other;
    private RuntimeException ex;

    public ErrorHandlerIterator(ErrorHandlerIteratorType type, Iterator<T> other) {
        this.other = other;
        this.type = type;
    }

    @Override
    public boolean hasNext() {
        try {
            boolean v = other.hasNext();
            ex = null;
            return v;
        } catch (RuntimeException ex) {
            switch (type) {
                case IGNORE: {
                    // do nothing
                    return false;
                }
                case POSPONE: {
                    // do nothing
                    this.ex = ex;
                    return true;
                }
                case THROW: {
                    throw ex;
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public T next() {
        if (ex != null) {
            throw ex;
        }
        return other.next();
    }

    @Override
    public void remove() {
        if (ex != null) {
            throw ex;
        }
        other.remove();
    }

}
