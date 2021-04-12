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
 * Standard Execution thrown when an artifact fails to run.
 *
 * @since 0.5.4
 * @category Exceptions
 */
public class NutsExecutionException extends NutsException {

    public static final int DEFAULT_ERROR_EXIT_CODE = 244;
    private final int exitCode;

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     */
    public NutsExecutionException(NutsSession session) {
        this(session, DEFAULT_ERROR_EXIT_CODE);
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsSession session, int exitCode) {
        super(session, "execution failed with error code " + exitCode);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param message message
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsSession session, String message, int exitCode) {
        super(session, message);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param message message
     * @param cause cause
     */
    public NutsExecutionException(NutsSession session, String message, Throwable cause) {
        this(session, message, cause, DEFAULT_ERROR_EXIT_CODE);
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param message message
     * @param cause cause
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsSession session, String message, Throwable cause, int exitCode) {
        super(session, message, cause);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param cause cause
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsSession session, Throwable cause, int exitCode) {
        super(session, cause == null ? "" : cause.getMessage(), cause);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param session workspace
     * @param message message
     * @param cause cause
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsSession session, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int exitCode) {
        super(session, message, cause, enableSuppression, writableStackTrace);
        this.exitCode = exitCode;
    }

    /**
     * artifact exit code
     * @return artifact exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        String m = getMessage();
        if(m==null){
            if(getCause()!=null){
                return "NutsExecutionException: "+getCause();
            }
            return super.toString();
        }
        return m;
    }
}
