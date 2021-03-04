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


/**
 * @author thevpc
 * @since 0.5.4
 * @category Config
 */
public interface NutsRepositoryConfigManager {

    String getUuid();

    /**
     * name is the name attributed by the containing workspace. It is defined in
     * NutsRepositoryRef
     *
     * @return local name
     */
    String getName();

    /**
     * global name is independent from workspace
     *
     * @return repository global (workspace independent) name
     */
    String getGlobalName();



    NutsRepositoryRef getRepositoryRef();
    
    String getType();

    String getGroups();

    int getSpeed();

    int getSpeed(NutsSession session);


    boolean isTemporary();

    boolean isIndexSubscribed(NutsSession session);

    /**
     * return repository configured location as string
     *
     * @param expand when true, location will be expanded (~ and $ params will
     *               be expanded)
     * @return repository location
     */
    String getLocation(boolean expand);

    String getStoreLocation();

    String getStoreLocation(NutsStoreLocation folderType);

    NutsRepositoryConfigManager setIndexEnabled(boolean enabled, NutsUpdateOptions options);

    boolean isIndexEnabled();

    NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled, NutsUpdateOptions options);

    int getDeployOrder();

    NutsRepositoryConfigManager setEnabled(boolean enabled, NutsUpdateOptions options);

    NutsRepositoryConfigManager setTemporary(boolean enabled, NutsUpdateOptions options);

    boolean isEnabled();

    NutsRepositoryConfigManager subscribeIndex(NutsSession session);

    NutsRepositoryConfigManager unsubscribeIndex(NutsSession session);


    boolean isSupportedMirroring();

    NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session);

    NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session);

    NutsRepository[] getMirrors(NutsSession session);

    /**
     * search for (or throw error) a repository with the given repository name or id.
     *
     * @param repositoryIdOrName repository name or id
     * @param session session
     * @return found repository or throw an exception
     * @throws NutsRepositoryNotFoundException if not found
     */
    NutsRepository getMirror(String repositoryIdOrName, NutsSession session);

    /**
     * search for (or return null) a repository with the given repository name or id.
     *
     * @param repositoryIdOrName repository name or id
     * @param session session
     * @return found repository or return null
     */
    NutsRepository findMirror(String repositoryIdOrName, NutsSession session);

    /**
     * add new repository
     *
     * @param options repository definition
     * @return {@code this} instance
     */
    NutsRepository addMirror(NutsAddRepositoryOptions options);

    /**
     * @param repositoryId repository id pr id
     * @param options      remove options
     * @return {@code this} instance
     */
    NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options);

    NutsStoreLocationStrategy getStoreLocationStrategy();
}
