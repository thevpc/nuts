/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.util.NUnexpectedException;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NMsg;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NStreamFromNIterator<T> extends NStreamBase<T> {

    private final NIterator<T> o;
    public NStreamFromNIterator(String nutsBase, NIterator<T> o) {
        super(nutsBase);
        this.o = o;
    }

    @Override
    public List<T> toList() {
        return NCollections.list((Iterator<T>) o);
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
        final NStreamFromNIterator<?> other = (NStreamFromNIterator<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IteratorBasedResult" + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public void close() {
        if(o instanceof AutoCloseable){
            try {
                ((AutoCloseable)o).close();
            } catch (Exception e) {
                throw new NUnexpectedException(NMsg.ofC("unable to close iterator : %s",e));
            }
        }
        super.close();
    }

}
