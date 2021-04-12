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
package net.thevpc.nuts;

/**
 * @category Base
 */
public interface NutsIdManager {
    NutsIdParser parser();
    NutsIdFormat formatter();

    NutsIdFormat formatter(NutsId id);

    NutsIdFilterManager filter();

    /**
     * create new instance of id builder
     * @return new instance of id builder
     */
    NutsIdBuilder builder();

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NutsId resolveId(Class clazz);

    /**
     * detect all nuts ids from resources containing the given class.
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    NutsId[] resolveIds(Class clazz);


    NutsSession getSession();

    NutsIdManager setSession(NutsSession session);
}
