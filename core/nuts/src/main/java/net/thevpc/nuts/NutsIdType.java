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
 * Artifacts are organized according to {@code NutsIdType} to reflect how the artifact
 * should be managed by the workspace.
 * This information is available in {@link NutsDefinition}
 * %category Base
 */
public enum NutsIdType {
    /**
     * the id denotes a nuts api id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_API}
     */
    API,

    /**
     * the id denotes a nuts runtime id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_RUNTIME}
     */
    RUNTIME,

    /**
     * the id denotes a nuts known extension artifact.
     * an artifact is a valid nuts extension if it contains a property "nuts-extension" equal to
     * true in its descriptor.
     */
    EXTENSION,

    /**
     * the id denotes a nuts companion id artifact.
     * Default companions are
     * <ul>
     *     <li>net.thevpc.nuts.toolbox:nsh</li>
     *     <li>net.thevpc.nuts.toolbox:nadmin</li>
     *     <li>net.thevpc.nuts.toolbox:ndi</li>
     * </ul>
     *
     */
    COMPANION,

    /**
     * the id is none of the previous types
     */
    REGULAR
}