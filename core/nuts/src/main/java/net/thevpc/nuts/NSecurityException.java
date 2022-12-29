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
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NSecurityException extends SecurityException implements NSessionAwareExceptionBase {

    /**
     * workspace
     */
    private final NSession session;
    private final NMsg formattedMessage;

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param session session
     * @param message the detail message.
     */
    public NSecurityException(NSession session, NMsg message) {
        super(NException.messageToString(message, session));
        this.session = session;
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param session session
     * @param message message
     * @param cause   cause
     */
    public NSecurityException(NSession session, NMsg message, Throwable cause) {
        super(NException.messageToString(message, session), cause);
        this.session = session;
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    @Override
    public NMsg getFormattedMessage() {
        return formattedMessage;
    }

    /**
     * current workspace
     *
     * @return current workspace
     */
    public NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }
}
