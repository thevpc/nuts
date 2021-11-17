/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.NutsSession;

import java.util.*;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsCollectionStream<T> extends AbstractNutsStream<T> {

    private final Collection<T> o;

    public NutsCollectionStream(NutsSession session, String nutsBase, Collection<T> o) {
        super(session, nutsBase);
        this.o = o;
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
    public Iterator<T> iterator() {
        return ((Collection<T>) o).iterator();
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
        final NutsCollectionStream<?> other = (NutsCollectionStream<?>) obj;
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
