/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.NutsIterator;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;

import java.util.*;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsEmptyStream<T> extends AbstractNutsStream<T> {

    public NutsEmptyStream(NutsSession session, String nutsBase) {
        super(session, nutsBase);
    }

    @Override
    public List<T> toList() {
        return Collections.emptyList();
    }

    @Override
    public Stream<T> stream() {
        return Collections.<T>emptyList().stream();
    }

    @Override
    public NutsIterator<T> iterator() {
        return IteratorBuilder.emptyIterator();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final NutsEmptyStream<?> other = (NutsEmptyStream<?>) obj;
        return true;
    }

    @Override
    public String toString() {
        return "NullBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
