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
 * Exception thrown when copy validation fails
 * @category Exception
 */
public class NutsIndexerNotAccessibleException extends NutsException {

    /**
     * Constructs a new Validation Exception
     * @param workspace workspace
     */
    public NutsIndexerNotAccessibleException(NutsWorkspace workspace) {
        super(workspace);
    }

    /**
     * Constructs a new Validation Exception
     * @param message message
     * @param workspace workspace
     */
    public NutsIndexerNotAccessibleException(NutsWorkspace workspace, String message) {
        super(workspace,message);
    }

    /**
     * Constructs a new Validation Exception
     * @param workspace workspace
     * @param message message
     * @param cause cause
     */
    public NutsIndexerNotAccessibleException(NutsWorkspace workspace, String message, Throwable cause) {
        super(workspace,message, cause);
    }

    /**
     * Constructs a new Validation Exception
     * @param workspace workspace
     * @param cause cause
     */
    public NutsIndexerNotAccessibleException(NutsWorkspace workspace, Throwable cause) {
        super(workspace,cause);
    }

}
