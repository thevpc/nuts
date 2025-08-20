/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;
import net.thevpc.nuts.core.NAnyBootAwareExceptionBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

/**
 * Base Nuts Exception. Parent of all Nuts defined Exceptions.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NException extends RuntimeException implements NSessionAwareExceptionBase, NAnyBootAwareExceptionBase {

    private final NSession session;
    private final NMsg formattedMessage;

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link #getMessage()} method.
     */
    public NException(NMsg message) {
        super(NException.messageToString(message));
        this.session = NSession.get().orNull();
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
     */
    public NException(NMsg message, Throwable cause) {
        super(message==null?"error occurred":message.toString(), cause);
        this.session = NSession.get().orNull();
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
     */
    public NException(NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(NException.messageToString(message),
                cause, enableSuppression, writableStackTrace);
        this.session = NSession.get().orNull();
        this.formattedMessage = NException.validateFormattedMessage(message);
    }

    static NMsg validateFormattedMessage(NMsg message) {
        if (message == null) {
            message = NMsg.ofPlain("error occurred");
        }
        return message;
    }

    static NText messageToFormattedString(NMsg message) {
        if (NWorkspace.get().isNotPresent()) {
            throw new IllegalArgumentException(message==null?"missing workspace":message.toString());
        }
        return NText.of(validateFormattedMessage(message));
    }

    static String messageToString(NMsg message) {
        return messageToFormattedString(message).filteredText();
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


    public int processThrowable(NBootOptionsInfo options) {
        if (this.session != null) {
            return this.session.callWith(() -> NExceptionWorkspaceHandler.of().processThrowable(options.getApplicationArguments().toArray(new String[0]), this));
        } else {
            return NBootUtils.processThrowable(this, true, NBootUtils.resolveShowStackTrace(options), NBootUtils.resolveGui(options));
        }
    }
}
