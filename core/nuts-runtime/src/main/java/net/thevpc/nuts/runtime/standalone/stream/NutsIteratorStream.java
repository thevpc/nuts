/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsIteratorStream<T> extends AbstractNutsStream<T> {

    private final NutsIterator<T> o;
    public NutsIteratorStream(NutsSession session, String nutsBase, NutsIterator<T> o) {
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
    public NutsIterator<T> iterator() {
        return (NutsIterator<T>) o;
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
        final NutsIteratorStream<?> other = (NutsIteratorStream<?>) obj;
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
