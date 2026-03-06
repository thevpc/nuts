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
package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.lang.reflect.Array;
import java.util.*;

/**
 * register parent class/interface and get value for all sub classes
 *
 * @author thevpc
 */
public interface NClassMap<V> {
    static <V> NClassMap<V> of(Class<V> clazz) {
        return NCollectionsRPI.of().classMap(clazz);
    }

    static <V> NClassMap<V> of(Class keyType, Class<V> valueType) {
        return NCollectionsRPI.of().classMap(keyType, valueType);
    }

    static <V> NClassMap<V> of(Class keyType, Class<V> valueType, int initialCapacity) {
        return NCollectionsRPI.of().classMap(keyType, valueType, initialCapacity);
    }

    static NClassMap<Class<?>> ofClass() {
        return NCollectionsRPI.of().classClassMap();
    }

    Set<V> allKeySet();

    Set<Class> keySet();

    Collection<V> values();

    V put(Class classKey, V value);

    V remove(Class classKey);

    Class[] getKeys(Class classKey);

    V getRequired(Class key);

    boolean containsExactKey(Class key);

    V getExact(Class key);

    V get(Class key);

    V[] getAllRequired(Class key);

    V[] getAll(Class key);

    void clear();

    int size();

    void expand();
}
