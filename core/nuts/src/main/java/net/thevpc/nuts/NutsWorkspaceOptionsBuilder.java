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

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Mutable Workspace options
 * @app.category Config
 */
public interface NutsWorkspaceOptionsBuilder extends Serializable {
    /**
     * create a <strong>mutable</strong> copy of this instance
     *
     * @return a <strong>mutable</strong> copy of this instance
     */
    NutsWorkspaceOptionsBuilder copy();

    /**
     * create a new instance of options formatter that help formatting this instance.
     *
     * @return a new instance of options formatter that help formatting this instance.
     */
    NutsWorkspaceOptionsFormat formatter();

    /**
     * nuts api version to boot.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return nuts api version to boot.
     */
    String getApiVersion();

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return application arguments.
     */
    String[] getApplicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return workspace archetype to consider when creating a new workspace.
     */
    String getArchetype();

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return class loader supplier.
     */
    Supplier<ClassLoader> getClassLoaderSupplier();

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return confirm mode.
     */
    NutsConfirmationMode getConfirm();

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true no real execution, with dry exec (execute without side effect).
     */
    boolean isDry();

    Boolean getDry();

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return workspace creation evaluated time.
     */
    long getCreationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return extensions to be excluded when opening the workspace.
     */
    String[] getExcludedExtensions();

//    /**
//     * repository list to be excluded when opening the workspace.
//     * <br>
//     * <strong>option-type :</strong> exported (inherited in child workspaces)
//     * @return repository list to be excluded when opening the workspace.
//     */
//    String[] getExcludedRepositories();

    /**
     * execution type.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return execution type.
     */
    NutsExecutionType getExecutionType();

    NutsRunAs getRunAs();

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return extra executor options.
     */
    String[] getExecutorOptions();

    /**
     * return home location.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param layout   layout
     * @param location location
     * @return home location.
     */
    String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location);

    /**
     * return home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @return home locations
     */
    Map<String, String> getHomeLocations();

    /**
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return java command (or java home) used to run workspace.
     */
    String getJavaCommand();

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return java options used to run workspace.
     */
    String getJavaOptions();

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return workspace log configuration.
     */
    NutsLogConfig getLogConfig();

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return user friendly workspace name.
     */
    String getName();

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return mode used to open workspace.
     */
    NutsOpenMode getOpenMode();

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return default output format type.
     */
    NutsContentType getOutputFormat();

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return default output formation options.
     */
    String[] getOutputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return credential needed to log into workspace.
     */
    char[] getCredentials();

    /**
     * repository store location strategy to consider when creating new repositories
     * for a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return repository store location strategy to consider when creating new repositories
     * for a new workspace.
     */
    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return nuts runtime id (or version) to boot.
     */
    String getRuntimeId();

    /**
     * store location for the given folder.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param folder folder type
     * @return store location for the given folder.
     */
    String getStoreLocation(NutsStoreLocation folder);


    /**
     * store location layout to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store location layout to consider when creating a new workspace.
     */
    NutsOsFamily getStoreLocationLayout();

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store location strategy for creating a new workspace.
     */
    NutsStoreLocationStrategy getStoreLocationStrategy();

    /**
     * store locations map to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store locations map to consider when creating a new workspace.
     */
    Map<String, String> getStoreLocations();

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NutsTerminalMode getTerminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return repositories to register temporarily when running the workspace.
     */
    String[] getRepositories();

    /**
     * username to log into when running workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return username to log into when running workspace.
     */
    String getUserName();

    /**
     * workspace folder location path.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return workspace folder location path.
     */
    String getWorkspace();

    /**
     * if true, extra debug information is written to standard output.
     * Particularly, exception stack traces are displayed instead of simpler messages.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, extra debug information is written to standard output.
     */
    boolean isDebug();
    Boolean getDebug();

    /**
     * if true consider global/system repository
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true consider global/system repository
     */
    boolean isGlobal();
    Boolean getGlobal();

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true consider GUI/Swing mode
     */
    boolean isGui();
    Boolean getGui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  if true, workspace were invoked from parent process and hence inherits its options.
     */
    boolean isInherited();
    Boolean getInherited();

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that it is possible to load external libraries.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true, workspace configuration are non modifiable.
     */
    boolean isReadOnly();
    Boolean getReadOnly();

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, boot, cache and temp folder are deleted.
     */
    boolean isRecover();
    Boolean getRecover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */
    boolean isReset();
    Boolean getReset();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true, do not install nuts companion tools upon workspace creation.
     */
    boolean isSkipCompanions();
    Boolean getSkipCompanions();

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.5.5
     */
    boolean isSkipWelcome();
    Boolean getSkipWelcome();


    /**
     * if not null ant not empty, this prefix will be prefixed to output stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     * @return out line prefix
     * @since 0.8.0
     */
    String getOutLinePrefix();

    /**
     * if not null ant not empty, this prefix will be prefixed to error stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     * @return err line prefix
     * @since 0.8.0
     */
    String getErrLinePrefix();

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    boolean isSkipBoot();
    Boolean getSkipBoot();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  when true, extra trace user-friendly information is written to standard output.
     */
    boolean isTrace();
    Boolean getTrace();

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  when true, extra trace user-friendly information is written to standard output.
     */
    String getProgressOptions();

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use cache when true
     */
    boolean isCached();
    Boolean getCached();

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use index when true
     */
    boolean isIndexed();
    Boolean getIndexed();

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use transitive repositories when true
     */
    boolean isTransitive();
    Boolean getTransitive();

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */
    boolean isBot();

    Boolean getBot();

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use transitive repositories when true
     */
    NutsFetchStrategy getFetchStrategy();


    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard input or null
     */
    InputStream getStdin();

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard output or null
     */
    PrintStream getStdout();

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard error or null
     */
    PrintStream getStderr();

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  executor service used to create worker threads. when null, use default.
     */
    ExecutorService getExecutorService();

