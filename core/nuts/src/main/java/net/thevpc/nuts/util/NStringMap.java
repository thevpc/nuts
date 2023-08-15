/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.util;

import java.util.*;

public class NStringMap<V> {
    private Map<String, V> map;
    private char separator;

    public NStringMap(Map<String, V> map, char separator) {
        NAssert.requireNonNull(map, "map");
        this.map = map;
        this.separator = separator;
    }

    public NStringMap<V> clear() {
        map.clear();
        return this;
    }

    public int size() {
        return map.size();
    }

    public char getSeparator() {
        return separator;
    }

    public Map<String, V> toMap(String prefix) {
        NAssert.requireNonNull(prefix, "prefix");
        if (prefix.isEmpty()) {
            return new LinkedHashMap<>(map);
        }
        Map<String, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<String, V>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, V> e = iterator.next();
            String k = e.getKey();
            if (k.startsWith(prefix)) {
                if (k.length() == prefix.length()) {
                    result.put("", e.getValue());
                } else if (k.charAt(prefix.length()) == separator) {
                    result.put(k.substring(prefix.length() + 1), e.getValue());
                }
            } else {
                //just ignore!
            }
        }
        return result;
    }

    public Map<String, V> toMap() {
        return new LinkedHashMap<>(map);
    }

    public NStringMap<V> removeAll(String prefix) {
        NAssert.requireNonNull(prefix, "prefix");
        if (prefix.isEmpty()) {
            return this;
        }
        for (Iterator<Map.Entry<String, V>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, V> e = iterator.next();
            String k = e.getKey();
            if (k.startsWith(prefix)) {
                if (k.length() == prefix.length()) {
                    iterator.remove();
                } else if (k.charAt(prefix.length()) == separator) {
                    iterator.remove();
                }
            } else {
                //just ignore!
            }
        }
        return this;
    }

    public NStringMap<V> putAll(Map<String, V> values) {
        NAssert.requireNonNull(values, "values");
        map.putAll(values);
        return this;
    }

    public V put(String prefix, String key, V value) {
        NAssert.requireNonNull(prefix, "prefix");
        return map.put(keyOf(prefix, key), value);
    }

    public V put(String key, V value) {
        return map.put(key, value);
    }

    public V get(String key) {
        return map.get(key);
    }

    public NOptional<V> getOptional(String prefix, String key) {
        return getOptional(keyOf(prefix, key));
    }

    public NOptional<V> getOptional(String key) {
        V v = map.get(key);
        if (v == null) {
            if (map.containsKey(key)) {
                return NOptional.ofNull();
            }
            return NOptional.ofNamedEmpty(key);
        }
        return NOptional.of(v);
    }

    public V set(String prefix, String key, V value) {
        NAssert.requireNonNull(prefix, "prefix");
        if (value == null) {
            return map.remove(keyOf(prefix, key));
        } else {
            return map.put(keyOf(prefix, key), value);
        }
    }

    public V set(String key, V value) {
        if (value == null) {
            return map.remove(key);
        } else {
            return map.put(key, value);
        }
    }

    public V remove(String prefix, String key) {
        NAssert.requireNonNull(prefix, "prefix");
        return map.remove(keyOf(prefix, key));
    }

    public Set<String> nextKeys(String prefix) {
        Set<String> keys = new LinkedHashSet<>();
        NAssert.requireNonNull(prefix, "prefix");
        for (Iterator<Map.Entry<String, V>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, V> e = iterator.next();
            String k = e.getKey();
            if (k.startsWith(prefix)) {
                if (k.length() == prefix.length()) {
                    //
                } else if (k.charAt(prefix.length()) == separator) {
                    String n = k.substring(prefix.length() + 1);
                    int d = n.indexOf(separator);
                    if (d >= 0) {
                        keys.add(n.substring(0, d));
                    } else {
                        keys.add(n);
                    }
                }
            } else {
                //just ignore!
            }
        }
        return keys;
    }

    public NStringMap<V> putAll(String prefix, Map<String, V> values) {
        NAssert.requireNonNull(prefix, "prefix");
        NAssert.requireNonNull(values, "values");
        for (Map.Entry<String, V> e : values.entrySet()) {
            String k = e.getKey();
            map.put(keyOf(prefix, k), e.getValue());
        }
        return this;
    }

    private String keyOf(String prefix, String k) {
        return k.isEmpty() ? prefix : (prefix + separator + k);
    }

    public NStringMap<V> copy() {
        return new NStringMap<>(new LinkedHashMap<>(map), separator);
    }
}
