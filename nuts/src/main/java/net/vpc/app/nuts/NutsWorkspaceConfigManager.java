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

import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceConfigManager extends NutsEnvProvider {

    String name();

    String getName();

    String getUuid();

    NutsWorkspaceStoredConfig stored();

    ClassLoader getBootClassLoader();

    URL[] getBootClassWorldURLs();

//    Path getBootNutsJar();
//
    Path getWorkspaceLocation();

    boolean isReadOnly();

    void setEnv(String property, String value);

    void addImports(String... importExpression);

    void removeAllImports();

    void removeImports(String... importExpression);

    void setImports(String[] imports);

    Set<String> getImports();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return
     */
    boolean save(boolean force);

    void save();

    void setLogLevel(Level levek);

    String[] getSdkTypes();

    boolean addSdk(NutsSdkLocation location, NutsAddOptions options);

    NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options);

    NutsSdkLocation findSdkByName(String sdkType, String locationName);

    NutsSdkLocation findSdkByPath(String sdkType, Path path);

    NutsSdkLocation findSdkByVersion(String sdkType, String version);

    NutsSdkLocation findSdk(String sdkType, NutsSdkLocation location);

    NutsSdkLocation getSdk(String sdkType, String requestedVersion);

    NutsSdkLocation[] getSdks(String sdkType);

    NutsSdkLocation[] searchSdkLocations(String sdkType, PrintStream out);

    NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, PrintStream out);

    /**
     * verify if the path is a valid a
     *
     * @param sdkType
     * @param path
     * @return null if not a valid jdk path
     */
    NutsSdkLocation resolveSdkLocation(String sdkType, Path path);

    NutsWorkspaceOptions options();

    NutsWorkspaceOptions getOptions();

    void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactory, NutsAddOptions options);

    boolean removeCommandAliasFactory(String name, NutsRemoveOptions options);

    boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options);

    boolean removeCommandAlias(String name, NutsRemoveOptions options);

    NutsWorkspaceCommandAlias findCommandAlias(String name);

    List<NutsWorkspaceCommandAlias> findCommandAliases();

    List<NutsWorkspaceCommandAlias> findCommandAliases(NutsId id);

    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy);

    void setStoreLocationLayout(NutsOsFamily layout);

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();

    String getDefaultIdFilename(NutsId id);

    String getDefaultIdBasedir(NutsId id);

    NutsId createComponentFaceId(NutsId id, NutsDescriptor desc);

    NutsCommandAliasFactoryConfig[] getCommandFactories();

    NutsRepositoryRef[] getRepositoryRefs();

    NutsWorkspaceListManager createWorkspaceListManager(String name);

    void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location);

    boolean isSupportedRepositoryType(String repositoryType);

    NutsRepository addRepository(NutsRepositoryDefinition definition);

    NutsRepository addRepository(NutsCreateRepositoryOptions options);

    /**
     *
     * @param repositoryIdOrName
     * @param transitive if true find into repositories mirrors
     * @return null if not found
     */
    NutsRepository findRepository(String repositoryIdOrName, boolean transitive);

    NutsRepository getRepository(String repositoryIdOrName) throws NutsRepositoryNotFoundException;

    NutsRepository getRepository(String repositoryIdOrName, boolean transitive) throws NutsRepositoryNotFoundException;

    NutsWorkspaceConfigManager removeRepository(String locationOrRepositoryId, NutsRemoveOptions options);

    NutsRepository[] getRepositories();

    NutsRepositoryDefinition[] getDefaultRepositories();

    Set<String> getAvailableArchetypes();

    Path resolveRepositoryPath(String repositoryLocation);

    NutsIndexStoreClientFactory getIndexStoreClientFactory();

    NutsRepository createRepository(NutsCreateRepositoryOptions options, Path rootFolder, NutsRepository parentRepository);

//    boolean isGlobal();
    String getDefaultIdComponentExtension(String packaging);

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

    String getRuntimeDependencies();

    String getExtensionDependencies();

    String getBootRepositories();

    String getJavaCommand();

    String getJavaOptions();

    boolean isGlobal();

    NutsOsFamily getPlatformOsFamily();

    NutsId getPlatformOs();

    NutsId getPlatformOsDist();

    NutsId getPlatformArch();
}
