/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class NIndexFirstIterator<T> extends NIteratorBase<T> {

    private Iterator<T> index;
    private Iterator<T> other;
    private long readFromIndex;
    private T nextItem;
    private boolean hasNextItem;

    public NIndexFirstIterator(Iterator<T> index, Iterator<T> other) {
        this.index = index;
        this.other = other;
    }

    @Override
    public NElement describe() {
        return NElement
                .ofObjectBuilder()
                .name("IndexFirst")
                .set("index", NDescribableElementSupplier.describeResolveOrDestruct(index))
                .set("nonIndex", NDescribableElementSupplier.describeResolveOrDestruct(other))
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
                } catch (NIndexerNotAccessibleException ex) {
                    index = null;
                }
            } else {
                try {
                    if (index.hasNext()) {
                        return true;
                    }
                    index = null;
                } catch (NIndexerNotAccessibleException ex) {
                    NLogOp.of(NIndexFirstIterator.class)
                            .verb(NLogVerb.WARNING)
                            .level(Level.FINEST)
                            .log(NMsg.ofC("error evaluating Iterator 'hasNext()' : %s", ex));
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
