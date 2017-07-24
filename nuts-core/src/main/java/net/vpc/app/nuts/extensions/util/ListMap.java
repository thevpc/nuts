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
package net.vpc.app.nuts.extensions.util;

import java.util.*;

/**
 * Created by vpc on 1/9/17.
 */
public class ListMap<K, V> {

    private Map<K, List<V>> base = new HashMap<K, List<V>>();

    public V getOne(K a) {
        List<V> all = base.get(a);
        if (all == null) {
            return null;
        }
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    public boolean contains(K a, V value) {
        List<V> all = base.get(a);
        if (all != null) {
            return all.contains(value);
        }
        return false;
    }

    public boolean remove(K a, V value) {
        List<V> all = base.get(a);
        if (all != null) {
            return all.remove(value);
        }
        return false;
    }

    public List<V> getAll(K a) {
        List<V> all = base.get(a);
        if (all == null) {
            return Collections.EMPTY_LIST;
        }
        if (all.size() > 0) {
            return Collections.unmodifiableList(all);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public Set<K> keySize() {
        return Collections.unmodifiableSet(base.keySet());
    }

    public int size() {
        return base.size();
    }

    public void add(K key, V value) {
        List<V> all = base.get(key);
        if (all == null) {
            all = new ArrayList<>();
            base.put(key, all);
        }
        all.add(value);
    }

    public boolean containsKey(K key) {
        return base.containsKey(key);
    }
}
