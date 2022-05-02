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

import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.util.NutsLogConfig;

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

    NutsOptional<NutsSupportMode> getDesktopLauncher();

    NutsOptional<NutsSupportMode> getMenuLauncher();

    NutsOptional<NutsSupportMode> getUserLauncher();

    NutsOptional<NutsWorkspaceIsolation> getIsolation();

    /**
     * init launcher
     * @since 0.8.4
     * @return init launcher
     */
    NutsOptional<Boolean> getInitLaunchers();

    /**
     * init scripts
     * @since 0.8.4
     * @return init scripts
     */
    NutsOptional<Boolean> getInitScripts();

    /**
     * init platforms
     * @since 0.8.4
     * @return init platforms
     */
    NutsOptional<Boolean> getInitPlatforms();

    /**
     * init java
     * @since 0.8.4
     * @return init java
     */
    NutsOptional<Boolean> getInitJava();

    /**
     * nuts api version to boot.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return nuts api version to boot.
     */
    NutsOptional<NutsVersion> getApiVersion();

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return application arguments.
     */
    NutsOptional<List<String>> getApplicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return workspace archetype to consider when creating a new workspace.
     */
    NutsOptional<String> getArchetype();

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return class loader supplier.
     */
    NutsOptional<Supplier<ClassLoader>> getClassLoaderSupplier();

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return confirm mode.
     */
    NutsOptional<NutsConfirmationMode> getConfirm();

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true no real execution, with dry exec (execute without side effect).
     */

    NutsOptional<Boolean> getDry();

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return workspace creation evaluated time.
     */
    NutsOptional<Instant> getCreationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return extensions to be excluded when opening the workspace.
     */
    NutsOptional<List<String>> getExcludedExtensions();

    /**
     * execution type.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return execution type.
     */
    NutsOptional<NutsExecutionType> getExecutionType();

    NutsOptional<NutsRunAs> getRunAs();

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return extra executor options.
     */
    NutsOptional<List<String>> getExecutorOptions();

    /**
     * return home location.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param location location
     * @return home location.
     */
    NutsOptional<String> getHomeLocation(NutsHomeLocation location);

    /**
     * return home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @return home locations
     */
    NutsOptional<Map<NutsHomeLocation, String>> getHomeLocations();

    /**
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java command (or java home) used to run workspace.
     */
    NutsOptional<String> getJavaCommand();

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java options used to run workspace.
     */
    NutsOptional<String> getJavaOptions();

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return workspace log configuration.
     */
    NutsOptional<NutsLogConfig> getLogConfig();

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return user friendly workspace name.
     */
    NutsOptional<String> getName();

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return mode used to open workspace.
     */
    NutsOptional<NutsOpenMode> getOpenMode();

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output format type.
     */
    NutsOptional<NutsContentType> getOutputFormat();

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output formation options.
     */
    NutsOptional<List<String>> getOutputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return credential needed to log into workspace.
     */
    NutsOptional<char[]> getCredentials();

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
    NutsOptional<NutsStoreLocationStrategy> getRepositoryStoreLocationStrategy();

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return nuts runtime id (or version) to boot.
     */
    NutsOptional<NutsId> getRuntimeId();

    /**
     * store location for the given folder.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param folder folder type
     * @return store location for the given folder.
     */
    NutsOptional<String> getStoreLocation(NutsStoreLocation folder);


    /**
     * store location layout to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location layout to consider when creating a new workspace.
     */
    NutsOptional<NutsOsFamily> getStoreLocationLayout();

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location strategy for creating a new workspace.
     */
    NutsOptional<NutsStoreLocationStrategy> getStoreLocationStrategy();

    /**
     * store locations map to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store locations map to consider when creating a new workspace.
     */
    NutsOptional<Map<NutsStoreLocation, String>> getStoreLocations();

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NutsOptional<NutsTerminalMode> getTerminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return repositories to register temporarily when running the workspace.
     */
    NutsOptional<List<String>> getRepositories();

    /**
     * username to log into when running workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return username to log into when running workspace.
     */
    NutsOptional<String> getUserName();

    /**
     * workspace folder location path.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return workspace folder location path.
     */
    NutsOptional<String> getWorkspace();

    NutsOptional<String> getDebug();

    /**
     * if true consider global/system repository
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider global/system repository
     */

    NutsOptional<Boolean> getGlobal();

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider GUI/Swing mode
     */

    NutsOptional<Boolean> getGui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace were invoked from parent process and hence inherits its options.
     */

    NutsOptional<Boolean> getInherited();

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that it is possible to load external libraries.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, workspace configuration are non modifiable.
     */

    NutsOptional<Boolean> getReadOnly();

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, boot, cache and temp folder are deleted.
     */

    NutsOptional<Boolean> getRecover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */

    NutsOptional<Boolean> getReset();


    NutsOptional<Boolean> getCommandVersion();


    NutsOptional<Boolean> getCommandHelp();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, do not install nuts companion tools upon workspace creation.
     */

    NutsOptional<Boolean> getSkipCompanions();

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

    NutsOptional<Boolean> getSkipWelcome();


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
    NutsOptional<String> getOutLinePrefix();

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
    NutsOptional<String> getErrLinePrefix();

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

    NutsOptional<Boolean> getSkipBoot();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */

    NutsOptional<Boolean> getTrace();

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    NutsOptional<String> getProgressOptions();

    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    NutsOptional<String> getDependencySolver();

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use cache when true
     */

    NutsOptional<Boolean> getCached();

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use index when true
     */
    NutsOptional<Boolean> getIndexed();

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */

    NutsOptional<Boolean> getTransitive();

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */

    NutsOptional<Boolean> getBot();

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    NutsOptional<NutsFetchStrategy> getFetchStrategy();


    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard input or null
     */
    NutsOptional<InputStream> getStdin();

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard output or null
     */
    NutsOptional<PrintStream> getStdout();

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NutsOptional<PrintStream> getStderr();

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NutsOptional<ExecutorService> getExecutorService();

    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    NutsOptional<Instant> getExpireTime();

    NutsOptional<Boolean> getSkipErrors();

    NutsOptional<Boolean> getSwitchWorkspace();

    NutsOptional<List<NutsMessage>> getErrors();

    NutsOptional<List<String>> getCustomOptions();

    /**
     * locale
     *
     * @return session locale
     * @since 0.8.1
     */
    NutsOptional<String> getLocale();

    /**
     * theme
     *
     * @return session locale
     * @since 0.8.1
     */
    NutsOptional<String> getTheme();

    NutsWorkspaceOptionsBuilder builder();
    NutsWorkspaceOptions readOnly();

    NutsCommandLine toCommandLine();
    NutsCommandLine toCommandLine(NutsWorkspaceOptionsConfig config);


}
