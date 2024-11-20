/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NIterator;

import java.util.*;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NStreamFromList<T> extends NStreamBase<T> {

    private final List<?> o;

    public NStreamFromList(NSession session, String nutsBase, List<T> o) {
        super(nutsBase);
        this.o = o;
    }

    @Override
    public List<T> toList() {
        return (List<T>) o;
    }

    @Override
    public Stream<T> stream() {
        return ((Collection<T>) o).stream();
    }

    @Override
    public NIterator<T> iterator() {
        return NIterator.of(
                ((Collection<T>) o).iterator()
        );
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
        final NStreamFromList<?> other = (NStreamFromList<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ListBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
