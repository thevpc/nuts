/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class ErrorHandlerIterator<T> extends IterInfoNodeAware2Base<T> {

    private static Logger LOG = Logger.getLogger(ErrorHandlerIterator.class.getName());
    private IteratorErrorHandlerType type;
    private Iterator<T> base;
    private RuntimeException ex;

    public ErrorHandlerIterator(IteratorErrorHandlerType type, Iterator<T> base) {
        this.base = base;
        this.type = type;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        switch (type){
            case THROW:{
                return info("CatchAndThrow",IterInfoNode.resolveOrNull("base", base, session));
            }
            case IGNORE:{
                return info("CatchAndIgnore",IterInfoNode.resolveOrNull("base", base, session));
            }
            case POSTPONE:{
                return info("CatchAndPostpone",IterInfoNode.resolveOrNull("base", base, session));
            }
        }
        return info("CatchAndThrow",IterInfoNode.resolveOrNull("base", base, session));
    }

    @Override
    public boolean hasNext() {
        try {
            boolean v = base.hasNext();
            ex = null;
            return v;
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "error evaluating Iterator 'hasNext()' : " + CoreStringUtils.exceptionToString(ex), ex);
            switch (type) {
                case IGNORE: {
                    // do nothing
                    return false;
                }
                case POSTPONE: {
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
        return base.next();
    }

    @Override
    public void remove() {
        if (ex != null) {
            throw ex;
        }
        base.remove();
    }

    @Override
    public String toString() {
        switch (type){
            case THROW:return "ThrowOnError("+ base +")";
            case POSTPONE:return "PostponeError("+ base +")";
            case IGNORE:return "IgnoreError("+ base +")";
        }
        return "ErrorHandlerIterator(" +
                "type=" + type +
                ", base=" + base +
                ')';
    }
}
