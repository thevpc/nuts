/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NCollectionStream<T> extends AbstractNStream<T> {

    private final Collection<T> o;
    private final Function<NSession, NElement> name;

    public NCollectionStream(NSession session, String nutsBase, Collection<T> o, Function<NSession, NElement> name) {
        super(session, nutsBase);
        this.o = o;
        this.name = name;
    }

    @Override
    public List<T> toList() {
        return new ArrayList<>((Collection<T>) o);
    }

    @Override
    public Stream<T> stream() {
        return ((Collection<T>) o).stream();
    }

    @Override
    public NIterator<T> iterator() {
        return NIterator.of(((Collection<T>) o).iterator(),name);
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
        final NCollectionStream<?> other = (NCollectionStream<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CollectionBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
