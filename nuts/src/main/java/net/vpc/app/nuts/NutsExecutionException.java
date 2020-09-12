/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Standard Execution thrown when an artifact fails to run.
 *
 * @since 0.5.4
 * @category Exception
 */
public class NutsExecutionException extends NutsException {

    public static final int DEFAULT_ERROR_EXIT_CODE = 244;
    private final int exitCode;

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     */
    public NutsExecutionException(NutsWorkspace workspace) {
        this(workspace, DEFAULT_ERROR_EXIT_CODE);
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsWorkspace workspace, int exitCode) {
        super(workspace, "Execution Failed with error code " + exitCode);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param message message
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsWorkspace workspace, String message, int exitCode) {
        super(workspace, message);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param message message
     * @param cause cause
     */
    public NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause) {
        this(workspace, message, cause, DEFAULT_ERROR_EXIT_CODE);
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param message message
     * @param cause cause
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause, int exitCode) {
        super(workspace, message, cause);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param cause cause
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsWorkspace workspace, Throwable cause, int exitCode) {
        super(workspace, cause == null ? "" : cause.getMessage(), cause);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new NutsExecutionException exception
     * @param workspace workspace
     * @param message message
     * @param cause cause
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     * @param exitCode exit code
     */
    public NutsExecutionException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int exitCode) {
        super(workspace, message, cause, enableSuppression, writableStackTrace);
        this.exitCode = exitCode;
    }

    /**
     * artifact exit code
     * @return artifact exit code
     */
    public int getExitCode() {
        return exitCode;
    }

}
