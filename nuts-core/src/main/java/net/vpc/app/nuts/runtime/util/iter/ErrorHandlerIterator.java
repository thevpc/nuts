/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util.iter;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class ErrorHandlerIterator<T> implements Iterator<T> {

    private static Logger LOG = Logger.getLogger(ErrorHandlerIterator.class.getName());
    private IteratorErrorHandlerType type;
    private Iterator<T> other;
    private RuntimeException ex;

    public ErrorHandlerIterator(IteratorErrorHandlerType type, Iterator<T> other) {
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
            LOG.log(Level.SEVERE, "Error evaluating Iterator 'hasNext()' : " + ex.toString(), ex);
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

    @Override
    public String toString() {
        switch (type){
            case THROW:return "ThrowOnError("+other+")";
            case POSPONE:return "PostponeError("+other+")";
            case IGNORE:return "IgnoreError("+other+")";
        }
        return "ErrorHandlerIterator(" +
                "type=" + type +
                ", base=" + other +
                ')';
    }
}
