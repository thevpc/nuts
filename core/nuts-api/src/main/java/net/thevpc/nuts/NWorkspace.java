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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reserved.NScopedWorkspace;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.util.*;

import java.io.Closeable;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by vpc on 1/5/17.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NWorkspace extends NWorkspaceBase, NComponent, Closeable {
    static NWorkspace of() {
        return get().get();
    }

    static NOptional<NWorkspace> get() {
        return NScopedWorkspace.currentWorkspace();
    }

    static void run(NRunnable runnable) {
        NScopedWorkspace.runWith(runnable);
    }

    static <T> T call(NCallable<T> callable) {
        return NScopedWorkspace.callWith(callable);
    }

    NWorkspace setSharedInstance();

    boolean isSharedInstance();

    void runWith(NRunnable runnable);

    <T> T callWith(NCallable<T> callable);

    /**
     * Workspace identifier, most likely to be unique cross machines
     *
     * @return uuid
     */
    String getUuid();

    /**
     * Workspace name
     *
     * @return name
     */
    String getName();

    String getHashName();

    NVersion getApiVersion();

    NId getApiId();

    NId getAppId();

    NId getRuntimeId();


    NPath getLocation();

    /// ////////////////// create new session
    NSession createSession();

    NSession currentSession();

    NExtensions extensions();

    void close();

    /// ////////////////////


    NWorkspace removeRepositoryListener(NRepositoryListener listener);

    NWorkspace addRepositoryListener(NRepositoryListener listener);

    List<NRepositoryListener> getRepositoryListeners();

    NWorkspace addUserPropertyListener(NObservableMapListener<String, Object> listener);

    NWorkspace removeUserPropertyListener(NObservableMapListener<String, Object> listener);

    List<NObservableMapListener<String, Object>> getUserPropertyListeners();

    NWorkspace removeWorkspaceListener(NWorkspaceListener listener);

    NWorkspace addWorkspaceListener(NWorkspaceListener listener);

    List<NWorkspaceListener> getWorkspaceListeners();

    NWorkspace removeInstallListener(NInstallListener listener);

    NWorkspace addInstallListener(NInstallListener listener);

    List<NInstallListener> getInstallListeners();

    /// ///////////////////////////////

    NRepository addRepository(NAddRepositoryOptions options);

    /**
     * creates a new repository from the given
     * {@code repositoryNamedUrl}.Accepted {@code repositoryNamedUrl} values are
     * :
     * <ul>
     * <li>'local' : corresponds to a local updatable repository.
     * <p>
     * will be named 'local'</li>
     * <li>'m2', '.m2', 'maven-local' : corresponds the local maven folder
     * repository. will be named 'local'</li>
     * <li>'maven-central': corresponds the remote maven central repository.
     * will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-maven': corresponds the remote maven
     * vpc-public-maven git folder repository. will be named 'local'</li>
     * <li>'maven-git', 'nuts-public': corresponds the remote nuts
     * nuts-public git folder repository. will be named 'local'</li>
     * <li>name=uri-or-path : corresponds the given uri. will be named name.
     * Here are some examples:
     * <ul>
     * <li>myremote=http://192.168.6.3/folder</li>
     * <li>myremote=/folder/subfolder</li>
     * <li>myremote=c:/folder/subfolder</li>
     * </ul>
     * </li>
     * <li>uri-or-path : corresponds the given uri. will be named uri's last
     * path package name. Here are some examples:
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
    NRepository addRepository(String repositoryNamedUrl);

    NOptional<NRepository> findRepositoryById(String repositoryIdOrName);

    NOptional<NRepository> findRepositoryByName(String repositoryIdOrName);

    /**
     * @param repositoryIdOrName repository id or name
     * @return null if not found
     */
    NOptional<NRepository> findRepository(String repositoryIdOrName);

    NWorkspace removeRepository(String locationOrRepositoryId);

    List<NRepository> getRepositories();

    NWorkspace removeAllRepositories();

    /// /////////


    /**
     * @return properties
     * @since 0.8.1
     */
    Map<String, Object> getProperties();

    /**
     * return property raw value
     *
     * @param property property name
     * @return property raw value
     * @since 0.8.1
     */
    NOptional<NLiteral> getProperty(String property);

    /**
     * @param property property
     * @param value    value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NWorkspace setProperty(String property, Object value);

    String getHostName();

    String getPid();

    NOsFamily getOsFamily();

    Set<NShellFamily> getShellFamilies();

    NShellFamily getShellFamily();

    NId getDesktopEnvironment();

    Set<NId> getDesktopEnvironments();

    NDesktopEnvironmentFamily getDesktopEnvironmentFamily();

    Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies();

    NId getPlatform();

    NId getOs();

    NId getOsDist();

    NId getArch();

    NArchFamily getArchFamily();

    boolean isGraphicalDesktopEnvironment();

    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    Path getDesktopPath();

    void addLauncher(NLauncherOptions launcher);

    List<String> buildEffectiveCommand(String[] cmd,
                                       NRunAs runAsMode,
                                       Set<NDesktopEnvironmentFamily> de,
                                       Function<String, String> sysWhich,
                                       Boolean gui,
                                       String rootName,
                                       String userName,
                                       String[] executorOptions
    );

    NPath getHomeLocation(NStoreType folderType);

    NPath getStoreLocation(NStoreType folderType);

    NPath getStoreLocation(NId id, NStoreType folderType);

    NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName);

    NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName);

    NStoreStrategy getStoreStrategy();

    NWorkspace setStoreStrategy(NStoreStrategy strategy);

    NStoreStrategy getRepositoryStoreStrategy();

    NOsFamily getStoreLayout();

    NWorkspace setStoreLayout(NOsFamily storeLayout);

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NStoreType, String> getStoreLocations();

    String getDefaultIdFilename(NId id);

    NPath getDefaultIdBasedir(NId id);

    String getDefaultIdContentExtension(String packaging);

    String getDefaultIdExtension(NId id);

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NHomeLocation, String> getHomeLocations();

    NPath getHomeLocation(NHomeLocation location);

    NPath getWorkspaceLocation();

    NWorkspace setStoreLocation(NStoreType folderType, String location);

    NWorkspace setHomeLocation(NHomeLocation homeType, String location);

    boolean addPlatform(NPlatformLocation location);

    boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation);

    boolean removePlatform(NPlatformLocation location);

    NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily platformType, String locationName);

    NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily platformType, NPath path);

    NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, String version);

    NOptional<NPlatformLocation> findPlatform(NPlatformLocation location);

    NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion);


    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily);

    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily, NPath path);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param platformType  platform type
     * @param path          platform path
     * @param preferredName preferredName
     * @return null if not a valid jdk path
     */
    NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformType, NPath path, String preferredName);

    NOptional<NPlatformLocation> findPlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter);

    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter);

    NStream<NPlatformLocation> findPlatforms();

    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type);

    NWorkspace addDefaultPlatforms(NPlatformFamily type);

    NWorkspace addDefaultPlatform(NPlatformFamily type);

    /// /

    NWorkspace addImports(String... importExpression);

    NWorkspace clearImports();

    NWorkspace removeImports(String... importExpression);

    NWorkspace updateImports(String[] imports);

    Set<String> getAllImports();

    boolean isImportedGroupId(String groupId);

    /// /////////////////

    NWorkspaceStoredConfig getStoredConfig();

    boolean isReadOnly();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return true if the save action was applied
     */
    boolean saveConfig(boolean force);

    boolean saveConfig();

    NWorkspaceBootConfig loadBootConfig(String path, boolean global, boolean followLinks);

    boolean isSupportedRepositoryType(String repositoryType);

    List<NAddRepositoryOptions> getDefaultRepositories();

    Set<String> getAvailableArchetypes();

    NPath resolveRepositoryPath(String repositoryLocation);

    NIndexStoreFactory getIndexStoreClientFactory();

    String getJavaCommand();

    String getJavaOptions();

    boolean isSystemWorkspace();


    Map<String, String> getConfigMap();

    NOptional<NLiteral> getConfigProperty(String property);

    /**
     * @param property property
     * @param value    value
     * @return {@code this} instance
     */
    NWorkspace setConfigProperty(String property, String value);

    /// ///////////////////////

    /**
     * return registered command factories
     *
     * @return registered command factories
     */
    List<NCommandFactoryConfig> getCommandFactories();

    /**
     * register a new commandFactory. If it already exists, a NutsIllegalArgumentException is thrown
     *
     * @param commandFactory commandFactory
     */
    void addCommandFactory(NCommandFactoryConfig commandFactory);

    /**
     * unregister an existing commandFactory. If it is not found, a NutsIllegalArgumentException is thrown
     *
     * @param commandFactoryId commandFactoryId
     */
    void removeCommandFactory(String commandFactoryId);

    /**
     * unregister an existing commandFactory if it exists.
     *
     * @param commandFactoryId commandFactoryId
     * @return true if removed
     */
    boolean removeCommandFactoryIfExists(String commandFactoryId);

    /**
     * return true if the command is registered or provided by a registered command factory
     *
     * @param command command name
     * @return true if the command is registered or provided by a registered command factory
     */
    boolean commandExists(String command);

    /**
     * return true if the command factory is registered
     *
     * @param command command name
     * @return true if the command factory is registered
     */
    boolean commandFactoryExists(String command);

    /**
     * add command. if the command is already registered (regardless off being defined by command factories) a confirmation is required to update it.
     *
     * @param command command
     * @return true if successfully added
     */
    boolean addCommand(NCommandConfig command);

    /**
     * update command. if the command does not exists (not registered, regardless off being defined by command factories) a NutsIllegalArgumentException is thrown.
     *
     * @param command command
     * @return true if successfully updated
     */
    boolean updateCommand(NCommandConfig command);

    /**
     * remove command. if the command does not exists a NutsIllegalArgumentException is thrown.
     *
     * @param command command name
     */
    void removeCommand(String command);

    /**
     * return true if exists and is removed
     *
     * @param name name
     * @return true if exists and is removed
     */
    boolean removeCommandIfExists(String name);

    /**
     * return the first command for a given name, id and owner.
     * Search is first performed in the registered commands then in each registered command factory.
     *
     * @param name     command name, not null
     * @param forId    if not null, the alias name should resolve to the given id
     * @param forOwner if not null, the alias name should resolve to the owner
     * @return alias definition or null
     */
    NCustomCmd findCommand(String name, NId forId, NId forOwner);

    /**
     * return the first command for a given name, id and owner.
     * Search is first performed in the registered commands then in each registered command factory.
     *
     * @param name command name, not null
     * @return alias definition or null
     */
    NCustomCmd findCommand(String name);

    /**
     * find all registered and factory defined commands
     *
     * @return find all registered and factory defined commands
     */
    List<NCustomCmd> findAllCommands();

    /**
     * find all registered and factory defined commands by owner
     *
     * @param id owner
     * @return all registered and factory defined commands by owner
     */
    List<NCustomCmd> findCommandsByOwner(NId id);

    /// ///////////////////////////////

    /**
     * return true when this is a first boot of the workspace (just installed!)
     *
     * @return true when this is a first boot of the workspace (just installed!)
     */
    boolean isFirstBoot();

    NOptional<NLiteral> getCustomBootOption(String... names);

    NBootOptions getBootOptions();

    ClassLoader getBootClassLoader();

    List<URL> getBootClassWorldURLs();

    String getBootRepositories();

    Instant getCreationStartTime();

    Instant getCreationFinishTime();

    Duration getCreationDuration();

    NClassLoaderNode getBootRuntimeClassLoaderNode();

    List<NClassLoaderNode> getBootExtensionClassLoaderNode();

    NWorkspaceTerminalOptions getBootTerminal();

}
