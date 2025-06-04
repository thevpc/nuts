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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class NQueueIterator<T> extends NIteratorBase<T> {

    private Queue<NIterator<? extends T>> children = new LinkedList<NIterator<? extends T>>();
    private int size;


    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .name("Queue")
                .addAll(
                        children
                                .stream().map(
                                        x -> NEDesc.describeResolveOrDestruct(x)
                                ).toArray(NElement[]::new)
                )
                .build();
    }

    public void addNonNull(NIterator<? extends T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(NIterator<? extends T> child) {
        child = NIteratorUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(NIterator<? extends T> child) {
        NAssert.requireNonNull(child, "child");
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

    public NIterator<T>[] getChildren() {
        return children.toArray(new NIterator[0]);
    }

    @Override
    public String toString() {
        return "QueueIterator(" +
                children +
                ')';
    }
}
