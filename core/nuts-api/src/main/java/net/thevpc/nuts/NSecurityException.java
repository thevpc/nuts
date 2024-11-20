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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

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
     * @param message the detail message.
     */
    public NSecurityException(NMsg message) {
        super(NException.messageToString(message));
        this.session = NSession.of().orNull();
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    /**
     * Constructs a <code>NutsSecurityException</code> with the specified
     * parameters.
     *
     * @param message message
     * @param cause   cause
     */
    public NSecurityException(NMsg message, Throwable cause) {
        super(NException.messageToString(message), cause);
        this.session = NSession.of().orNull();
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
