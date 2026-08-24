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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.artifact.NTypedFilters;
import net.thevpc.nuts.ext.NExtensions;

/**
 * @app.category Base
 */
public interface NIdFilterRPI extends NTypedFilters<NIdFilter> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NIdFilterRPI of() {
       return NExtensions.of(NIdFilterRPI.class);
    }

    /**
     * By value.
     *
     * @param id id
     * @return by value result
     */
    NIdFilter byValue(NId id);

    /**
     * By default version.
     *
     * @param defaultVersion default version
     * @return by default version result
     */
    NIdFilter byDefaultVersion(Boolean defaultVersion);


    /**
     * By name.
     *
     * @param names names
     * @return by name result
     */
    NIdFilter byName(String... names);

}
