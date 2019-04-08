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
import java.util.Queue;
import net.vpc.app.nuts.core.util.CoreCommonUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class QueueIterator<T> implements Iterator<T> {

    private Queue<Iterator<T>> children = new LinkedList<Iterator<T>>();
    private int size;
//    public static void main(String[] args) {
//        QueueIterator<String> q=new QueueIterator<String>();
////        q.add(Arrays.asList("a","b").iterator());
////        q.add(Arrays.asList("c","d").iterator());
//        while(q.hasNext()){
//            System.out.println(q.next());
//        }
//    }

    public void addNonNull(Iterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(Iterator<T> child) {
        child = CoreCommonUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(Iterator<T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        children.add(child);
        size++;
    }

    @Override
    public boolean hasNext() {
        while (!children.isEmpty()) {
            if (children.peek().hasNext()) {
                return true;
            }
            children.poll();
            size--;
        }
        return false;
    }

    @Override
    public T next() {
        return children.peek().next();
    }

    public int size() {
        return size;
    }

    @Override
    public void remove() {
        children.peek().remove();
    }

    public Iterator<T>[] getChildren() {
        return children.toArray(new Iterator[0]);
    }
    
}
