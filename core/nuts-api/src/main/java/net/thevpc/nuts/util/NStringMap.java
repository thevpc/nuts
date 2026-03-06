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

import java.util.*;

public interface NStringMap<V> extends NCopiable {

    static <V> NStringMap<V> of(Map<String, V> map, char separator) {
        return NCollectionsRPI.of().stringMap(map, separator);
    }

    NStringMap<V> clear();

    int size();

    char getSeparator();

    Map<String, V> toMap(String prefix);

    Map<String, V> toMap();

    NStringMap<V> removeAll(String prefix);

    NStringMap<V> putAll(Map<String, V> values);

    V put(String prefix, String key, V value);

    V put(String key, V value);

    V get(String key);

    NOptional<V> getOptional(String prefix, String key);

    NOptional<V> getOptional(String key);

    V set(String prefix, String key, V value);

    V set(String key, V value);

    V remove(String prefix, String key);

    Set<String> nextKeys(String prefix);

    NStringMap<V> putAll(String prefix, Map<String, V> values);

    NStringMap<V> copy();
}
