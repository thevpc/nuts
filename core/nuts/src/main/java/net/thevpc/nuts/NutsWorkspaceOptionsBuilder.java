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

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsLogManager;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Mutable Workspace options
 *
 * @author thevpc
 * @app.category Config
 */
public interface NutsWorkspaceOptionsBuilder extends Serializable, NutsComponent {

    /**
     * create NutsWorkspaceOptionsBuilder instance for the given session (shall not be null).
     *
     * @param session session
     * @return new NutsWorkspaceOptionsBuilder instance
     */
    static NutsWorkspaceOptionsBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsWorkspaceOptionsBuilder.class, true, null);
    }

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
     *
     * @return nuts api version to boot.
     */
    String getApiVersion();

    NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion);

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return application arguments.
     */
    String[] getApplicationArguments();

    NutsWorkspaceOptionsBuilder setApplicationArguments(String[] applicationArguments);

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return workspace archetype to consider when creating a new workspace.
     */
    String getArchetype();

    NutsWorkspaceOptionsBuilder setArchetype(String archetype);

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return class loader supplier.
     */
    Supplier<ClassLoader> getClassLoaderSupplier();

    NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return confirm mode.
     */
    NutsConfirmationMode getConfirm();

//    /**
//     * repository list to be excluded when opening the workspace.
//     * <br>
//     * <strong>option-type :</strong> exported (inherited in child workspaces)
//     * @return repository list to be excluded when opening the workspace.
//     */
//    String[] getExcludedRepositories();

    NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm);

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true no real execution, with dry exec (execute without side effect).
     */
    boolean isDry();

    Boolean getDry();

    NutsWorkspaceOptionsBuilder setDry(Boolean dry);

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return workspace creation evaluated time.
     */
    long getCreationTime();

    NutsWorkspaceOptionsBuilder setCreationTime(long creationTime);

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return extensions to be excluded when opening the workspace.
     */
    String[] getExcludedExtensions();

    NutsWorkspaceOptionsBuilder setExcludedExtensions(String[] excludedExtensions);

    /**
     * execution type.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return execution type.
     */
    NutsExecutionType getExecutionType();

    NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType);

    NutsRunAs getRunAs();

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setRunAs(NutsRunAs runAs);

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return extra executor options.
     */
    String[] getExecutorOptions();

    NutsWorkspaceOptionsBuilder setExecutorOptions(String[] executorOptions);

    /**
     * return home location.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param location location
     * @return home location.
     */
    String getHomeLocation(NutsHomeLocation location);

    /**
     * return home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @return home locations
     */
    Map<NutsHomeLocation, String> getHomeLocations();

    /**
     * set home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setHomeLocations(Map<NutsHomeLocation, String> homeLocations);

    /**
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java command (or java home) used to run workspace.
     */
    String getJavaCommand();

    NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand);

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java options used to run workspace.
     */
    String getJavaOptions();

    NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions);

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return workspace log configuration.
     */
    NutsLogConfig getLogConfig();

    NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig);

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return user friendly workspace name.
     */
    String getName();

    NutsWorkspaceOptionsBuilder setName(String workspaceName);

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return mode used to open workspace.
     */
    NutsOpenMode getOpenMode();

    NutsWorkspaceOptionsBuilder setOpenMode(NutsOpenMode openMode);

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output format type.
     */
    NutsContentType getOutputFormat();

    NutsWorkspaceOptionsBuilder setOutputFormat(NutsContentType outputFormat);

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output formation options.
     */
    String[] getOutputFormatOptions();

    NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options);

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return credential needed to log into workspace.
     */
    char[] getCredentials();

    NutsWorkspaceOptionsBuilder setCredentials(char[] credentials);

    /**
     * repository store location strategy to consider when creating new repositories
     * for a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return repository store location strategy to consider when creating new repositories
     * for a new workspace.
     */
    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy);

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return nuts runtime id (or version) to boot.
     */
    String getRuntimeId();

    NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId);

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
     *
     * @return store location layout to consider when creating a new workspace.
     */
    NutsOsFamily getStoreLocationLayout();

    NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout);

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location strategy for creating a new workspace.
     */
    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy);

    /**
     * store locations map to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store locations map to consider when creating a new workspace.
     */
    Map<NutsStoreLocation, String> getStoreLocations();

    /**
     * set store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setStoreLocations(Map<NutsStoreLocation, String> storeLocations);

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NutsTerminalMode getTerminalMode();

    NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode);

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return repositories to register temporarily when running the workspace.
     */
    String[] getRepositories();

    NutsWorkspaceOptionsBuilder setRepositories(String[] transientRepositories);

    /**
     * username to log into when running workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return username to log into when running workspace.
     */
    String getUserName();

    /**
     * workspace folder location path.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return workspace folder location path.
     */
    String getWorkspace();

    NutsWorkspaceOptionsBuilder setWorkspace(String workspace);

    String getDebug();

    NutsWorkspaceOptionsBuilder setDebug(String debug);

    /**
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider global/system repository
     */
    boolean isGlobal();

    /**
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return true for global/system repository, null for 'not set' or false for 'set to non global'
     */
    Boolean getGlobal();

    /**
     * update 'global' option.
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @param global if true consider global/system repository shared between all users
     * @return if true consider global/system repository
     */
    NutsWorkspaceOptionsBuilder setGlobal(Boolean global);

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider GUI/Swing mode
     */
    boolean isGui();

    Boolean getGui();

    NutsWorkspaceOptionsBuilder setGui(Boolean gui);

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace were invoked from parent process and hence inherits its options.
     */
    boolean isInherited();

    Boolean getInherited();

    NutsWorkspaceOptionsBuilder setInherited(Boolean inherited);

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that it is possible to load external libraries.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, workspace configuration are non modifiable.
     */
    boolean isReadOnly();

    Boolean getReadOnly();

