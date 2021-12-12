/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public class IndexFirstIterator<T> extends NutsIteratorBase<T> {

    private Iterator<T> index;
    private Iterator<T> other;
    private long readFromIndex;
    private T nextItem;
    private boolean hasNextItem;
    private NutsSession session;

    public IndexFirstIterator(Iterator<T> index, Iterator<T> other,NutsSession session) {
        this.index = index;
        this.other = other;
        this.session = session;
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return elems
                .ofObject()
                .set("type","IndexFirst")
                .set("index", NutsDescribables.resolveOrDestruct(index,elems))
                .set("nonIndex", NutsDescribables.resolveOrDestruct(other,elems))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        if (index != null) {
            if (readFromIndex == 0) {
                try {
                    boolean v = index.hasNext();
                    if (v) {
                        hasNextItem = true;
                        nextItem = index.next();
                        readFromIndex++;
                    }
                    return v;
                } catch (NutsIndexerNotAccessibleException ex) {
                    index = null;
                }
            } else {
                try {
                    if (index.hasNext()) {
                        return true;
                    }
                    index = null;
                } catch (NutsIndexerNotAccessibleException ex) {
                    NutsLoggerOp.of(IndexFirstIterator.class,session)
                            .verb(NutsLogVerb.WARNING)
                            .level(Level.FINEST)
                            .log(NutsMessage.cstyle("error evaluating Iterator 'hasNext()' : %s", ex));
                    other = null;
                    return false;
                }
            }
        }
        if (other != null) {
            return other.hasNext();
        }
        return false;
    }

    @Override
    public T next() {
        if (hasNextItem) {
            hasNextItem = false;
            T t = nextItem;
            nextItem = null;
            return t;
        }
        if (index != null) {
            return index.next();
        }
        return other.next();
    }

    @Override
    public void remove() {
        if (index != null) {
            index.remove();
        } else if (other != null) {
            other.remove();
        }
    }

    @Override
    public String toString() {
        return "IndexFirstIterator(" + other + ")";
    }
}
