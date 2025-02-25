/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author thevpc
 */
public class NErrorHandlerIterator<T> extends NIteratorBase<T> {

    private NIteratorErrorHandlerType type;
    private Iterator<T> base;
    private RuntimeException ex;

    public NErrorHandlerIterator(NIteratorErrorHandlerType type, Iterator<T> base) {
        this.base = base;
        this.type = type;
    }

    @Override
    public NElement describe() {
        return NEDesc.describeResolveOrDestructAsObject(base)
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
            NLogOp.of(NIndexFirstIterator.class)
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
