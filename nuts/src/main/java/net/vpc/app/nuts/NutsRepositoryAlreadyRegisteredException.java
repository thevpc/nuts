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
 * This exception is thrown when a repository location could no be loaded because
 *  the repository is already registered for the actual workspace.
 * @since 0.5.4
 * @category Exception
 */
public class NutsRepositoryAlreadyRegisteredException extends NutsRepositoryException {

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace  workspace
     * @param repository repository
     */
    public NutsRepositoryAlreadyRegisteredException(NutsWorkspace workspace, String repository) {
        super(workspace, repository,"Repository Already registered " + (repository == null ? "<null>" : repository),null);
    }

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace  workspace
     * @param repository repository
     * @param err error
     */
    public NutsRepositoryAlreadyRegisteredException(NutsWorkspace workspace, String repository, Throwable err) {
        super(workspace, repository,"Repository Already registered " + (repository == null ? "<null>" : repository), err);
    }
}
