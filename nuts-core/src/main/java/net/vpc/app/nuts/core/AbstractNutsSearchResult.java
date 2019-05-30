/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsTooManyElementsException;
import net.vpc.app.nuts.NutsSearchResult;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class AbstractNutsSearchResult<T> implements NutsSearchResult<T> {

    private String nutsBase;
    protected NutsWorkspace ws;

    public AbstractNutsSearchResult(NutsWorkspace ws,String nutsBase) {
        this.ws = ws;
        this.nutsBase = nutsBase;
    }

    @Override
    public T required() throws NutsNotFoundException {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new NutsNotFoundException(ws,nutsBase);
    }

    @Override
    public T first() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public T singleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                throw new NutsTooManyElementsException(ws);
            }
            return t;
        } else {
            throw new NutsNotFoundException(ws,nutsBase);
        }
    }

    @Override
    public long count() {
        long count = 0;
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            count++;
        }
        return count;
    }

    @Override
    public List<T> list() {
        List<T> list = new ArrayList<>();
        for (T a : this) {
            list.add(a);
        }
        return list;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) iterator(), Spliterator.ORDERED), false);
    }

}
