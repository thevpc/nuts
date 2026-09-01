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
package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NOptional;

import java.util.*;

/**
 * NStringMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NStringMap<V> extends NCopiable {

    /**
     * Creates a new instance NStringMap.
     *
     * @param map map
     * @param separator separator
     * @return of result
     */
    static <V> NStringMap<V> of(Map<String, V> map, char separator) {
        return NUtilsRPI.of().createStringMap(map, separator);
    }

    /**
     * Clear.
     *
     * @return clear result
     */
    NStringMap<V> clear();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Separator.
     *
     * @return separator result
     */
    char separator();

    /**
     * Converts to map.
     *
     * @param prefix prefix
     * @return to map result
     */
    Map<String, V> toMap(String prefix);

    /**
     * Converts to map.
     *
     * @return to map result
     */
    Map<String, V> toMap();

    /**
     * Removes the specified all.
     *
     * @param prefix prefix
     * @return remove all result
     */
    NStringMap<V> removeAll(String prefix);

    /**
     * Put all.
     *
     * @param values values
     * @return put all result
     */
    NStringMap<V> putAll(Map<String, V> values);

    /**
     * Put.
     *
     * @param prefix prefix
     * @param key key
     * @param value value
     * @return put result
     */
    V put(String prefix, String key, V value);

    /**
     * Put.
     *
     * @param key key
     * @param value value
     * @return put result
     */
    V put(String key, V value);

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    V get(String key);

    /**
     * Returns the optional.
     *
     * @param prefix prefix
     * @param key key
     * @return get optional result
     */
    NOptional<V> getOptional(String prefix, String key);

    /**
     * Returns the optional.
     *
     * @param key key
     * @return get optional result
     */
    NOptional<V> getOptional(String key);

    /**
     * Sets the set.
     *
     * @param prefix prefix
     * @param key key
     * @param value value
     * @return set result
     */
    V set(String prefix, String key, V value);

    /**
     * Sets the set.
     *
     * @param key key
     * @param value value
     * @return set result
     */
    V set(String key, V value);

    /**
     * Removes remove.
     *
     * @param prefix prefix
     * @param key key
     * @return remove result
     */
    V remove(String prefix, String key);

    /**
     * Next keys.
     *
     * @param prefix prefix
     * @return next keys result
     */
    Set<String> nextKeys(String prefix);

    /**
     * Put all.
     *
     * @param prefix prefix
     * @param values values
     * @return put all result
     */
    NStringMap<V> putAll(String prefix, Map<String, V> values);

    /**
     * Copy.
     *
     * @return copy result
     */
    NStringMap<V> copy();
}
