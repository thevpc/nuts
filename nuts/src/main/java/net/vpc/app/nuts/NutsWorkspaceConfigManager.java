/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vpc
 * @category Config
 * @since 0.5.4
 */
public interface NutsWorkspaceConfigManager {

    String getName();

    String getUuid();


    NutsWorkspaceStoredConfig stored();

    ClassLoader getBootClassLoader();

    URL[] getBootClassWorldURLs();

    Path getWorkspaceLocation();

    boolean isReadOnly();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force   when true, save will always be performed
     * @param session session
     * @return true if the save action was applied
     */
    boolean save(boolean force, NutsSession session);

    void save(NutsSession session);

    NutsWorkspaceOptionsBuilder optionsBuilder();

    NutsWorkspaceOptions options();

    NutsWorkspaceOptions getOptions();
    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options);

    void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options);

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);

    String getDefaultIdFilename(NutsId id);

    String getDefaultIdBasedir(NutsId id);

    NutsId createContentFaceId(NutsId id, NutsDescriptor desc);

    NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session);

    void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location, NutsUpdateOptions options);

    boolean isSupportedRepositoryType(String repositoryType);

    NutsRepositoryDefinition[] getDefaultRepositories();

    Set<String> getAvailableArchetypes(NutsSession session);

    Path resolveRepositoryPath(String repositoryLocation);

    NutsIndexStoreFactory getIndexStoreClientFactory();

    String getDefaultIdContentExtension(String packaging);

    String getDefaultIdExtension(NutsId id);

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getStoreLocations();

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getHomeLocations();

    Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation location);


    String getBootRepositories();

    String getJavaCommand();

    String getJavaOptions();

    boolean isGlobal();

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();


}
