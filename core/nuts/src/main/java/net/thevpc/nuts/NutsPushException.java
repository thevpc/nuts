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
 * Push Exception
 *
 * @since 0.5.4
 * %category Exception
 */
public class NutsPushException extends NutsException {

    /**
     * artifact id
     */
    private final String id;

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     */
    public NutsPushException(NutsWorkspace workspace, String id) {
        this(workspace, id, null, null);
    }

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     * @param message message
     */
    public NutsPushException(NutsWorkspace workspace, String id, String message) {
        this(workspace, id, message, null);
    }

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     * @param message message
     * @param cause cause
     */
    public NutsPushException(NutsWorkspace workspace, String id, String message, Throwable cause) {
        super(workspace, message == null ? ("Unable to push " + id) : message, cause);
        this.id = id == null ? "<null>" : id;
    }

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     */
    public NutsPushException(NutsWorkspace workspace, NutsId id) {
        this(workspace, id, null, null);
    }

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     * @param message message
     */
    public NutsPushException(NutsWorkspace workspace, NutsId id, String message) {
        this(workspace, id, message, null);
    }

    /**
     * Constructs a new NutsPushException exception
     * @param workspace workspace
     * @param id artifact id
     * @param message message
     * @param cause cause
     */
    public NutsPushException(NutsWorkspace workspace, NutsId id, String message, Throwable cause) {
        this(workspace, id == null ? null : id.toString(), message, cause);
    }

    /**
     * artifact id
     * @return artifact id
     */
    public String getId() {
        return id;
    }

}
