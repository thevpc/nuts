/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NStreamFromNIterable<T> extends NStreamBase<T> {

    private final NIterable<T> o;

    public NStreamFromNIterable(String nutsBase, NIterable<T> o) {
        super(nutsBase);
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
    public NIterator<T> iterator() {
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
        final NStreamFromNIterable<?> other = (NStreamFromNIterable<?>) obj;
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
