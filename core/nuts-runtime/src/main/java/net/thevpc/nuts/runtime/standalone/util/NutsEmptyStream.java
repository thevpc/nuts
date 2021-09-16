/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUnsupportedArgumentException;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.core.commands.ws.AbstractNutsStream;
import net.thevpc.nuts.runtime.core.util.CoreCollectionUtils;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NutsEmptyStream<T> extends AbstractNutsStream<T> {

    public NutsEmptyStream(NutsSession ws, String nutsBase) {
        super(ws, nutsBase);
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
    public Iterator<T> iterator() {
        return IteratorUtils.emptyIterator();
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
