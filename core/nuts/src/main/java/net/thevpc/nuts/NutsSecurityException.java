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
 * Thrown by Nuts Workspace to indicate a security violation.
 * @since 0.5.4
 * @app.category Exceptions
 */
public class NutsSecurityException extends SecurityException {

    /**
     * workspace
     */
    private final NutsSession session;

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param   session session
     */
    public NutsSecurityException(NutsSession session) {
        this.session = session;
    }

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param   session session
     * @param   message   the detail message.
     */
    public NutsSecurityException(NutsSession session, String message) {
        super(message);
        this.session = session;
    }

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param   session session
     * @param   message message
     * @param   cause cause
     */
    public NutsSecurityException(NutsSession session, String message, Throwable cause) {
        super(message, cause);
        this.session = session;
    }

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param   session session
     * @param   cause cause
     */
    public NutsSecurityException(NutsSession session, Throwable cause) {
        super(cause);
        this.session = session;
    }

    /**
     * current workspace
     * @return current workspace
     */
    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }
    
}
