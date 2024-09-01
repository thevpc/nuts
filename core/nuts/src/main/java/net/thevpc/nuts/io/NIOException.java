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
package net.thevpc.nuts.io;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.NExceptionBase;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

/**
 * Exception thrown when copy validation fails
 *
 * @app.category Exceptions
 */
public class NIOException extends NException {

    /**
     * Constructs a new Validation Exception
     *
     * @param session workspace
     * @param message message
     */
    public NIOException(NSession session, NMsg message) {
        super(session, message);
    }

    /**
     * Constructs a new Validation Exception
     *
     * @param session workspace
     * @param message message
     * @param cause   cause
     */
    public NIOException(NSession session, NMsg message, Throwable cause) {
        super(session, message, cause);
    }

    /**
     * Constructs a new Validation Exception
     *
     * @param session workspace
     * @param cause   cause
     */
    public NIOException(NSession session, Throwable cause) {
        super(session,
                cause == null ? null
                        : (cause instanceof NExceptionBase) ?
                        ((NExceptionBase) cause).getFormattedMessage()
                        : NMsg.ofPlain(cause.getMessage() == null ? "error" : cause.getMessage()),
                cause);
    }
}
