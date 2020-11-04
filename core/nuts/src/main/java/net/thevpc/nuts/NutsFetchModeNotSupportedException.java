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
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
 * Created by vpc on 1/15/17.
 *
 * @since 0.5.4
 * @category Exception
 */
public class NutsFetchModeNotSupportedException extends NutsException {

    private final String id;
    private final String repositoryName;
    private final String repositoryUuid;
    private final NutsFetchMode fetchMode;

    /**
     * Constructs a new NutsFetchModeNotSupportedException exception
     * @param workspace workspace
     * @param repo repository
     * @param fetchMode fetch mode
     * @param id artifact id
     * @param message message
     * @param cause cause
     */
    public NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String message, Exception cause) {
        super(workspace, PrivateNutsUtils.isBlank(message) ? ("Unsupported Fetch Mode " + fetchMode.id()) : message, cause);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.getName();
        this.repositoryUuid = repo == null ? null : repo.getUuid();
        this.fetchMode = fetchMode;
    }

    /**
     * Constructs a new NutsFetchModeNotSupportedException exception
     * @param workspace workspace
     * @param repo repository
     * @param fetchMode fetch mode
     * @param id artifact id
     * @param message message
     */
    public NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String message) {
        super(workspace, PrivateNutsUtils.isBlank(message) ? ("Unsupported Fetch Mode " + fetchMode.id()) : message);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.getName();
        this.repositoryUuid = repo == null ? null : repo.getUuid();
        this.fetchMode = fetchMode;
    }

    /**
     * repository name
     * @return repository name
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * repository uuid
     * @return repository uuid
     */
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    /**
     * fetch mode
     * @return fetch mode
     */
    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

    /**
     * artifact id
     * @return artifact id
     */
    public String getId() {
        return id;
    }
}