//    /**
//     * boot repositories ';' separated
//     *
//     * <br>
//     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
//     * @return  boot repositories ';' separated
//     */
//    String getBootRepositories();


    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    Instant getExpireTime();

    boolean isSkipErrors();

    Boolean getSkipErrors();

    boolean isSwitchWorkspace();

    Boolean getSwitchWorkspace();

    NutsMessage[] getErrors();

    String[] getProperties();

    /**
     * locale
     * @since 0.8.1
     * @return session locale
     */
    String getLocale();

    /**
     * theme
     * @since 0.8.1
     * @return session locale
     */
    String getTheme();

    NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other);

    NutsWorkspaceOptionsBuilder parseCommandLine(String commandLine);

    NutsWorkspaceOptionsBuilder parseArguments(String[] args);

    NutsWorkspaceOptionsBuilder setWorkspace(String workspace);

    NutsWorkspaceOptionsBuilder setName(String workspaceName);

    NutsWorkspaceOptionsBuilder setGlobal(Boolean global);

    NutsWorkspaceOptionsBuilder setGui(Boolean gui);

    NutsWorkspaceOptionsBuilder setArchetype(String archetype);

    NutsWorkspaceOptionsBuilder setExcludedExtensions(String[] excludedExtensions);

//    NutsWorkspaceOptionsBuilder setExcludedRepositories(String[] excludedRepositories);

    NutsWorkspaceOptionsBuilder setUsername(String username);

    NutsWorkspaceOptionsBuilder setCredentials(char[] credentials);

    NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType);

    NutsWorkspaceOptionsBuilder setInherited(Boolean inherited);

    NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode);

    NutsWorkspaceOptionsBuilder setSkipErrors(Boolean value);

    NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean value);

    NutsWorkspaceOptionsBuilder setErrors(NutsMessage[] errors);

    NutsWorkspaceOptionsBuilder setDry(Boolean dry);

    NutsWorkspaceOptionsBuilder setCreationTime(long creationTime);

    NutsWorkspaceOptionsBuilder setReadOnly(Boolean readOnly);

    NutsWorkspaceOptionsBuilder setTrace(Boolean trace);

    NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions);

    NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId);

    NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);

    NutsWorkspaceOptionsBuilder setApplicationArguments(String[] applicationArguments);

    NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand);

    NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions);

    NutsWorkspaceOptionsBuilder setExecutorOptions(String[] executorOptions);

    NutsWorkspaceOptionsBuilder setRecover(Boolean recover);

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setReset(Boolean reset);

    NutsWorkspaceOptionsBuilder setDebug(Boolean debug);

    NutsWorkspaceOptionsBuilder setRepositories(String[] transientRepositories);

    NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig);

    NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion);

    NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout);

    NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy);

    NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy);

    NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setSkipCompanions(Boolean skipInstallCompanions);

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @param skipBoot skipBoot
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    NutsWorkspaceOptionsBuilder setSkipBoot(Boolean skipBoot);

    NutsWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome);

    NutsWorkspaceOptionsBuilder setOpenMode(NutsOpenMode openMode);

    NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm);

    NutsWorkspaceOptionsBuilder setOutputFormat(NutsContentType outputFormat);

    NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options);

    NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options);

    NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy);

    NutsWorkspaceOptionsBuilder setCached(Boolean cached);

    NutsWorkspaceOptionsBuilder setIndexed(Boolean indexed);

    NutsWorkspaceOptionsBuilder setTransitive(Boolean transitive);
    
    NutsWorkspaceOptionsBuilder setBot(Boolean bot);

    NutsWorkspaceOptionsBuilder setStdin(InputStream stdin);

    NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout);

    NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr);

    NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService);

//    NutsWorkspaceOptionsBuilder setBootRepositories(String bootRepositories);

    /**
     * set home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setHomeLocations(Map<String, String> homeLocations);

    /**
     * set store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setStoreLocations(Map<String, String> storeLocations);


    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setExpireTime(Instant value);


    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setOutLinePrefix(String value);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setErrLinePrefix(String value);

    NutsWorkspaceOptionsBuilder setProperties(String[] properties);

    /**
     * set locale
     *
     * @param locale value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setLocale(String locale);

    /**
     * set theme
     *
     * @param theme value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setTheme(String theme);

    /**
     * set runAs mode
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setRunAs(NutsRunAs runAs);

    NutsWorkspaceOptions build();
}
