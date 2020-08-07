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

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceConfigManager {

    String name();

    String getName();

    String getUuid();

    Map<String, String> getEnv();

    String getEnv(String property, String defaultValue);

    NutsWorkspaceStoredConfig stored();

    ClassLoader getBootClassLoader();

    URL[] getBootClassWorldURLs();

    Path getWorkspaceLocation();

    boolean isReadOnly();

    void setEnv(String property, String value, NutsUpdateOptions options);

    void addImports(String[] importExpression, NutsAddOptions options);

    void removeAllImports(NutsRemoveOptions options);

    void removeImports(String[] importExpression, NutsRemoveOptions options);

    void setImports(String[] imports, NutsUpdateOptions options);

    Set<String> getImports();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @param session session
     * @return true if the save action was applied
     */
    boolean save(boolean force, NutsSession session);

    void save(NutsSession session);

    String[] getSdkTypes();

    boolean addSdk(NutsSdkLocation location, NutsAddOptions options);

    NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options);

    NutsSdkLocation findSdkByName(String sdkType, String locationName, NutsSession session);

    NutsSdkLocation findSdkByPath(String sdkType, Path path, NutsSession session);

    NutsSdkLocation findSdkByVersion(String sdkType, String version, NutsSession session);

    NutsSdkLocation findSdk(String sdkType, NutsSdkLocation location, NutsSession session);

    NutsSdkLocation getSdk(String sdkType, String requestedVersion, NutsSession session);

    NutsSdkLocation[] getSdks(String sdkType, NutsSession session);

    NutsSdkLocation[] searchSdkLocations(String sdkType, NutsSession session);

    NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, NutsSession session);

    /**
     * verify if the path is a valid sdk path and return null if not
     *
     * @param sdkType sdk type
     * @param path sdk path
     * @param preferredName preferredName
     * @param session session
     * @return null if not a valid jdk path
     */
    NutsSdkLocation resolveSdkLocation(String sdkType, Path path, String preferredName, NutsSession session);

    NutsWorkspaceOptions options();

    NutsWorkspaceOptions getOptions();

    void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactory, NutsAddOptions options);

    boolean removeCommandAliasFactory(String name, NutsRemoveOptions options);

    boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options);

    boolean removeCommandAlias(String name, NutsRemoveOptions options);

    /**
     * return alias definition for given name id and owner.
     *
     * @param name alias name, not null
     * @param forId if not null, the alias name should resolve to the given id
     * @param forOwner if not null, the alias name should resolve to the owner
     * @param session session
     * @return alias definition or null
     */
    NutsWorkspaceCommandAlias findCommandAlias(String name, NutsId forId, NutsId forOwner, NutsSession session);

    NutsWorkspaceCommandAlias findCommandAlias(String name, NutsSession session);

    List<NutsWorkspaceCommandAlias> findCommandAliases(NutsSession session);

    List<NutsWorkspaceCommandAlias> findCommandAliases(NutsId id, NutsSession session);

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

    NutsCommandAliasFactoryConfig[] getCommandFactories(NutsSession session);

    NutsRepositoryRef[] getRepositoryRefs(NutsSession session);

    NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session);

    void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location, NutsUpdateOptions options);

    boolean isSupportedRepositoryType(String repositoryType);

    /**
     * add temporary repository
     *
     * @param repository temporary repository
     * @param session session
     * @return repository
     */
    NutsRepository addRepository(NutsRepositoryModel repository, NutsSession session);

    NutsRepository addRepository(NutsRepositoryDefinition definition);

    NutsRepository addRepository(NutsAddRepositoryOptions options);

    /**
     * creates a new repository from the given {@code repositoryNamedUrl}.
     *
     * Accepted {@code repositoryNamedUrl} values are :
     * <ul>
     * <li>'local' : corresponds to a local updatable repository. will be named
     * 'local'</li>
     * <li>'m2', '.m2', 'maven-local' : corresponds the local maven folder
     * repository. will be named 'local'</li>
     * <li>'maven-central': corresponds the remote maven central repository.
     * will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-maven': corresponds the remote maven
     * vpc-public-maven git folder repository. will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-nuts': corresponds the remote nuts
     * vpc-public-nuts git folder repository. will be named 'local'</li>
     * <li>name=uri-or-path : corresponds the given uri. will be named name.
     * Here are some examples:
     * <ul>
     * <li>myremote=http://192.168.6.3/folder</li>
     * <li>myremote=/folder/subfolder</li>
     * <li>myremote=c:/folder/subfolder</li>
     * </ul>
     * </li>
     * <li>uri-or-path : corresponds the given uri. will be named uri's last
     * path component name. Here are some examples:
     * <ul>
     * <li>http://192.168.6.3/folder : will be named 'folder'</li>
     * <li>myremote=/folder/subfolder : will be named 'folder'</li>
     * <li>myremote=c:/folder/subfolder : will be named 'folder'</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param repositoryNamedUrl repositoryNamedUrl
     * @return created repository
     */
    NutsRepository addRepository(String repositoryNamedUrl, NutsSession session);

    NutsRepository findRepositoryById(String repositoryIdOrName, NutsSession session);

    NutsRepository findRepositoryByName(String repositoryIdOrName, NutsSession session);

    /**
     *
     * @param repositoryIdOrName repository id or name
     * @param session session
     * @return null if not found
     */
    NutsRepository findRepository(String repositoryIdOrName, NutsSession session);

    NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException;

    NutsWorkspaceConfigManager removeRepository(String locationOrRepositoryId, NutsRemoveOptions options);

    NutsRepository[] getRepositories(NutsSession session);

    NutsRepositoryDefinition[] getDefaultRepositories();

    Set<String> getAvailableArchetypes(NutsSession session);

    Path resolveRepositoryPath(String repositoryLocation);

    NutsIndexStoreFactory getIndexStoreClientFactory();

    NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository);

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

    String getApiVersion();

    NutsId getApiId();

    NutsId getRuntimeId();

    String getBootRepositories();

    String getJavaCommand();

    String getJavaOptions();

    boolean isGlobal();

    NutsOsFamily getOsFamily();

    NutsId getPlatform();

    NutsId getOs();

    NutsId getOsDist();

    NutsId getArch();

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();

}
