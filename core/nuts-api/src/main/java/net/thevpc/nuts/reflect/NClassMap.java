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
 * <br>
 * <p>
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
package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.*;

/**
 * register parent class/interface and get value for all sub classes
 *
 * @author thevpc
 */
public interface NClassMap<K,V> {
    /**
     * Creates a new instance of of.
     *
     * @param clazz clazz
     * @return of result
     */
    static <K,V> NClassMap<K,V> of(Class<V> clazz) {
        return NUtilsRPI.of().createClassMap(clazz);
    }

    /**
     * Creates a new instance of of.
     *
     * @param keyType key type
     * @param valueType value type
     * @return of result
     */
    static <K,V> NClassMap<K,V> of(Class<K> keyType, Class<V> valueType) {
        return NUtilsRPI.of().createClassMap(keyType, valueType);
    }

    /**
     * Creates a new instance of of.
     *
     * @param keyType key type
     * @param valueType value type
     * @param initialCapacity initial capacity
     * @return of result
     */
    static <K,V> NClassMap<K,V> of(Class<K> keyType, Class<V> valueType, int initialCapacity) {
        return NUtilsRPI.of().createClassMap(keyType, valueType, initialCapacity);
    }

    /**
     * Creates a new instance of of class.
     *
     * @return of class result
     */
    static <K,V> NClassMap<K,V> ofClass() {
      /**
       * Return.
       *
       * @param NUtilsRPI.of().createClassClassMap( n utils rpi.of().create class class map(
       */
        return (NClassMap) NUtilsRPI.of().createClassClassMap();
    }

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<Class<? extends K>> keySet();

    /**
     * Entry set.
     *
     * @return entry set result
     */
    Set<Map.Entry<Class<? extends K>, V>> entrySet();

    /**
     * Values.
     *
     * @return values result
     */
    Collection<V> values();

    /**
     * Put.
     *
     * @param classKey class key
     * @param value value
     * @return put result
     */
    V put(Class<? extends K> classKey, V value);

    /**
     * Removes remove.
     *
     * @param classKey class key
     * @return remove result
     */
    V remove(Class<? extends K> classKey);

    /**
     * Returns the search path.
     *
     * @param classKey class key
     * @return get search path result
     */
    List<Class<? extends K>> getSearchPath(Class<? extends K> classKey);

    /**
     * Contains exact key.
     *
     * @param key key
     * @return contains exact key result
     */
    boolean containsExactKey(Class<? extends K> key);

    /**
     * Returns the exact.
     *
     * @param key key
     * @return get exact result
     */
    V getExact(Class<? extends K> key);

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    V get(Class<? extends K> key);

    /**
     * Finds the find matches.
     *
     * @param key key
     * @return find matches result
     */
    List<V> findMatches(Class<? extends K> key);

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();
    /**
     * Clear.
     */
    void clear();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

//    void expand();
}
