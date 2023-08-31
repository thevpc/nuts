/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;

import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author thevpc
 */
public class ErrorHandlerIterator<T> extends NIteratorBase<T> {

    private IteratorErrorHandlerType type;
    private Iterator<T> base;
    private RuntimeException ex;
    private NSession session;

    public ErrorHandlerIterator(IteratorErrorHandlerType type, Iterator<T> base, NSession session) {
        this.base = base;
        this.type = type;
        this.session = session;
    }

    @Override
    public NElement describe(NSession session) {
        return NDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("onError",type.toString().toLowerCase())
                .build();
    }


    @Override
    public boolean hasNext() {
        try {
            boolean v = base.hasNext();
            ex = null;
            return v;
        } catch (RuntimeException ex) {
            NLogOp.of(IndexFirstIterator.class,session)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC("error evaluating Iterator 'hasNext()' : %s", ex));
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
