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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Mutable Workspace options
 */
public interface NutsWorkspaceOptionsBuilder extends NutsWorkspaceOptions{
    NutsWorkspaceOptionsBuilder setWorkspace(String workspace);

    NutsWorkspaceOptionsBuilder setName(String workspaceName);

    NutsWorkspaceOptionsBuilder setGlobal(boolean global);

    NutsWorkspaceOptionsBuilder setGui(boolean gui);

    NutsWorkspaceOptionsBuilder setArchetype(String archetype);

    NutsWorkspaceOptionsBuilder setExcludedExtensions(String[] excludedExtensions);

    NutsWorkspaceOptionsBuilder setExcludedRepositories(String[] excludedRepositories);

    NutsWorkspaceOptionsBuilder setUsername(String username);

    NutsWorkspaceOptionsBuilder setCredentials(char[] credentials);

    NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType);

    NutsWorkspaceOptionsBuilder setInherited(boolean inherited);

    NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode);

    NutsWorkspaceOptionsBuilder setDry(boolean dry);

    NutsWorkspaceOptionsBuilder setCreationTime(long creationTime);

    NutsWorkspaceOptionsBuilder setReadOnly(boolean readOnly);

    NutsWorkspaceOptionsBuilder setTrace(boolean trace);

    NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions);

    NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId);

    NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);

    NutsWorkspaceOptionsBuilder setApplicationArguments(String[] applicationArguments);

    NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand);

    NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions);

    NutsWorkspaceOptionsBuilder setExecutorOptions(String[] executorOptions);

    NutsWorkspaceOptionsBuilder setRecover(boolean recover);

    NutsWorkspaceOptionsBuilder setReset(boolean reset);

    NutsWorkspaceOptionsBuilder setDebug(boolean debug);

    NutsWorkspaceOptionsBuilder setTransientRepositories(String[] transientRepositories);

    NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig);

    NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion);

    NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout);

    NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy);

    NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy);

    NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setSkipCompanions(boolean skipInstallCompanions);

    NutsWorkspaceOptionsBuilder setSkipWelcome(boolean skipWelcome);

    NutsWorkspaceOptionsBuilder setOpenMode(NutsWorkspaceOpenMode openMode);

    NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm);

    NutsWorkspaceOptionsBuilder setOutputFormat(NutsOutputFormat outputFormat);

    NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options);

    NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options);

    NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy);

    NutsWorkspaceOptionsBuilder setCached(boolean cached);

    NutsWorkspaceOptionsBuilder setIndexed(boolean indexed);

    NutsWorkspaceOptionsBuilder setTransitive(boolean transitive);

    NutsWorkspaceOptionsBuilder setStdin(InputStream stdin);

    NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout);

    NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr);

    NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService);

    NutsWorkspaceOptionsBuilder setBootRepositories(String bootRepositories);

    /**
     * set home locations.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setHomeLocations(Map<String, String> homeLocations);

    /**
     * set store location strategy for creating a new workspace.
     * <p>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setStoreLocations(Map<String, String> storeLocations);

}
