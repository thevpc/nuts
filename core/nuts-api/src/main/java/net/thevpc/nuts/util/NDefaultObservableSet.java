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

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public class NDefaultObservableSet<V> extends AbstractSet<V> implements NObservableSet<V> {

    private Set<V> base = new HashSet<>();
    private List<NObservableSetListener<V>> listeners;

    public NDefaultObservableSet(Set<V> base) {
        this.base = base==null?new HashSet<>() : base;
    }


    public void addSetListener(NObservableSetListener<V> listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeSetListener(NObservableSetListener<V> listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    @Override
    public boolean add(V v) {
        if (super.add(v)) {
            if (listeners != null) {
                for (NObservableSetListener<V> listener : listeners) {
                    listener.itemAdded(v);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            if (listeners != null) {
                for (NObservableSetListener<V> listener : listeners) {
                    listener.itemRemoved((V) o);
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NDefaultObservableSet) {
            return base.equals(((NDefaultObservableSet<?>) o).base);
        }
        return base.equals(o);
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    public List<NObservableSetListener<V>> getSetListeners() {
        return listeners;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return base.contains(o);
    }

    @Override
    public Object[] toArray() {
        return base.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return base.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return base.containsAll(c);
    }

    @Override
    public String toString() {
        return base.toString();
    }

    @Override
    public Iterator<V> iterator() {
        final Iterator<V> baseIterator = base.iterator();
        return new Iterator<V>() {
            V curr;

            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public V next() {
                curr = baseIterator.next();
                return curr;
            }

            @Override
            public void remove() {
                baseIterator.remove();
                if (listeners != null) {
                    for (NObservableSetListener<V> listener : listeners) {
                        listener.itemRemoved(curr);
                    }
                }
            }
        };

    }
}
