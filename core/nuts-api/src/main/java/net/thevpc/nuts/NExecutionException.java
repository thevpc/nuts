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
 * Standard Execution thrown when an artifact fails to run.
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NExecutionException extends NException implements NExceptionWithExitCodeBase {

    public static final int SUCCESS = 0;
    public static final int ERROR_1 = 1;
    public static final int ERROR_2 = 2;
    public static final int ERROR_3 = 3;
    public static final int ERROR_4 = 4;
    public static final int ERROR_5 = 5;
    public static final int ERROR_250 = 250;
    public static final int ERROR_251 = 251;
    public static final int ERROR_252 = 252;
    public static final int ERROR_253 = 253;
    public static final int ERROR_254 = 254;
    public static final int ERROR_255 = 255;
    private final int exitCode;

    /**
     * Constructs a new NutsExecutionException exception
     *
     * @param message  message
     * @param exitCode exit code
     */
    public NExecutionException(NMsg message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     *
     * @param message message
     * @param cause   cause
     */
    public NExecutionException(NMsg message, Throwable cause) {
        this(message, cause, ERROR_255);
    }

    /**
     * Constructs a new NutsExecutionException exception
     *
     * @param message  message
     * @param cause    cause
     * @param exitCode exit code
     */
    public NExecutionException(NMsg message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     *
     * @param message            message
     * @param cause              cause
     * @param enableSuppression  whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     * @param exitCode           exit code
     */
    public NExecutionException(NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int exitCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.exitCode = exitCode;
    }

    /**
     * artifact exit code
     *
     * @return artifact exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        String m = getMessage();
        if (m == null) {
            if (getCause() != null) {
                return "NutsExecutionException: " + getCause();
            }
            return super.toString();
        }
        return m;
    }
}
