/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.commands.ws.AbstractNutsStream;
import net.thevpc.nuts.runtime.core.util.CoreCollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsJavaStream<T> extends AbstractNutsStream<T> {

    private final Stream<T> o;
    public NutsJavaStream(NutsSession ws, String nutsBase, Stream<T> o) {
        super(ws, nutsBase);
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
        final NutsJavaStream<?> other = (NutsJavaStream<?>) obj;
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StreamBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
