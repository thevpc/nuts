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
package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NMapListener;

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public class DefaultObservableMap<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {

    private Map<K, V> base = new HashMap<>();
    private List<NMapListener<K, V>> listeners;


    public void addListener(NMapListener<K, V> listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeListener(NMapListener<K, V> listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public List<NMapListener<K, V>> getListeners() {
        return listeners;
    }

    @Override
    public V put(K key, V value) {
        if (base.containsKey(key)) {
            V old = base.put(key, value);
            if (listeners != null) {
                for (NMapListener<K, V> listener : listeners) {
                    listener.entryUpdated(key, value, old);
                }
            }
            return old;
        } else {
            V old = base.put(key, value);
            if (listeners != null) {
                for (NMapListener<K, V> listener : listeners) {
                    listener.entryAdded(key, value);
                }
            }
            return old;
        }
    }

    @Override
    public V remove(Object key) {
        K kkey = (K) key;
        boolean found = base.containsKey(kkey);
        V r = base.remove(key);
        if (found && listeners != null) {
            for (NMapListener<K, V> listener : listeners) {
                listener.entryRemoved(kkey, r);
            }
        }
        return r;
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
    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return base.get(key);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> baseEntries = base.entrySet();
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<K, V>> baseIterator = baseEntries.iterator();
                return new Iterator<Entry<K, V>>() {
                    Entry<K, V> curr;

                    @Override
                    public boolean hasNext() {
                        return baseIterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        curr = baseIterator.next();
                        return curr;
                    }

                    @Override
                    public void remove() {
                        baseIterator.remove();
                        for (NMapListener<K, V> listener : listeners) {
                            listener.entryRemoved(curr.getKey(), curr.getValue());
                        }
                    }
                };
            }

            @Override
            public int size() {
                return baseEntries.size();
            }
        };
    }
}
