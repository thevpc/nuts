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

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace options class that holds command argument information.
 *
 * @since 0.5.4
 */
public interface NutsWorkspaceOptions extends Serializable {

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
    NutsWorkspaceOptionsFormat format();

    /**
     * nuts api version to boot.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return nuts api version to boot.
     */
    String getApiVersion();

    /**
     * application arguments.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return application arguments.
     */
    String[] getApplicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return orkspace archetype to consider when creating a new workspace.
     */
    String getArchetype();

    /**
     * class loader supplier.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return class loader supplier.
     */
    Supplier<ClassLoader> getClassLoaderSupplier();

    /**
     * confirm mode.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return confirm mode.
     */
    NutsConfirmationMode getConfirm();

    /**
     * if true no real execution, wil dry exec (execute without side effect).
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true no real execution, wil dry exec (execute without side effect).
     */
    boolean isDry();

    /**
     * workspace creation evaluated time.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return workspace creation evaluated time.
     */
    long getCreationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return extensions to be excluded when opening the workspace.
     */
    String[] getExcludedExtensions();

    /**
     * repository list to be excluded when opening the workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return repository list to be excluded when opening the workspace.
     */
    String[] getExcludedRepositories();

    /**
     * execution type.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return execution type.
     */
    NutsExecutionType getExecutionType();

    /**
     * extra executor options.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return extra executor options.
     */
    String[] getExecutorOptions();

    /**
     * return home location.
     * <p>
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
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @return home locations
     */
    Map<String, String> getHomeLocations();

    /**
     * java command (or java home) used to run workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return java command (or java home) used to run workspace.
     */
    String getJavaCommand();

    /**
     * java options used to run workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return java options used to run workspace.
     */
    String getJavaOptions();

    /**
     * workspace log configuration.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return workspace log configuration.
     */
    NutsLogConfig getLogConfig();

    /**
     * user friendly workspace name.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return user friendly workspace name.
     */
    String getName();

    /**
     * mode used to open workspace.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return mode used to open workspace.
     */
    NutsWorkspaceOpenMode getOpenMode();

    /**
     * default output format type.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return default output format type.
     */
    NutsOutputFormat getOutputFormat();

    /**
     * default output formation options.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return default output formation options.
     */
    String[] getOutputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return credential needed to log into workspace.
     */
    char[] getCredentials();

    /**
     * repository store location strategy to consider when creating new repositories
     * for a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return repository store location strategy to consider when creating new repositories
     * for a new workspace.
     */
    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    /**
     * nuts runtime id (or version) to boot.
     *
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return nuts runtime id (or version) to boot.
     */
    String getRuntimeId();

    /**
     * store location for the given folder.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param folder folder type
     * @return store location for the given folder.
     */
    String getStoreLocation(NutsStoreLocation folder);


    /**
     * store location layout to consider when creating a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store location layout to consider when creating a new workspace.
     */
    NutsOsFamily getStoreLocationLayout();

    /**
     * store location strategy for creating a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store location strategy for creating a new workspace.
     */
    NutsStoreLocationStrategy getStoreLocationStrategy();

    /**
     * store locations map to consider when creating a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @return store locations map to consider when creating a new workspace.
     */
    Map<String, String> getStoreLocations();

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NutsTerminalMode getTerminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return repositories to register temporarily when running the workspace.
     */
    String[] getTransientRepositories();

    /**
     * username to log into when running workspace.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return username to log into when running workspace.
     */
    String getUserName();

    /**
     * workspace folder location path.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     * @return workspace folder location path.
     */
    String getWorkspace();

    /**
     * if true, extra debug information is written to standard output.
     * Particularly, exception stack traces are displayed instead of simpler messages.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, extra debug information is written to standard output.
     */
    boolean isDebug();

    /**
     * if true consider global/system repository
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true consider global/system repository
     */
    boolean isGlobal();

    /**
     * if true consider GUI/Swing mode
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true consider GUI/Swing mode
     */
    boolean isGui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  if true, workspace were invoked from parent process and hence inherits its options.
     */
    boolean isInherited();

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that it is possible to load external libraries.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true, workspace configuration are non modifiable.
     */
    boolean isReadOnly();

    /**
     * if true, boot, cache and temp folder are deleted.
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, boot, cache and temp folder are deleted.
     */
    boolean isRecover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */
    boolean isReset();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return if true, do not install nuts companion tools upon workspace creation.
     */
    boolean isSkipCompanions();

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * <p>
     * defaults to false.
     * <p>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.5.5
     */
    boolean isSkipWelcome();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  when true, extra trace user-friendly information is written to standard output.
     */
    boolean isTrace();

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  when true, extra trace user-friendly information is written to standard output.
     */
    String getProgressOptions();

    /**
     * when true, use cache
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use cache when true
     */
    boolean isCached();

    /**
     * when true, use index
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use index when true
     */
    boolean isIndexed();

    /**
     * when true, use transitive repositories
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use transitive repositories when true
     */
    boolean isTransitive();

    /**
     * default fetch strategy
     * <p>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     * @return  use transitive repositories when true
     */
    NutsFetchStrategy getFetchStrategy();


    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard input or null
     */
    InputStream getStdin();

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard output or null
     */
    PrintStream getStdout();

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  default standard error or null
     */
    PrintStream getStderr();

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  executor service used to create worker threads. when null, use default.
     */
    ExecutorService getExecutorService();

    /**
     * boot repositories ';' separated
     *
     * <p>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     * @return  boot repositories ';' separated
     */
    String getBootRepositories();
}
