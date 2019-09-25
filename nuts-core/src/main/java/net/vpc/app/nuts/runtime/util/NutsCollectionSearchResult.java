/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.runtime.AbstractNutsSearchResult;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;

/**
 *
 * @author vpc
 * @param <T>
 */
public class NutsCollectionSearchResult<T> extends AbstractNutsSearchResult<T> {

    private final Object o;
    private final char type;

    public NutsCollectionSearchResult(NutsWorkspace ws, String nutsBase) {
        super(ws, nutsBase);
        this.o = null;
        this.type = 'n';
    }

    public NutsCollectionSearchResult(NutsWorkspace ws, String nutsBase, Iterator<T> o) {
        super(ws, nutsBase);
        if (o == null) {
            this.o = null;
            this.type = 'n';
        } else {
            this.o = o;
            this.type = 'i';
        }
    }

    public NutsCollectionSearchResult(NutsWorkspace ws, String nutsBase, Collection<T> o) {
        super(ws, nutsBase);
        if (o == null) {
            this.o = null;
            this.type = 'n';
        } else {
            this.o = o;
            this.type = (o instanceof List) ? 'l' : 'c';
        }
    }

    public NutsCollectionSearchResult(NutsWorkspace ws, String nutsBase, List<T> o) {
        super(ws, nutsBase);
        if (o == null) {
            this.o = null;
            this.type = 'n';
        } else {
            this.o = o;
            this.type = 'l';
        }
    }

    @Override
    public List<T> list() {
        switch (type) {
            case 'n':
                return Collections.emptyList();
            case 'i':
                return CoreCommonUtils.toList((Iterator<T>) o);
            case 'l':
                return (List<T>) o;
            case 'c':
                return new ArrayList<>((Collection<T>) o);
        }
        throw new NutsUnsupportedArgumentException(ws, "Illegal type " + type);
    }

    @Override
    public Iterator<T> iterator() {
        switch (type) {
            case 'n':
                return IteratorUtils.emptyIterator();
            case 'i':
                return (Iterator<T>) o;
            case 'l':
                return ((Collection<T>) o).iterator();
            case 'c':
                return ((Collection<T>) o).iterator();
        }
        throw new NutsUnsupportedArgumentException(ws, "Illegal type " + type);
    }

    @Override
    public Stream<T> stream() {
        switch (type) {
            case 'n':
                return Collections.<T>emptyList().stream();
            case 'i':
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) o, Spliterator.ORDERED), false);
            case 'l':
                return ((Collection<T>) o).stream();
            case 'c':
                return ((Collection<T>) o).stream();
        }
        throw new NutsUnsupportedArgumentException(ws, "Illegal type " + type);
    }

    @Override
    public String toString() {
        switch (type) {
            case 'n':
                return "NullBasedResult" + "@" + Integer.toHexString(hashCode());
            case 'i':
                return "IteratorBasedResult" + "@" + Integer.toHexString(hashCode());
            case 'l':
                return "ListBasedResult" + "@" + Integer.toHexString(hashCode());
            case 'c':
                return "CollectionBasedResult" + "@" + Integer.toHexString(hashCode());
        }
        return super.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.o);
        hash = 47 * hash + this.type;
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
        final NutsCollectionSearchResult<?> other = (NutsCollectionSearchResult<?>) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        return true;
    }

}
