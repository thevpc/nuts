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
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NTooManyElementsException extends NException {

    /**
     * Constructs a new NutsTooManyElementsException exception
     *
     * @param session workspace
     */
    public NTooManyElementsException(NSession session) {
        this(session, NMsg.ofNtf("too many elements"));
    }

    /**
     * Constructs a new NutsTooManyElementsException exception
     *
     * @param session workspace
     * @param message message
     */
    public NTooManyElementsException(NSession session, NMsg message) {
        super(session, message);
    }

    /**
     * Constructs a new NutsTooManyElementsException exception
     *
     * @param session workspace
     * @param message message
     * @param cause   cause
     */
    public NTooManyElementsException(NSession session, NMsg message, Throwable cause) {
        super(session, message, cause);
    }

    /**
     * Constructs a new NutsTooManyElementsException exception
     *
     * @param session            workspace
     * @param message            message
     * @param cause              cause
     * @param enableSuppression  whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public NTooManyElementsException(NSession session, NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(session, message, cause, enableSuppression, writableStackTrace);
    }

}
