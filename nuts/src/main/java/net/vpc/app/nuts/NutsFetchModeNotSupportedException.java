/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Created by vpc on 1/15/17.
 *
 * @since 0.5.4
 */
public class NutsFetchModeNotSupportedException extends NutsException {

    private final String id;
    private final String repositoryName;
    private final String repositoryUuid;
    private final NutsFetchMode fetchMode;

    public NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String msg, Exception ex) {
        super(workspace, PrivateNutsUtils.isBlank(msg) ? ("Unsupported Fetch Mode " + fetchMode.id()) : msg, ex);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.config().name();
        this.repositoryUuid = repo == null ? null : repo.config().uuid();
        this.fetchMode = fetchMode;
    }

    public NutsFetchModeNotSupportedException(NutsWorkspace workspace, NutsRepository repo, NutsFetchMode fetchMode, String id, String msg) {
        super(workspace, PrivateNutsUtils.isBlank(msg) ? ("Unsupported Fetch Mode " + fetchMode.id()) : msg);
        this.id = id;
        this.repositoryName = repo == null ? null : repo.config().name();
        this.repositoryUuid = repo == null ? null : repo.config().uuid();
        this.fetchMode = fetchMode;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

    public String getId() {
        return id;
    }
}
