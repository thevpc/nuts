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
 * Base exception for installation fails.
 *
 * @since 0.5.4
 * @category Exception
 */
public abstract class NutsInstallationException extends NutsException {

    /**
     * artifact id
     */
    private final String id;

    /**
     * Custom Constructor
     *
     * @param workspace workspace
     * @param id nuts id
     * @param msg message
     * @param ex exception
     */
    public NutsInstallationException(NutsWorkspace workspace, String id, String msg, Exception ex) {
        super(workspace, PrivateNutsUtils.isBlank(msg) ? "Unable to install " + (id == null ? "<null>" : id) : msg,ex);
        this.id = id;
    }

    /**
     * Custom Constructor
     *
     * @param workspace workspace
     * @param id nuts id
     * @param msg message
     * @param ex exception
     */
    public NutsInstallationException(NutsWorkspace workspace, NutsId id, String msg, Exception ex) {
        this(workspace, id == null ? null : id.toString(), msg, ex);
    }

    /**
     * nuts id
     *
     * @return nuts id
     */
    public String getId() {
        return id;
    }
}
