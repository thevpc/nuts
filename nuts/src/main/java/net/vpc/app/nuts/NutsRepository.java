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

import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NutsRepository {

    int SPEED_FASTEST = 100000;
    int SPEED_FASTER = 10000;
    int SPEED_FAST = 1000;
    int SPEED_SLOW = 100;
    int SPEED_SLOWEST = 10;

    String getRepositoryType();

    String getUuid();

    String uuid();

    NutsWorkspace getWorkspace();

    NutsRepository getParentRepository();

    NutsRepositoryConfigManager config();

    NutsRepositorySecurityManager security();

    void deploy(NutsRepositoryDeploymentOptions options, NutsRepositorySession session);

    void undeploy(NutsRepositoryUndeploymentOptions options, NutsRepositorySession session);

    void push(NutsId id, NutsPushCommand options, NutsRepositorySession session);

    NutsDescriptor fetchDescriptor(NutsId id, NutsRepositorySession session);

    NutsContent fetchContent(NutsId id, Path localPath, NutsRepositorySession session);

    Iterator<NutsId> find(NutsIdFilter filter, NutsRepositorySession session);

    Iterator<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session);

    /**
     * @param listener
     */
    void removeRepositoryListener(NutsRepositoryListener listener);

    /**
     * @param listener
     */
    void addRepositoryListener(NutsRepositoryListener listener);

    /**
     * @return
     */
    NutsRepositoryListener[] getRepositoryListeners();

    void updateStatistics();
}
