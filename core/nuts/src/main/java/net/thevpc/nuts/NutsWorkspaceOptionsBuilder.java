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

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Mutable Workspace options
 *
 * @author thevpc
 * @app.category Config
 */
public interface NutsWorkspaceOptionsBuilder extends NutsWorkspaceOptions {

    /**
     * create NutsWorkspaceOptionsBuilder instance for the given session (shall not be null).
     *
     * @return new NutsWorkspaceOptionsBuilder instance
     */
    static NutsWorkspaceOptionsBuilder of() {
        return new DefaultNutsWorkspaceOptionsBuilder();
    }

    /**
     * create a <strong>mutable</strong> copy of this instance
     *
     * @return a <strong>mutable</strong> copy of this instance
     */
    NutsWorkspaceOptionsBuilder copy();

    NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion);

    NutsWorkspaceOptionsBuilder setApplicationArguments(List<String> applicationArguments);


    NutsWorkspaceOptionsBuilder setArchetype(String archetype);

    NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);


    NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm);

    NutsWorkspaceOptionsBuilder setDry(Boolean dry);


    NutsWorkspaceOptionsBuilder setCreationTime(long creationTime);


    NutsWorkspaceOptionsBuilder setExcludedExtensions(List<String> excludedExtensions);

    NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType);

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsWorkspaceOptionsBuilder setRunAs(NutsRunAs runAs);

    NutsWorkspaceOptionsBuilder setExecutorOptions(List<String> executorOptions);

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

    NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand);


    NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions);


    NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig);


    NutsWorkspaceOptionsBuilder setName(String workspaceName);

    NutsWorkspaceOptionsBuilder setOpenMode(NutsOpenMode openMode);


    NutsWorkspaceOptionsBuilder setOutputFormat(NutsContentType outputFormat);


    NutsWorkspaceOptionsBuilder setOutputFormatOptions(List<String> options);


    NutsWorkspaceOptionsBuilder setCredentials(char[] credentials);


    NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy);


    NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId);

    NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout);

    NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy);


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

    NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode);

    NutsWorkspaceOptionsBuilder setRepositories(List<String> transientRepositories);

    NutsWorkspaceOptionsBuilder setWorkspace(String workspace);

    NutsWorkspaceOptionsBuilder setDebug(String debug);

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

    NutsWorkspaceOptionsBuilder setGui(Boolean gui);

    NutsWorkspaceOptionsBuilder setInherited(Boolean inherited);


    NutsWorkspaceOptionsBuilder setReadOnly(Boolean readOnly);

    NutsWorkspaceOptionsBuilder setRecover(Boolean recover);

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     *
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NutsWorkspaceOptionsBuilder setReset(Boolean reset);

    NutsWorkspaceOptionsBuilder setCommandVersion(Boolean version);

    NutsWorkspaceOptionsBuilder setCommandHelp(Boolean help);

    NutsWorkspaceOptionsBuilder setSkipCompanions(Boolean skipInstallCompanions);

    NutsWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome);

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

    NutsWorkspaceOptionsBuilder setTrace(Boolean trace);

    NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions);

    NutsWorkspaceOptionsBuilder setCached(Boolean cached);

    NutsWorkspaceOptionsBuilder setIndexed(Boolean indexed);

    NutsWorkspaceOptionsBuilder setTransitive(Boolean transitive);

    NutsWorkspaceOptionsBuilder setBot(Boolean bot);

    NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy);

    NutsWorkspaceOptionsBuilder setStdin(InputStream stdin);

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
    NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService);

    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsWorkspaceOptionsBuilder setExpireTime(Instant value);

    NutsWorkspaceOptionsBuilder setSkipErrors(Boolean value);

    NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean value);

    NutsWorkspaceOptionsBuilder setErrors(List<NutsMessage> errors);

    NutsWorkspaceOptionsBuilder setCustomOptions(List<String> properties);

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

    NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other);

    NutsWorkspaceOptionsBuilder setCommandLine(String commandLine, NutsSession session);

    NutsWorkspaceOptionsBuilder setCommandLine(String[] args, NutsSession session);

    NutsWorkspaceOptionsBuilder setUsername(String username);

    NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value);

    NutsWorkspaceOptionsBuilder setHomeLocation(NutsHomeLocation location, String value);

    NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options);

    NutsWorkspaceOptions build();

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsWorkspaceOptionsBuilder setDependencySolver(String dependencySolver);

}
