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
 * <br>
 *
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
package net.thevpc.nuts.runtime.util.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class ClassClassMap extends ClassMap<Class> {

    public ClassClassMap() {
        super(Object.class, Class.class);
    }

    public void add(Class key) {
        super.put(key, key);
    }

    /**
     * Unsupported. use add() instead.
     *
     * @param classKey classKey
     * @param value value
     * @return throw error
     * @deprecated throw error, use add() instead.
     */
    @Override
    @Deprecated
    public Class put(Class classKey, Class value) {
        throw new IllegalArgumentException("Unsupported. use add() instead.");
    }

    @Override
    public Class[] getAllImpl(Class key) {
        List<Class> all = new ArrayList<Class>();
        for (Class value : values.values()) {
            if(key.isAssignableFrom(value)){
                all.add(value);
            }
        }
        return all.toArray(new Class[0]);
    }

}
