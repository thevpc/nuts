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
 */
public interface NutsWorkspaceConfigManager extends NutsEnvProvider {

    /**
     * boot time context information of loaded and running context
     *
     * @return
     */
    NutsBootContext getRunningContext();

    /**
     * boot time context information of requested context (from config)
     *
     * @return
     */
    NutsBootContext getBootContext();

    /**
     * current context information of requested context (from config) now
     *
     * @return
     */
    NutsBootContext getConfigContext();

    /**
     * current context information of requested context (from config)
     *
     * @return
     */
    NutsBootConfig getBootConfig();

    boolean isValidWorkspaceFolder();

    Path getWorkspaceLocation();

    boolean isReadOnly();

    void setEnv(String property, String value);

    Map<String, String> getRuntimeProperties();

    Path resolveNutsJarFile();

    void addImports(String... importExpression);

    void removeAllImports();

    void removeImports(String... importExpression);

    void setImports(String[] imports);

    String[] getImports();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return
     */
    boolean save(boolean force);

    void save();

    URL[] getBootClassWorldURLs();

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    void setUser(NutsUserConfig config);

    boolean isSecure();

    void setSecure(boolean secure);

//    void addRepositoryRef(NutsRepositoryRef repository);
//
//    void removeRepositoryRef(String repositoryName);
//
//    NutsRepositoryRef getRepositoryRef(String repositoryName);
    void removeUser(String userId);

    void setUsers(NutsUserConfig[] users);

    boolean addSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdkByName(String name, String locationName);

    NutsSdkLocation findSdkByPath(String name, Path path);

    NutsSdkLocation findSdkByVersion(String name, String version);

    NutsSdkLocation removeSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdk(String name, NutsSdkLocation location);

    void setBootConfig(NutsBootConfig other);

    String[] getSdkTypes();

    NutsSdkLocation getSdk(String type, String requestedVersion);

    NutsSdkLocation[] getSdks(String type);

    void setLogLevel(Level levek);

    NutsSdkLocation[] searchJdkLocations(PrintStream out);

    NutsSdkLocation[] searchJdkLocations(Path path, PrintStream out);

    NutsSdkLocation resolveJdkLocation(Path path);

    NutsWorkspaceOptions getOptions();

    byte[] decryptString(byte[] input);

    byte[] encryptString(byte[] input);

    void installCommandFactory(NutsWorkspaceCommandFactoryConfig commandFactory, NutsSession session);

    boolean uninstallCommandFactory(String name, NutsSession session);

    boolean installCommand(NutsWorkspaceCommandConfig command, NutsInstallCommandOptions options, NutsSession session);

    boolean uninstallCommand(String name, NutsUninstallOptions options, NutsSession session);

    NutsWorkspaceCommand findCommand(String name);

    NutsWorkspaceCommand findEmbeddedCommand(String name);

    List<NutsWorkspaceCommand> findCommands();

    List<NutsWorkspaceCommand> findCommands(NutsId id);

    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy);

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    void setStoreLocationLayout(NutsStoreLocationLayout layout);

    NutsStoreLocationLayout getStoreLocationLayout();

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, Path path);

    NutsOsFamily getPlatformOsFamily();

    NutsId getPlatformOs();

    NutsId getPlatformOsDist();

    NutsId getPlatformArch();

    String getPlatformOsHome(NutsStoreLocation location);

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();

    NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent);

    String getDefaultIdFilename(NutsId id);

    NutsId createComponentFaceId(NutsId id, NutsDescriptor desc);

    String getUuid();

    ClassLoader getBootClassLoader();

    NutsWorkspaceCommandFactoryConfig[] getCommandFactories();

    NutsRepositoryRef[] getRepositoryRefs();

    NutsWorkspaceListManager createWorkspaceListManager(String name);

    void setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folderType, String location);

    NutsId getApiId();

    NutsId getRuntimeId();

    boolean isSupportedRepositoryType(String repositoryType);

    NutsRepository addRepository(NutsRepositoryDefinition definition);

    NutsRepository addRepository(NutsCreateRepositoryOptions options);

    /**
     *
     * @param repositoryIdPath
     * @return null if not found
     */
    NutsRepository findRepository(String repositoryIdPath);

    NutsRepository getRepository(String repositoryIdPath) throws NutsRepositoryNotFoundException;

    void removeRepository(String locationOrRepositoryId);

    NutsRepository[] getRepositories();

    NutsRepositoryDefinition[] getDefaultRepositories();

    Set<String> getAvailableArchetypes();

    Path resolveRepositoryPath(String repositoryLocation);

    NutsIndexStoreClientFactory getIndexStoreClientFactory();

    NutsRepository createRepository(NutsCreateRepositoryOptions options, Path rootFolder, NutsRepository parentRepository);

    boolean isGlobal();

    String getDefaultIdComponentExtension(String packaging);

    String getDefaultIdExtension(NutsId id);

}
