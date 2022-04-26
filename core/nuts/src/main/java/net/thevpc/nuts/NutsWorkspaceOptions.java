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

import net.thevpc.nuts.boot.PrivateWorkspaceOptionsConfigHelper;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace options class that holds command argument information.
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NutsWorkspaceOptions extends Serializable {
    /**
     * nuts api version to boot.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return nuts api version to boot.
     */
    String getApiVersion();

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return application arguments.
     */
    List<String> getApplicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return workspace archetype to consider when creating a new workspace.
     */
    String getArchetype();

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return class loader supplier.
     */
    Supplier<ClassLoader> getClassLoaderSupplier();

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return confirm mode.
     */
    NutsConfirmationMode getConfirm();

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true no real execution, with dry exec (execute without side effect).
     */
    boolean isDry();

    Boolean getDry();

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return workspace creation evaluated time.
     */
    long getCreationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return extensions to be excluded when opening the workspace.
     */
    List<String> getExcludedExtensions();

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
     *
     * @return execution type.
     */
    NutsExecutionType getExecutionType();

    NutsRunAs getRunAs();

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return extra executor options.
     */
    List<String> getExecutorOptions();

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
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java command (or java home) used to run workspace.
     */
    String getJavaCommand();

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java options used to run workspace.
     */
    String getJavaOptions();

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return workspace log configuration.
     */
    NutsLogConfig getLogConfig();

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return user friendly workspace name.
     */
    String getName();

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return mode used to open workspace.
     */
    NutsOpenMode getOpenMode();

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output format type.
     */
    NutsContentType getOutputFormat();

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output formation options.
     */
    List<String> getOutputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return credential needed to log into workspace.
     */
    char[] getCredentials();

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

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
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
     *
     * @return store location layout to consider when creating a new workspace.
     */
    NutsOsFamily getStoreLocationLayout();

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location strategy for creating a new workspace.
     */
    NutsStoreLocationStrategy getStoreLocationStrategy();

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
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NutsTerminalMode getTerminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return repositories to register temporarily when running the workspace.
     */
    List<String> getRepositories();

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

    String getDebug();

    /**
     * if true consider global/system repository
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider global/system repository
     */
    boolean isGlobal();

    Boolean getGlobal();

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider GUI/Swing mode
     */
    boolean isGui();

    Boolean getGui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace were invoked from parent process and hence inherits its options.
     */
    boolean isInherited();

    Boolean getInherited();

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

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, boot, cache and temp folder are deleted.
     */
    boolean isRecover();

    Boolean getRecover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */
    boolean isReset();

    Boolean getReset();

    boolean isCommandVersion();

    Boolean getCommandVersion();

    boolean isCommandHelp();

    Boolean getCommandHelp();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
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
     *
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
     *
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
     *
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
     *
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    boolean isSkipBoot();

    Boolean getSkipBoot();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    boolean isTrace();

    Boolean getTrace();

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

    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    String getDependencySolver();

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use cache when true
     */
    boolean isCached();

    Boolean getCached();

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use index when true
     */
    boolean isIndexed();

    Boolean getIndexed();

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    boolean isTransitive();

    Boolean getTransitive();

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */
    boolean isBot();

    Boolean getBot();

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    NutsFetchStrategy getFetchStrategy();


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

    List<NutsMessage> getErrors();

    List<String> getCustomOptions();

    /**
     * locale
     *
     * @return session locale
     * @since 0.8.1
     */
    String getLocale();

    /**
     * theme
     *
     * @return session locale
     * @since 0.8.1
     */
    String getTheme();

    NutsWorkspaceOptionsBuilder builder();
    NutsWorkspaceOptions readOnly();

    NutsCommandLine toCommandLine();
    NutsCommandLine toCommandLine(NutsWorkspaceOptionsConfig config);


}