//    /**
//     * boot repositories ';' separated
//     *
//     * <br>
//     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
//     * @return  boot repositories ';' separated
//     */
//    String getBootRepositories();

    NutsWorkspaceOptionsBuilder setReadOnly(Boolean readOnly);

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, boot, cache and temp folder are deleted.
     */
    boolean isRecover();

    Boolean getRecover();

    NutsWorkspaceOptionsBuilder setRecover(Boolean recover);

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */
    boolean isReset();

    Boolean getReset();

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     *
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setReset(Boolean reset);

    boolean isCommandVersion();

    Boolean getCommandVersion();

    NutsWorkspaceOptionsBuilder setCommandVersion(Boolean version);

    boolean isCommandHelp();

    Boolean getCommandHelp();

    NutsWorkspaceOptionsBuilder setCommandHelp(Boolean help);

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, do not install nuts companion tools upon workspace creation.
     */
    boolean isSkipCompanions();

    Boolean getSkipCompanions();

    NutsWorkspaceOptionsBuilder setSkipCompanions(Boolean skipInstallCompanions);

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.5.5
     */
    boolean isSkipWelcome();

    Boolean getSkipWelcome();

//    NutsWorkspaceOptionsBuilder setExcludedRepositories(String[] excludedRepositories);

    NutsWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome);

    /**
     * if not null ant not empty, this prefix will be prefixed to output stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return out line prefix
     * @since 0.8.0
     */
    String getOutLinePrefix();

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setOutLinePrefix(String value);

    /**
     * if not null ant not empty, this prefix will be prefixed to error stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return err line prefix
     * @since 0.8.0
     */
    String getErrLinePrefix();

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setErrLinePrefix(String value);

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    boolean isSkipBoot();

    Boolean getSkipBoot();

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @param skipBoot skipBoot
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    NutsWorkspaceOptionsBuilder setSkipBoot(Boolean skipBoot);

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    boolean isTrace();

    Boolean getTrace();

    NutsWorkspaceOptionsBuilder setTrace(Boolean trace);

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    String getProgressOptions();

    NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions);

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use cache when true
     */
    boolean isCached();

    Boolean getCached();

    NutsWorkspaceOptionsBuilder setCached(Boolean cached);

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use index when true
     */
    boolean isIndexed();

    Boolean getIndexed();

    NutsWorkspaceOptionsBuilder setIndexed(Boolean indexed);

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    boolean isTransitive();

    Boolean getTransitive();

    NutsWorkspaceOptionsBuilder setTransitive(Boolean transitive);

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */
    boolean isBot();

    Boolean getBot();

    NutsWorkspaceOptionsBuilder setBot(Boolean bot);

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    NutsFetchStrategy getFetchStrategy();

    NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy);

    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard input or null
     */
    InputStream getStdin();

    NutsWorkspaceOptionsBuilder setStdin(InputStream stdin);

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard output or null
     */
    PrintStream getStdout();

    NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout);

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    PrintStream getStderr();

    NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr);

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    ExecutorService getExecutorService();

    NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService);

    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    Instant getExpireTime();

    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setExpireTime(Instant value);

    boolean isSkipErrors();

    Boolean getSkipErrors();

    NutsWorkspaceOptionsBuilder setSkipErrors(Boolean value);

    boolean isSwitchWorkspace();

    Boolean getSwitchWorkspace();

    NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean value);

    NutsMessage[] getErrors();

    NutsWorkspaceOptionsBuilder setErrors(NutsMessage[] errors);

    String[] getCustomOptions();

    NutsWorkspaceOptionsBuilder setCustomOptions(String[] properties);

    /**
     * locale
     *
     * @return session locale
     * @since 0.8.1
     */
    String getLocale();

    /**
     * set locale
     *
     * @param locale value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setLocale(String locale);

    /**
     * theme
     *
     * @return session locale
     * @since 0.8.1
     */
    String getTheme();

    /**
     * set theme
     *
     * @param theme value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setTheme(String theme);

    NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other);

    NutsWorkspaceOptionsBuilder parseCommandLine(String commandLine);

    NutsWorkspaceOptionsBuilder parseArguments(String[] args);

    NutsWorkspaceOptionsBuilder setUsername(String username);

    NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setHomeLocation(NutsHomeLocation location, String value);

    NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options);

    NutsWorkspaceOptions build();


    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    String getDependencySolver();

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsWorkspaceOptionsBuilder setDependencySolver(String dependencySolver);

    NutsBootOptions toBootOptions();
}
