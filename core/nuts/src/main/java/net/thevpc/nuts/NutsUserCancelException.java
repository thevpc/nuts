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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 *
 * @author vpc
 * @since 0.5.4
 * @category Exception
 */
public class NutsUserCancelException extends NutsExecutionException {

    public static final int DEFAULT_CANCEL_EXIT_CODE = 245;

    /**
     * Constructs a new NutsUserCancelException exception
     * @param workspace workspace
     */
    public NutsUserCancelException(NutsWorkspace workspace) {
        this(workspace, null);
    }

    /**
     * Constructs a new NutsUserCancelException exception
     * @param workspace workspace
     * @param message message
     */
    public NutsUserCancelException(NutsWorkspace workspace, String message) {
        this(workspace, message, DEFAULT_CANCEL_EXIT_CODE);
    }

    /**
     * Constructs a new NutsUserCancelException exception
     * @param workspace workspace
     * @param message message
     * @param exitCode exit code
     */
    public NutsUserCancelException(NutsWorkspace workspace, String message, int exitCode) {
        super(workspace, (message == null || message.trim().isEmpty()) ? "User cancelled operation" : message, exitCode);
    }
}
