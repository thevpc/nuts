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

import net.thevpc.nuts.NListener;

/**
 * Map Listener to catch updates
 *
 * @param <V> key type
 * @app.category Base
 * @since 0.2.0
 */
public interface NObservableSetListener<V> extends NListener {

    /**
     * Invoked when item added
     *
     * @param value   value
     */
    default void itemAdded(V value) {

    }

    /**
     * Invoked when item removed
     *
     * @param value   value
     */
    default void itemRemoved(V value) {

    }

    /**
     * Invoked when item updated
     *
     * @param newValue new value
     * @param oldValue old value
     */
    default void itemUpdated(V newValue, V oldValue) {

    }
}
