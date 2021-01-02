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

import net.thevpc.nuts.spi.NutsIndexStoreFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author thevpc
 * @category Config
 * @since 0.5.4
 */
public interface NutsWorkspaceConfigManager {

    String getName();

    String getUuid();


    NutsWorkspaceStoredConfig stored();

    ClassLoader getBootClassLoader();

    URL[] getBootClassWorldURLs();


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

    NutsWorkspaceBootConfig loadBootConfig(String path, boolean global, boolean followLinks, NutsSession session);

    NutsWorkspaceOptionsBuilder optionsBuilder();

    boolean isExcludedExtension(String extensionId,NutsWorkspaceOptions options) ;

    NutsWorkspaceOptions options();

    NutsWorkspaceOptions getOptions();

    NutsId createContentFaceId(NutsId id, NutsDescriptor desc);

    NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session);

    boolean isSupportedRepositoryType(String repositoryType, NutsSession session);

    NutsRepositoryDefinition[] getDefaultRepositories(NutsSession session);

    Set<String> getAvailableArchetypes(NutsSession session);

    String resolveRepositoryPath(String repositoryLocation, NutsSession session);

    NutsIndexStoreFactory getIndexStoreClientFactory();

    String getBootRepositories();

    String getJavaCommand();

    String getJavaOptions();

    boolean isGlobal();

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();


}
