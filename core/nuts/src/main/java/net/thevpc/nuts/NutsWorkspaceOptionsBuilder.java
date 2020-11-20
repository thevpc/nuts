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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Mutable Workspace options
 * %category Config
 */
public interface NutsWorkspaceOptionsBuilder extends NutsWorkspaceOptions{
    NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other);

    NutsWorkspaceOptionsBuilder parseCommandLine(String commandLine);

    NutsWorkspaceOptionsBuilder parseArguments(String[] args);

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

    NutsWorkspaceOptionsBuilder setSkipErrors(boolean value);

    NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean value);

    NutsWorkspaceOptionsBuilder setErrors(String[] errors);

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
    NutsWorkspaceOptionsBuilder setSkipBoot(boolean skipBoot);

    NutsWorkspaceOptionsBuilder setSkipWelcome(boolean skipWelcome);

    NutsWorkspaceOptionsBuilder setOpenMode(NutsWorkspaceOpenMode openMode);

    NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm);

    NutsWorkspaceOptionsBuilder setOutputFormat(NutsContentType outputFormat);

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

}
