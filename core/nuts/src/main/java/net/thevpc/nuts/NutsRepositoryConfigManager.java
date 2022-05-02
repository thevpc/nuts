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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NutsRepositoryConfigManager {

    /**
     * global name is independent from workspace
     *
     * @return repository global (workspace independent) name
     */
    String getGlobalName();

    NutsRepositoryRef getRepositoryRef();

    String getType();

    String getGroups();

    NutsSpeedQualifier getSpeed();

    boolean isTemporary();

    NutsRepositoryConfigManager setTemporary(boolean enabled);

    boolean isIndexSubscribed();


    NutsRepositoryLocation getLocation();
    NutsPath getLocationPath();

    /**
     * return repository configured location as string
     *
     * @return repository location path
     */
    NutsPath getStoreLocation();

    NutsPath getStoreLocation(NutsStoreLocation folderType);

    boolean isIndexEnabled();

    NutsRepositoryConfigManager setIndexEnabled(boolean enabled);

    NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled);

    int getDeployWeight();

    boolean isEnabled();

    NutsRepositoryConfigManager setEnabled(boolean enabled);

    NutsRepositoryConfigManager subscribeIndex();

    NutsRepositoryConfigManager unsubscribeIndex();

    boolean isSupportedMirroring();

    NutsRepository findMirrorById(String repositoryNameOrId);

    NutsRepository findMirrorByName(String repositoryNameOrId);

    List<NutsRepository> getMirrors();

    /**
     * search for (or throw error) a repository with the given repository name
     * or id.
     *
     * @param repositoryIdOrName repository name or id
     * @return found repository or throw an exception
     * @throws NutsRepositoryNotFoundException if not found
     */
    NutsRepository getMirror(String repositoryIdOrName);

    /**
     * search for (or return null) a repository with the given repository name
     * or id.
     *
     * @param repositoryIdOrName repository name or id
     * @return found repository or return null
     */
    NutsRepository findMirror(String repositoryIdOrName);

    /**
     * add new repository
     *
     * @param options repository definition
     * @return {@code this} instance
     */
    NutsRepository addMirror(NutsAddRepositoryOptions options);

    /**
     * @param repositoryId repository id pr id
     * @return {@code this} instance
     */
    NutsRepositoryConfigManager removeMirror(String repositoryId);

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsSession getSession();

    NutsRepositoryConfigManager setSession(NutsSession session);

    Map<String, String> getConfigMap(boolean inherit);

    String getConfigProperty(String key, String defaultValue, boolean inherit);

    Map<String, String> getConfigMap();

    String getConfigProperty(String property, String defaultValue);

    NutsRepositoryConfigManager setConfigProperty(String property, String value);
}
