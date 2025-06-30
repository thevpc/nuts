/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.util.NIterator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NStreamFromJavaStream<T> extends NStreamBase<T> {

    private final Stream<T> o;
    public NStreamFromJavaStream(String nutsBase, Stream<T> o) {
        super(nutsBase);
        this.o = o;
    }

    @Override
    public List<T> toList() {
        return o.collect(Collectors.toList());
    }

    @Override
    public Stream<T> stream() {
        return o;
    }

    @Override
    public NIterator<T> iterator() {
        return NIterator.of(o.iterator());
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
        final NStreamFromJavaStream<?> other = (NStreamFromJavaStream<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StreamBasedResult" + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public void close() {
        o.close();
        super.close();
    }
}
