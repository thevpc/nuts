/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.bundles.iter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class QueueIterator<T> implements Iterator<T> {

    private Queue<Iterator<T>> children = new LinkedList<Iterator<T>>();
    private int size;

    public void addNonNull(Iterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(Iterator<T> child) {
        child = IteratorUtils.nullifyIfEmpty(child);
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

    @Override
    public String toString() {
        return "QueueIterator(" +
                children +
                ')';
    }
}
