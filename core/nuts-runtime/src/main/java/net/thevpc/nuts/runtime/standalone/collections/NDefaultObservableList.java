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
package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NObservableList;
import net.thevpc.nuts.collections.NObservableListListener;

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public class NDefaultObservableList<V> extends AbstractList<V> implements NObservableList<V> {

    private List<V> base;
    private List<NObservableListListener<V>> listeners;

    public NDefaultObservableList(List<V> base) {
        this.base = base==null?new ArrayList<>() : base;
    }

    public NDefaultObservableList() {
        this.base = new ArrayList<>();
    }

    @Override
    public V get(int index) {
        return base.get(index);
    }

    @Override
    public List<NObservableListListener<V>> listListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void addListListener(NObservableListListener<V> listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeListListener(NObservableListListener<V> listener) {
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
                int index = size() - 1;
                for (NObservableListListener<V> listener : listeners) {
                    listener.itemAdded(v, index);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i < 0) {
            return  false;
        }
        remove(i);
        return true;
    }

    @Override
    public V remove(int index) {
        if (index < 0 || index>=size()) {
            return null;
        }
        V o = super.remove(index);
        if (listeners != null) {
            for (NObservableListListener<V> listener : listeners) {
                listener.itemRemoved((V) o,index);
            }
        }
        return o;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            super.remove(fromIndex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NDefaultObservableList) {
            return base.equals(((NDefaultObservableList<?>) o).base);
        }
        return base.equals(o);
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    public List<NObservableListListener<V>> listeners() {
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
            int index=0;

            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public V next() {
                curr = baseIterator.next();
                index++;
                return curr;
            }

            @Override
            public void remove() {
                baseIterator.remove();
                if (listeners != null) {
                    for (NObservableListListener<V> listener : listeners) {
                        listener.itemRemoved(curr,index);
                    }
                }
            }
        };

    }
}
