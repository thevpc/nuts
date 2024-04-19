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

import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.util.NSupportMode;

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
public interface NWorkspaceOptionsBuilder extends NWorkspaceOptions {

    /**
     * create NutsWorkspaceOptionsBuilder instance for the given session (shall not be null).
     *
     * @return new NutsWorkspaceOptionsBuilder instance
     */
    static NWorkspaceOptionsBuilder of() {
        return new DefaultNWorkspaceOptionsBuilder();
    }

    NWorkspaceOptionsBuilder setInitLaunchers(Boolean initLaunchers);

    NWorkspaceOptionsBuilder setInitScripts(Boolean initScripts);

    NWorkspaceOptionsBuilder setInitPlatforms(Boolean initPlatforms);

    NWorkspaceOptionsBuilder setInitJava(Boolean initJava);

    NWorkspaceOptionsBuilder setIsolationLevel(NIsolationLevel isolationLevel);

    NWorkspaceOptionsBuilder setDesktopLauncher(NSupportMode desktopLauncher);

    NWorkspaceOptionsBuilder setMenuLauncher(NSupportMode menuLauncher);

    NWorkspaceOptionsBuilder setUserLauncher(NSupportMode userLauncher);

    /**
     * create a <strong>mutable</strong> copy of this instance
     *
     * @return a <strong>mutable</strong> copy of this instance
     */
    NWorkspaceOptionsBuilder copy();

    NWorkspaceOptionsBuilder setApiVersion(NVersion apiVersion);

    NWorkspaceOptionsBuilder setApplicationArguments(List<String> applicationArguments);


    NWorkspaceOptionsBuilder setArchetype(String archetype);

    NWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);


    NWorkspaceOptionsBuilder setConfirm(NConfirmationMode confirm);

    NWorkspaceOptionsBuilder setDry(Boolean dry);
    
    NWorkspaceOptionsBuilder setShowException(Boolean dry);

    NWorkspaceOptionsBuilder setCreationTime(Instant creationTime);


    NWorkspaceOptionsBuilder setExcludedExtensions(List<String> excludedExtensions);

    NWorkspaceOptionsBuilder setExecutionType(NExecutionType executionType);

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NWorkspaceOptionsBuilder setRunAs(NRunAs runAs);

    NWorkspaceOptionsBuilder setExecutorOptions(List<String> executorOptions);

    /**
     * set home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NWorkspaceOptionsBuilder setHomeLocations(Map<NHomeLocation, String> homeLocations);

    NWorkspaceOptionsBuilder setJavaCommand(String javaCommand);


    NWorkspaceOptionsBuilder setJavaOptions(String javaOptions);


    NWorkspaceOptionsBuilder setLogConfig(NLogConfig logConfig);


    NWorkspaceOptionsBuilder setName(String workspaceName);

    NWorkspaceOptionsBuilder setOpenMode(NOpenMode openMode);


    NWorkspaceOptionsBuilder setOutputFormat(NContentType outputFormat);


    NWorkspaceOptionsBuilder setOutputFormatOptions(List<String> options);


    NWorkspaceOptionsBuilder setCredentials(char[] credentials);


    NWorkspaceOptionsBuilder setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy);


    NWorkspaceOptionsBuilder setRuntimeId(NId runtimeId);

    NWorkspaceOptionsBuilder setStoreLayout(NOsFamily storeLayout);

    NWorkspaceOptionsBuilder setStoreStrategy(NStoreStrategy storeStrategy);


    /**
     * set store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NWorkspaceOptionsBuilder setStoreLocations(Map<NStoreType, String> storeLocations);

    NWorkspaceOptionsBuilder setTerminalMode(NTerminalMode terminalMode);

    NWorkspaceOptionsBuilder setRepositories(List<String> transientRepositories);

    NWorkspaceOptionsBuilder setWorkspace(String workspace);

    NWorkspaceOptionsBuilder setDebug(String debug);

    /**
     * update 'global' option.
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @param global if true consider global/system repository shared between all users
     * @return if true consider global/system repository
     */
    NWorkspaceOptionsBuilder setSystem(Boolean global);

    NWorkspaceOptionsBuilder setGui(Boolean gui);

    NWorkspaceOptionsBuilder setInherited(Boolean inherited);


    NWorkspaceOptionsBuilder setReadOnly(Boolean readOnly);

    NWorkspaceOptionsBuilder setRecover(Boolean recover);

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     *
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NWorkspaceOptionsBuilder setReset(Boolean reset);

    NWorkspaceOptionsBuilder setCommandVersion(Boolean version);

    NWorkspaceOptionsBuilder setCommandHelp(Boolean help);

    NWorkspaceOptionsBuilder setInstallCompanions(Boolean skipInstallCompanions);

    NWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NWorkspaceOptionsBuilder setOutLinePrefix(String value);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NWorkspaceOptionsBuilder setErrLinePrefix(String value);

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
    NWorkspaceOptionsBuilder setSkipBoot(Boolean skipBoot);

    NWorkspaceOptionsBuilder setTrace(Boolean trace);

    NWorkspaceOptionsBuilder setProgressOptions(String progressOptions);

    NWorkspaceOptionsBuilder setCached(Boolean cached);

    NWorkspaceOptionsBuilder setIndexed(Boolean indexed);

    NWorkspaceOptionsBuilder setTransitive(Boolean transitive);

    NWorkspaceOptionsBuilder setBot(Boolean bot);

    NWorkspaceOptionsBuilder setFetchStrategy(NFetchStrategy fetchStrategy);

    NWorkspaceOptionsBuilder setStdin(InputStream stdin);

    NWorkspaceOptionsBuilder setStdout(PrintStream stdout);

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NWorkspaceOptionsBuilder setStderr(PrintStream stderr);

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService);

    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NWorkspaceOptionsBuilder setExpireTime(Instant value);

    NWorkspaceOptionsBuilder setSkipErrors(Boolean value);

    NWorkspaceOptionsBuilder setSwitchWorkspace(Boolean value);

    NWorkspaceOptionsBuilder setErrors(List<NMsg> errors);

    NWorkspaceOptionsBuilder setCustomOptions(List<String> properties);

    /**
     * set locale
     *
     * @param locale value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NWorkspaceOptionsBuilder setLocale(String locale);

    /**
     * set theme
     *
     * @param theme value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NWorkspaceOptionsBuilder setTheme(String theme);

    NWorkspaceOptionsBuilder setAll(NWorkspaceOptions other);
    NWorkspaceOptionsBuilder setAllPresent(NWorkspaceOptions other);

    NWorkspaceOptionsBuilder setCmdLine(String cmdLine, NSession session);

    NWorkspaceOptionsBuilder setCmdLine(String[] args, NSession session);

    NWorkspaceOptionsBuilder setUserName(String username);

    NWorkspaceOptionsBuilder setStoreLocation(NStoreType location, String value);

    NWorkspaceOptionsBuilder setHomeLocation(NHomeLocation location, String value);

    NWorkspaceOptionsBuilder addOutputFormatOptions(String... options);

    NWorkspaceOptions build();

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NWorkspaceOptionsBuilder setDependencySolver(String dependencySolver);
    NWorkspaceOptionsBuilder unsetRuntimeOptions();
    NWorkspaceOptionsBuilder unsetCreationOptions();
    NWorkspaceOptionsBuilder unsetExportedOptions();

}
