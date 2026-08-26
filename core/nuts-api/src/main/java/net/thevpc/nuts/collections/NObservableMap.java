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
package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public interface NObservableMap<K, V> extends Map<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NObservableMap<K, V> of(){
        return  NUtilsRPI.of().createObservableMap();
    }
    /**
     * Creates a new instance of of.
     *
     * @param base base
     * @return of result
     */
    static <K, V> NObservableMap<K, V> of(Map<K, V> base){
        return  NUtilsRPI.of().createObservableMap(base);
    }

    /**
     * Adds the specified map listener.
     *
     * @param listener listener
     */
    void addMapListener(NObservableMapListener<K, V> listener);

    /**
     * Removes the specified map listener.
     *
     * @param listener listener
     */
    void removeMapListener(NObservableMapListener<K, V> listener);

    /**
     * Map listeners.
     *
     * @return map listeners result
     */
    List<NObservableMapListener<K, V>> mapListeners();

}
