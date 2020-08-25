/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Map;

/**
 * execution context used in {@link NutsExecutorComponent} and
 * {@link NutsInstallerComponent}.
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsExecutionContext {

    /**
     * command name
     *
     * @return command name
     */
    String getCommandName();

    /**
     * executor options
     *
     * @return executor options
     */
    String[] getExecutorOptions();

    /**
     * executor properties
     *
     * @return executor properties
     */
    Map<String, String> getExecutorProperties();

    /**
     * command definition if any
     *
     * @return command definition if any
     */
    NutsDefinition getDefinition();

    /**
     * command arguments
     *
     * @return command arguments
     */
    String[] getArguments();

    /**
     * workspace
     *
     * @return workspace
     */
    NutsWorkspace workspace();

    /**
     * workspace
     *
     * @return workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * executor descriptor
     *
     * @return executor descriptor
     */
    NutsArtifactCall getExecutorDescriptor();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getExecSession();

    NutsSession getTraceSession();

    /**
     * execution environment
     *
     * @return execution environment
     */
    Map<String, String> getEnv();

    /**
     * current working directory
     *
     * @return current working directory
     */
    String getCwd();

    /**
     * when true, any non 0 exited command will throw an Exception
     *
     * @return fail fast status
     */
    boolean isFailFast();

    /**
     * when true, the component is temporary and is not registered withing the
     * workspace
     *
     * @return true if the component is temporary and is not registered withing
     * the workspace
     */
    boolean isTemporary();

    /**
     * execution type
     *
     * @return execution type
     */
    NutsExecutionType getExecutionType();
}
