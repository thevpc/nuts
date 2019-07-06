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

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NutsRepository {

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * in memory repositories (and hence fastest) repositories.
     */
    int SPEED_FASTEST = 1000000;
    
    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * local file system repositories (and hence faster) repositories.
     */
    int SPEED_FASTER = 100000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * local network repositories (and hence fast) repositories.
     */
    int SPEED_FAST = 10000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories (and hence slow) repositories.
     */
    int SPEED_SLOW = 1000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories with limited known bandwidth
     * (and hence slower) repositories.
     */
    int SPEED_SLOWER = 100;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories with very limited known bandwidth
     * (and hence slowest) repositories.
     */
    int SPEED_SLOWEST = 10;

    /**
     * return repository type
     * @return repository type
     */
    String repositoryType();

    /**
     * return repository type
     * @return repository type
     */
    String getRepositoryType();

    /**
     * return repository unique identifier
     * @return repository unique identifier
     */
    String getUuid();

    /**
     * return repository unique identifier
     * @return repository unique identifier
     */
    String uuid();

    /**
     * return parent workspace
     * @return parent workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * return parent workspace
     * @return parent workspace
     */
    NutsWorkspace workspace();

    /**
     * return parent repository or null
     * @return parent repository or null
     */
    NutsRepository getParentRepository();

    /**
     * return parent repository or null
     * @return parent repository or null
     */
    NutsRepository parentRepository();

    /**
     * return repository configuration manager
     * @return repository configuration manager
     */
    NutsRepositoryConfigManager config();

    /**
     * return repository security manager
     * @return repository security manager
     */
    NutsRepositorySecurityManager security();

    /**
     * create deploy command
     * @return deploy command
     */
    NutsDeployRepositoryCommand deploy();

    /**
     * create undeploy command
     * @return undeploy command
     */
    NutsRepositoryUndeployCommand undeploy();

    /**
     * create push command
     * @return push command
     */
    NutsPushRepositoryCommand push();

    /**
     * create fetchDescriptor command
     * @return fetchDescriptor command
     */
    NutsFetchDescriptorRepositoryCommand fetchDescriptor();

    /**
     * create fetchContent command
     * @return fetchContent command
     */
    NutsFetchContentRepositoryCommand fetchContent();

    /**
     * create search command
     * @return search command
     */
    NutsSearchRepositoryCommand search();

    /**
     * create searchVersions command
     * @return searchVersions command
     */
    NutsSearchVersionsRepositoryCommand searchVersions();

    /**
     * create update statistics command
     *
     * @return update statistics command
     */
    NutsUpdateRepositoryStatisticsCommand updateStatistics();

    /**
     * remove repository listener
     *
     * @param listener listener
     */
    void removeRepositoryListener(NutsRepositoryListener listener);

    /**
     * add repository listener
     *
     * @param listener listener
     */
    void addRepositoryListener(NutsRepositoryListener listener);

    /**
     * Repository Listeners
     *
     * @return Repository Listeners
     */
    NutsRepositoryListener[] getRepositoryListeners();

}
