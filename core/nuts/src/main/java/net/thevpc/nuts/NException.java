/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

/**
 * Base Nuts Exception. Parent of all Nuts defined Exceptions.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NException extends RuntimeException implements NSessionAwareExceptionBase {

    private final NSession session;
    private final NMsg formattedMessage;

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link #initCause}.
     *
     * @param session the workspace session of this Nuts Exception
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link #getMessage()} method.
     */
    public NException(NSession session, NMsg message) {
        super(NException.messageToString(message, session));
        NAssert.requireSession(session);
        this.session = session;
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by
     *                the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A {@code null} value is permitted, and
     *                indicates that the cause is nonexistent or unknown.)
     * @param session the workspace of this Nuts Exception
     */
    public NException(NSession session, NMsg message, Throwable cause) {
        super(NException.messageToString(message, session), cause);
        this.session = session;
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled
     * or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause. (A {@code null} value is permitted, and indicates
     *                           that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether suppression is enabled or not
     *                           disabled
     * @param writableStackTrace whether the stack trace should be writable or not
     * @param session            the workspace session of this Nuts Exception
     */
    public NException(NSession session, NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(NException.messageToString(message, session),
                cause, enableSuppression, writableStackTrace);
        this.session = session;
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    static NMsg validateFormattedMessage(NMsg message) {
        if (message == null) {
            message = NMsg.ofPlain("error occurred");
        }
        return message;
    }

    static NString messageToFormattedString(NMsg message, NSession session) {
        return NTexts.of(session).ofText(validateFormattedMessage(message));
    }

    static String messageToString(NMsg message, NSession session) {
        return messageToFormattedString(message, session).filteredText();
    }

    @Override
    public NMsg getFormattedMessage() {
        return formattedMessage;
    }

    /**
     * Returns the workspace of this Nuts Exception.
     *
     * @return the workspace of this {@code NutsException} instance (which may
     * be {@code null}).
     */
    public NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }
}
