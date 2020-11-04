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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.core.common;

import net.thevpc.nuts.NutsMapListener;

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public class DefaultObservableMap<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {

    private Map<K, V> base = new HashMap<>();
    private List<NutsMapListener<K, V>> listeners;


    public void addListener(NutsMapListener<K, V> listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeListener(NutsMapListener<K, V> listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public NutsMapListener<K, V>[] getListeners() {
        return listeners.toArray(new NutsMapListener[0]);
    }

    @Override
    public V put(K key, V value) {
        if (base.containsKey(key)) {
            V old = base.put(key, value);
            if (listeners != null) {
                for (NutsMapListener<K, V> listener : listeners) {
                    listener.entryUpdated(key, value, old);
                }
            }
            return old;
        } else {
            V old = base.put(key, value);
            if (listeners != null) {
                for (NutsMapListener<K, V> listener : listeners) {
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
            for (NutsMapListener<K, V> listener : listeners) {
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
                        for (NutsMapListener<K, V> listener : listeners) {
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
