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
package net.vpc.app.nuts.core.util.bundledlibs.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 1/9/17.
 * @param <F>
 * @param <T>
 */
public class ConvertedToListIterator<F, T> implements Iterator<T> {

    private final Iterator<F> base;
    private final Function<F, List<T>> converter;
    private final LinkedList<T> current=new LinkedList<>();

    public ConvertedToListIterator(Iterator<F> base, Function<F, List<T>> converter) {
        this.base = base;
        this.converter = converter;
    }

    @Override
    public boolean hasNext() {
        if(!current.isEmpty()){
            return true;
        }
        while(base.hasNext()){
            F f = base.next();
            List<T> c = converter.apply(f);
            current.addAll(c);
            if(!current.isEmpty()){
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        return current.poll();
    }

    @Override
    public void remove() {
        base.remove();
    }
}
