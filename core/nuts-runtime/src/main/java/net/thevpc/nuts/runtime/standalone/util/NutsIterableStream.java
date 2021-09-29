/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.commands.ws.AbstractNutsStream;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsIterableStream<T> extends AbstractNutsStream<T> {

    private final Iterable<T> o;

    public NutsIterableStream(NutsSession session, String nutsBase, Iterable<T> o) {
        super(session, nutsBase);
        this.o = o;
    }

    @Override
    public List<T> toList() {
        Iterator<T> source = iterator();
        List<T> target = new ArrayList<>();
        source.forEachRemaining(target::add);
        return target;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(o.spliterator(), false);
    }

    @Override
    public Iterator<T> iterator() {
        return o.iterator();
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
        final NutsIterableStream<?> other = (NutsIterableStream<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IterableBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
