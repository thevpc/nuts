/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.core;

import net.thevpc.nuts.app.NApplicationHandleMode;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NWorkspaceTerminalOptions;
import net.thevpc.nuts.boot.core.NWorkspaceBase;
import net.thevpc.nuts.command.NCommandConfig;
import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.command.NCustomCmd;
import net.thevpc.nuts.command.NInstallListener;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.internal.NScopedWorkspace;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.collections.NObservableMapListener;

import java.io.Closeable;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by vpc on 1/5/17.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NWorkspace extends NWorkspaceBase, NComponent, Closeable {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NWorkspace of() {
        /**
         * Returns the get.
         *
         * @param ).get( ).get(
         * @return get result
         */
        return get().get();
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    static NOptional<NWorkspace> get() {
        return NScopedWorkspace.currentWorkspace();
    }

    /**
     * Run.
     *
     * @param runnable runnable
     */
    static void run(Runnable runnable) {
        NScopedWorkspace.runWith(runnable);
    }

    /**
     * Call.
     *
     * @param callable callable
     * @return call result
     */
    static <T> T call(NCallable<T> callable) {
        return NScopedWorkspace.callWith(callable);
    }

    /**
     * Resolve effective descriptor.
     *
     * @param descriptor descriptor
     * @param effectiveNDescriptorConfig effective n descriptor config
     * @return resolve effective descriptor result
     */
    NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NDescriptorEffectiveConfig effectiveNDescriptorConfig);

    /**
     * Resolve effective descriptor.
     *
     * @param descriptor descriptor
     * @return resolve effective descriptor result
     */
    NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor);

    /**
     * Sets the shared instance.
     *
     * @return set shared instance result
     */
    NWorkspace setSharedInstance();

    /**
     * Share.
     *
     * @return share result
     */
    NWorkspace share();

    /**
     * Checks if is shared instance.
     *
     * @return is shared instance result
     */
    boolean isSharedInstance();

    /**
     * Run with.
     *
     * @param runnable runnable
     */
    void runWith(Runnable runnable);

    /**
     * Call with.
     *
     * @param callable callable
     * @return call with result
     */
    <T> T callWith(NCallable<T> callable);

    /**
     * Workspace identifier, most likely to be unique cross machines
     *
     * @return uuid
     */
    String uuid();

    /**
     * Workspace name
     *
     * @return name
     */
    String name();

    /**
     * Digest name.
     *
     * @return digest name result
     */
    String digestName();

    /**
     * Api version.
     *
     * @return api version result
     */
    NVersion apiVersion();

    /**
     * Boot version.
     *
     * @return boot version result
     */
    NVersion bootVersion();

    /**
     * Api id.
     *
     * @return api id result
     */
    NId apiId();

    /**
     * App id.
     *
     * @return app id result
     */
    NId appId();

    /**
     * Runtime id.
     *
     * @return runtime id result
     */
    NId runtimeId();


    /**
     * Location.
     *
     * @return location result
     */
    NPath location();

    /// ////////////////// create new session
    /**
     * Creates a new instance of create session.
     *
     * @return create session result
     */
    NSession createSession();

    /**
     * Current session.
     *
     * @return current session result
     */
    NSession currentSession();

    /**
     * Extensions.
     *
     * @return extensions result
     */
    NExtensions extensions();

    /**
     * Close.
     */
    void close();

    /// ////////////////////


    /**
     * Removes the specified repository listener.
     *
     * @param listener listener
     * @return remove repository listener result
     */
    NWorkspace removeRepositoryListener(NRepositoryListener listener);

    /**
     * Adds the specified repository listener.
     *
     * @param listener listener
     * @return add repository listener result
     */
    NWorkspace addRepositoryListener(NRepositoryListener listener);

    /**
     * Repository listeners.
     *
     * @return repository listeners result
     */
    List<NRepositoryListener> repositoryListeners();

    /**
     * Removes the specified workspace listener.
     *
     * @param listener listener
     * @return remove workspace listener result
     */
    NWorkspace removeWorkspaceListener(NWorkspaceListener listener);

    /**
     * Adds the specified workspace listener.
     *
     * @param listener listener
     * @return add workspace listener result
     */
    NWorkspace addWorkspaceListener(NWorkspaceListener listener);

    /**
     * Workspace listeners.
     *
     * @return workspace listeners result
     */
    List<NWorkspaceListener> workspaceListeners();

    /**
     * Removes the specified install listener.
     *
     * @param listener listener
     * @return remove install listener result
     */
    NWorkspace removeInstallListener(NInstallListener listener);

    /**
     * Adds the specified install listener.
     *
     * @param listener listener
     * @return add install listener result
     */
    NWorkspace addInstallListener(NInstallListener listener);

    /**
     * Install listeners.
     *
     * @return install listeners result
     */
    List<NInstallListener> installListeners();

    /// ///////////////////////////////

    /**
     * Adds the specified repository.
     *
     * @param options options
     * @return add repository result
     */
    NRepository addRepository(NRepositorySpec options);

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

    /**
     * Finds the find repository by id.
     *
     * @param repositoryIdOrName repository id or name
     * @return find repository by id result
     */
    NOptional<NRepository> findRepositoryById(String repositoryIdOrName);

    /**
     * Finds the find repository by name.
     *
     * @param repositoryIdOrName repository id or name
     * @return find repository by name result
     */
    NOptional<NRepository> findRepositoryByName(String repositoryIdOrName);

    /**
     * @param repositoryIdOrName repository id or name
     * @return null if not found
     */
    NOptional<NRepository> getRepository(String repositoryIdOrName);

    /**
     * Removes the specified repository.
     *
     * @param locationOrRepositoryId location or repository id
     * @return remove repository result
     */
    NWorkspace removeRepository(String locationOrRepositoryId);

    /**
     * Repositories.
     *
     * @return repositories result
     */
    List<NRepository> repositories();

    /**
     * Removes the specified all repositories.
     *
     * @return remove all repositories result
     */
    NWorkspace removeAllRepositories();

    /// /////////

    /**
     * Adds the specified property listener.
     *
     * @param listener listener
     * @return add property listener result
     */
    NWorkspace addPropertyListener(NObservableMapListener<String, Object> listener);

    /**
     * Removes the specified property listener.
     *
     * @param listener listener
     * @return remove property listener result
     */
    NWorkspace removePropertyListener(NObservableMapListener<String, Object> listener);

    /**
     * Property listeners.
     *
     * @return property listeners result
     */
    List<NObservableMapListener<String, Object>> propertyListeners();


    /**
     * Returns a map of all properties currently defined in this workspace.
     * <p>
     * The returned map is a snapshot of the workspace properties and may be
     * unmodifiable depending on implementation. Use {@link #getProperty(String)}
     * or {@link #setProperty(String, Object)} to interact with individual properties.
     *
     * @return a map of property names to values
     * @since 0.8.1
     */
    Map<String, Object> properties();

    /**
     * Retrieves the raw value of a property by name from the workspace.
     * <p>
     * The returned value is not type-checked; use {@link #getProperty(Class)} for
     * typed access.
     *
     * @param property the name of the property
     * @return the property value wrapped in {@link NOptional}, empty if not present
     * @since 0.8.1
     */
    NOptional<Object> getProperty(String property);

    /**
     * Retrieves the value of a property using a class as key.
     * <p>
     * The property key is the fully qualified class name of {@code propertyTypeAndName}.
     *
     * @param propertyTypeAndName the class used as property key
     * @param <T>                 the type of the property
     * @return the property value wrapped in {@link NOptional}, empty if not present
     * @since 0.8.9
     */
    <T> NOptional<T> getProperty(Class<T> propertyTypeAndName);

    /**
     * Sets a property value in this workspace.
     * <p>
     * If the property already exists, its value is replaced. This method returns
     * the workspace itself to allow fluent chaining.
     *
     * @param property the property name
     * @param value    the property value
     * @return this {@code NWorkspace} instance
     * @since 0.8.1
     */
    NWorkspace setProperty(String property, Object value);

    /**
     * Retrieves an existing property by class key, or computes it using the provided supplier if absent.
     * <p>
     * The property is added to the workspace if computed. The key is the fully qualified
     * name of {@code property}.
     *
     * @param property the class used as property key
     * @param supplier the function to compute the property if absent
     * @param <T>      the type of the property
     * @return the existing or newly computed property
     * @throws NullPointerException if {@code supplier} is null
     * @since 0.8.9
     */
    <T> T getOrComputeProperty(Class<T> property, Supplier<T> supplier);


    /**
     * Retrieves an existing property by name, or computes it using the provided supplier if absent.
     * <p>
     * The property is added to the workspace if computed.
     *
     * @param property the property name
     * @param supplier the function to compute the property if absent
     * @param <T>      the type of the property
     * @return the existing or newly computed property
     * @throws NullPointerException if {@code supplier} is null
     * @since 0.8.1
     */
    <T> T getOrComputeProperty(String property, Supplier<T> supplier);


    /**
     * Adds the specified launcher.
     *
     * @param launcher launcher
     */
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

    /**
     * Returns the home location.
     *
     * @param folderType folder type
     * @return get home location result
     */
    NPath getHomeLocation(NStoreType folderType);

    /**
     * Store strategy.
     *
     * @return store strategy result
     */
    NStoreStrategy storeStrategy();

    /**
     * Store strategy.
     *
     * @param strategy strategy
     * @return store strategy result
     */
    NWorkspace storeStrategy(NStoreStrategy strategy);

    /**
     * Repository store strategy.
     *
     * @return repository store strategy result
     */
    NStoreStrategy repositoryStoreStrategy();

    /**
     * Store layout.
     *
     * @return store layout result
     */
    NOsFamily storeLayout();

    /**
     * Store layout.
     *
     * @param storeLayout store layout
     * @return store layout result
     */
    NWorkspace storeLayout(NOsFamily storeLayout);

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NStoreType, String> storeLocations();

    /**
     * Returns the default id filename.
     *
     * @param id id
     * @return get default id filename result
     */
    String getDefaultIdFilename(NId id);

    /**
     * Returns the default id basedir.
     *
     * @param id id
     * @return get default id basedir result
     */
    NPath getDefaultIdBasedir(NId id);

    /**
     * Returns the default id content extension.
     *
     * @param packaging packaging
     * @return get default id content extension result
     */
    String getDefaultIdContentExtension(String packaging);

    /**
     * Returns the default id extension.
     *
     * @param id id
     * @return get default id extension result
     */
    String getDefaultIdExtension(NId id);

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NHomeLocation, String> homeLocations();

    /**
     * Returns the home location.
     *
     * @param location location
     * @return get home location result
     */
    NPath getHomeLocation(NHomeLocation location);

    /**
     * Workspace location.
     *
     * @return workspace location result
     */
    NPath workspaceLocation();

    /**
     * Sets the store location.
     *
     * @param folderType folder type
     * @param location location
     * @return set store location result
     */
    NWorkspace setStoreLocation(NStoreType folderType, String location);

    /**
     * Sets the home location.
     *
     * @param homeType home type
     * @param location location
     * @return set home location result
     */
    NWorkspace setHomeLocation(NHomeLocation homeType, String location);

    /**
     * Finds the find sys command.
     *
     * @param name name
     * @return find sys command result
     */
    NOptional<String> findSysCommand(String name);

    /**
     * Adds the specified imports.
     *
     * @param importExpression import expression
     * @return add imports result
     */
    NWorkspace addImports(String... importExpression);

    /**
     * Clear imports.
     *
     * @return clear imports result
     */
    NWorkspace clearImports();

    /**
     * Removes the specified imports.
     *
     * @param importExpression import expression
     * @return remove imports result
     */
    NWorkspace removeImports(String... importExpression);

    /**
     * Update imports.
     *
     * @param imports imports
     * @return update imports result
     */
    NWorkspace updateImports(String[] imports);

    /**
     * All imports.
     *
     * @return all imports result
     */
    Set<String> allImports();

    /**
     * Checks if is imported group id.
     *
     * @param groupId group id
     * @return is imported group id result
     */
    boolean isImportedGroupId(String groupId);

    /// /////////////////

    /**
     * Stored config.
     *
     * @return stored config result
     */
    NWorkspaceStoredConfig storedConfig();

    /**
     * Checks if is read only.
     *
     * @return is read only result
     */
    boolean isReadOnly();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return true if the save action was applied
     */
    boolean saveConfig(boolean force);

    /**
     * Save config.
     *
     * @return save config result
     */
    boolean saveConfig();

    /**
     * Load boot config.
     *
     * @param path path
     * @param global global
     * @param followLinks follow links
     * @return load boot config result
     */
    NWorkspaceBootConfig loadBootConfig(String path, boolean global, boolean followLinks);

    /**
     * Checks if is supported repository type.
     *
     * @param repositoryType repository type
     * @return is supported repository type result
     */
    boolean isSupportedRepositoryType(String repositoryType);

    /**
     * Default repositories.
     *
     * @return default repositories result
     */
    List<NRepositorySpec> defaultRepositories();

    /**
     * Available archetypes.
     *
     * @return available archetypes result
     */
    Set<String> availableArchetypes();

    /**
     * Resolve repository path.
     *
     * @param repositoryLocation repository location
     * @return resolve repository path result
     */
    NPath resolveRepositoryPath(String repositoryLocation);

    /**
     * Index store client factory.
     *
     * @return index store client factory result
     */
    NIndexStoreFactory indexStoreClientFactory();

    /**
     * Java command.
     *
     * @return java command result
     */
    String javaCommand();

    /**
     * Java options.
     *
     * @return java options result
     */
    String javaOptions();

    /**
     * Checks if is system workspace.
     *
     * @return is system workspace result
     */
    boolean isSystemWorkspace();


    /**
     * Config map.
     *
     * @return config map result
     */
    Map<String, String> configMap();

    /**
     * Returns the config property.
     *
     * @param property property
     * @return get config property result
     */
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
    List<NCommandFactoryConfig> commandFactories();

    /**
     * register a new commandFactory. If it already exists, a NutsIllegalArgumentException is thrown
     *
     * @param commandFactory commandFactory
     */
    void addCommandFactory(NCommandFactoryConfig commandFactory);

    /**
     * unregister an existing commandFactory. If It's not found, a NutsIllegalArgumentException is thrown
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
     * update command. if the command does not exist (not registered, regardless off being defined by command factories) a NutsIllegalArgumentException is thrown.
     *
     * @param command command
     * @return true if successfully updated
     */
    boolean updateCommand(NCommandConfig command);

    /**
     * remove command. if the command does not exist a NutsIllegalArgumentException is thrown.
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

    /**
     * Returns the custom boot option.
     *
     * @param names names
     * @return get custom boot option result
     */
    NOptional<NLiteral> getCustomBootOption(String... names);

    /**
     * Boot options.
     *
     * @return boot options result
     */
    NBootOptions bootOptions();

    /**
     * Boot class loader.
     *
     * @return boot class loader result
     */
    ClassLoader bootClassLoader();

    /**
     * Boot class world ur ls.
     *
     * @return boot class world ur ls result
     */
    List<URL> bootClassWorldURLs();

    /**
     * Boot repositories.
     *
     * @return boot repositories result
     */
    List<String> bootRepositories();

    /**
     * Creation start time.
     *
     * @return creation start time result
     */
    Instant creationStartTime();

    /**
     * Creation finish time.
     *
     * @return creation finish time result
     */
    Instant creationFinishTime();

    /**
     * Creation duration.
     *
     * @return creation duration result
     */
    NDuration creationDuration();

    /**
     * Boot runtime class loader node.
     *
     * @return boot runtime class loader node result
     */
    NClassLoaderNode bootRuntimeClassLoaderNode();

    /**
     * Boot terminal.
     *
     * @return boot terminal result
     */
    NWorkspaceTerminalOptions bootTerminal();

    /**
     * Run application.
     *
     * @param handleMode handle mode
     */
    void runApplication(NApplicationHandleMode handleMode);
}
