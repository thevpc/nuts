/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NIteratorStream<T> extends AbstractNStream<T> {

    private final NIterator<T> o;
    public NIteratorStream(NSession session, String nutsBase, NIterator<T> o) {
        super(session, nutsBase);
        this.o = o;
    }

    @Override
    public List<T> toList() {
        return CoreCollectionUtils.toList((Iterator<T>) o);
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) o, Spliterator.ORDERED), false);
    }

    @Override
    public NIterator<T> iterator() {
        return (NIterator<T>) o;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.o);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NIteratorStream<?> other = (NIteratorStream<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IteratorBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
