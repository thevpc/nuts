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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsArgumentsParser;
import net.thevpc.nuts.boot.PrivateNutsUtilCollections;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace creation/opening options class.
 * <p>
 * %category Config
 *
 * @since 0.5.4
 */
public class DefaultNutsWorkspaceOptionsBuilder implements NutsWorkspaceOptionsBuilder {

    private static final long serialVersionUID = 1;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> outputFormatOptions;

    private List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private NutsVersion apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * child workspaces)
     */
    private NutsId runtimeId;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String javaCommand;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String javaOptions;

    /**
     * workspace folder location path option-type : exported (inherited in child
     * workspaces)
     */
    private String workspace;

    /**
     * out line prefix, option-type : exported (inherited in child workspaces)
     */
    private String outLinePrefix;

    /**
     * err line prefix, option-type : exported (inherited in child workspaces)
     */
    private String errLinePrefix;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private String name;

    /**
     * if true, do not install nuts companion tools upon workspace creation
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean skipCompanions;

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * defaults to false option-type : exported (inherited in child workspaces)
     *
     * @since 0.5.5
     */
    private Boolean skipWelcome;

    /**
     * if true, do not bootstrap workspace after reset/recover. When
     * reset/recover is not active this option is not accepted and an error will
     * be thrown
     *
     * @since 0.6.0
     */
    private Boolean skipBoot;

    /**
     * if true consider global/system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean global;

    /**
     * if true consider GUI/Swing mode
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean gui;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> excludedExtensions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> repositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String userName;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private char[] credentials;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsTerminalMode terminalMode;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean readOnly;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean trace;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String progressOptions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String dependencySolver;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsOpenMode openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Instant creationTime;

    /**
     * if true no real execution, wil dry exec option-type : runtime (available
     * only for the current workspace instance)
     */
    private Boolean dry;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Supplier<ClassLoader> classLoaderSupplier;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean recover;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean reset;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean commandVersion;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean commandHelp;

    /**
     * option-type : runtime / exported (depending on the value)
     */
    private String debug;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean inherited;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private NutsRunAs runAs;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String archetype;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @since 0.8.0
     */
    private Boolean switchWorkspace;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NutsStoreLocation, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NutsHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsOsFamily storeLocationLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy storeLocationStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsFetchStrategy fetchStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean cached;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean indexed;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean transitive;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean bot;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private InputStream stdin;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stdout;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stderr;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private ExecutorService executorService;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
//    private String bootRepositories;
    private Instant expireTime;
    private List<NutsMessage> errors;
    private Boolean skipErrors;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String locale;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String theme;

    private Boolean initLaunchers;
    private Boolean initScripts;
    private Boolean initPlatforms;
    private Boolean initJava;
    private NutsWorkspaceIsolation isolation;
    private NutsSupportMode desktopLauncher;
    private NutsSupportMode menuLauncher;
    private NutsSupportMode userLauncher;

    public DefaultNutsWorkspaceOptionsBuilder() {

    }

    @Override
    public NutsOptional<NutsSupportMode> getDesktopLauncher() {
        return NutsOptional.of(desktopLauncher);
    }

    @Override
    public NutsOptional<NutsSupportMode> getMenuLauncher() {
        return NutsOptional.of(menuLauncher);
    }

    @Override
    public NutsOptional<NutsSupportMode> getUserLauncher() {
        return NutsOptional.of(userLauncher);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setInitLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setInitScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setInitPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setInitJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setIsolation(NutsWorkspaceIsolation isolation) {
        this.isolation = isolation;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setDesktopLauncher(NutsSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setMenuLauncher(NutsSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setUserLauncher(NutsSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder copy() {
        return new DefaultNutsWorkspaceOptionsBuilder().setAll(this);
    }


    @Override
    public NutsOptional<NutsVersion> getApiVersion() {
        return NutsOptional.of(apiVersion);
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApiVersion(NutsVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getApplicationArguments() {
        return NutsOptional.of(applicationArguments);
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NutsOptional<String> getArchetype() {
        return NutsOptional.of(archetype);
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public NutsOptional<Supplier<ClassLoader>> getClassLoaderSupplier() {
        return NutsOptional.of(classLoaderSupplier);
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NutsOptional<NutsConfirmationMode> getConfirm() {
        return NutsOptional.of(confirm);
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getDry() {
        return NutsOptional.of(dry);
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public NutsOptional<Instant> getCreationTime() {
        return NutsOptional.of(creationTime);
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getExcludedExtensions() {
        return NutsOptional.of(excludedExtensions);
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public NutsOptional<NutsExecutionType> getExecutionType() {
        return NutsOptional.of(executionType);
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NutsOptional<NutsRunAs> getRunAs() {
        return NutsOptional.of(runAs);
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NutsWorkspaceOptionsBuilder setRunAs(NutsRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getExecutorOptions() {
        return NutsOptional.of(executorOptions);
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NutsOptional<String> getHomeLocation(NutsHomeLocation location) {
        return NutsOptional.of(homeLocations.get(location));
    }

    @Override
    public NutsOptional<Map<NutsHomeLocation, String>> getHomeLocations() {
        return NutsOptional.of(homeLocations);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        if (homeLocations != null) {
            if (this.homeLocations == null) {
                this.homeLocations = new HashMap<>();
            }
            this.homeLocations.putAll(homeLocations);
        } else {
            this.homeLocations = null;
        }
        return this;
    }

    @Override
    public NutsOptional<String> getJavaCommand() {
        return NutsOptional.of(javaCommand);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NutsOptional<String> getJavaOptions() {
        return NutsOptional.of(javaOptions);
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public NutsOptional<NutsLogConfig> getLogConfig() {
        return NutsOptional.of(logConfig);
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }

    @Override
    public NutsOptional<String> getName() {
        return NutsOptional.of(name);
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public NutsOptional<NutsOpenMode> getOpenMode() {
        return NutsOptional.of(openMode);
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOpenMode(NutsOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NutsOptional<NutsContentType> getOutputFormat() {
        return NutsOptional.of(outputFormat);
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOutputFormat(NutsContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getOutputFormatOptions() {
        return NutsOptional.of(outputFormatOptions);
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOutputFormatOptions(List<String> options) {
        if (options != null) {
            if (outputFormatOptions == null) {
                outputFormatOptions = new ArrayList<>();
            }
            this.outputFormatOptions.clear();
            return addOutputFormatOptions(PrivateNutsUtilCollections.nonNullList(options).toArray(new String[0]));
        } else {
            this.outputFormatOptions = null;
        }
        return this;
    }

    public NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options) {
        if (outputFormatOptions == null) {
            outputFormatOptions = new ArrayList<>();
        }
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NutsOptional<char[]> getCredentials() {
        return NutsOptional.of(credentials);
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NutsOptional<NutsStoreLocationStrategy> getRepositoryStoreLocationStrategy() {
        return NutsOptional.of(repositoryStoreLocationStrategy);
    }

    /**
     * set repositoryStoreLocationStrategy
     *
     * @param repositoryStoreLocationStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    @Override
    public NutsOptional<NutsId> getRuntimeId() {
        return NutsOptional.of(runtimeId);
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRuntimeId(NutsId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public NutsOptional<String> getStoreLocation(NutsStoreLocation folder) {
        return NutsOptional.of(storeLocations.get(folder));
    }

    @Override
    public NutsOptional<NutsOsFamily> getStoreLocationLayout() {
        return NutsOptional.of(storeLocationLayout);
    }

    /**
     * set storeLocationLayout
     *
     * @param storeLocationLayout new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    @Override
    public NutsOptional<NutsStoreLocationStrategy> getStoreLocationStrategy() {
        return NutsOptional.of(storeLocationStrategy);
    }

    /**
     * set storeLocationStrategy
     *
     * @param storeLocationStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    @Override
    public NutsOptional<Map<NutsStoreLocation, String>> getStoreLocations() {
        return NutsOptional.of(storeLocations);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocations(Map<NutsStoreLocation, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(PrivateNutsUtilCollections.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return this;
    }

    @Override
    public NutsOptional<NutsTerminalMode> getTerminalMode() {
        return NutsOptional.of(terminalMode);
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getRepositories() {
        return NutsOptional.of(repositories);
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRepositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    @Override
    public NutsOptional<String> getUserName() {
        return NutsOptional.of(userName);
    }

    @Override
    public NutsOptional<String> getWorkspace() {
        return NutsOptional.of(workspace);
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NutsOptional<String> getDebug() {
        return NutsOptional.of(debug);
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getGlobal() {
        return NutsOptional.of(global);
    }

    /**
     * set global
     *
     * @param global new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setGlobal(Boolean global) {
        this.global = global;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getGui() {
        return NutsOptional.of(gui);
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getInherited() {
        return NutsOptional.of(inherited);
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getReadOnly() {
        return NutsOptional.of(readOnly);
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getRecover() {
        return NutsOptional.of(recover);
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRecover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getReset() {
        return NutsOptional.of(reset);
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setReset(Boolean reset) {
        this.reset = reset;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getCommandVersion() {
        return NutsOptional.of(commandVersion);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getCommandHelp() {
        return NutsOptional.of(commandHelp);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getSkipCompanions() {
        return NutsOptional.of(skipCompanions);
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipCompanions(Boolean skipInstallCompanions) {
        this.skipCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getSkipWelcome() {
        return NutsOptional.of(skipWelcome);
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NutsOptional<String> getOutLinePrefix() {
        return NutsOptional.of(outLinePrefix);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NutsOptional<String> getErrLinePrefix() {
        return NutsOptional.of(errLinePrefix);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getSkipBoot() {
        return NutsOptional.of(skipBoot);
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getTrace() {
        return NutsOptional.of(trace);
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public NutsOptional<String> getProgressOptions() {
        return NutsOptional.of(progressOptions);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getCached() {
        return NutsOptional.of(cached);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getIndexed() {
        return NutsOptional.of(indexed);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getTransitive() {
        return NutsOptional.of(transitive);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getBot() {
        return NutsOptional.of(bot);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NutsOptional<NutsFetchStrategy> getFetchStrategy() {
        return NutsOptional.of(fetchStrategy);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NutsOptional<InputStream> getStdin() {
        return NutsOptional.of(stdin);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NutsOptional<PrintStream> getStdout() {
        return NutsOptional.of(stdout);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NutsOptional<PrintStream> getStderr() {
        return NutsOptional.of(stderr);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NutsOptional<ExecutorService> getExecutorService() {
        return NutsOptional.of(executorService);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NutsOptional<Instant> getExpireTime() {
        return NutsOptional.of(expireTime);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getSkipErrors() {
        return NutsOptional.of(skipErrors);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NutsOptional<Boolean> getSwitchWorkspace() {
        return NutsOptional.of(switchWorkspace);
    }

    public NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NutsOptional<List<NutsMessage>> getErrors() {
        return NutsOptional.of(errors);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setErrors(List<NutsMessage> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NutsOptional<List<String>> getCustomOptions() {
        return NutsOptional.of(customOptions);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCustomOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NutsOptional<String> getLocale() {
        return NutsOptional.of(locale);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NutsOptional<String> getTheme() {
        return NutsOptional.of(theme);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other) {
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setSkipCompanions(other.getSkipCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setGlobal(other.getGlobal().orNull());
        this.setGui(other.getGui().orNull());
        this.setUserName(other.getUserName().orNull());
        this.setCredentials(other.getCredentials().orNull());
        this.setTerminalMode(other.getTerminalMode().orNull());
        this.setReadOnly(other.getReadOnly().orNull());
        this.setTrace(other.getTrace().orNull());
        this.setProgressOptions(other.getProgressOptions().orNull());
        this.setLogConfig(other.getLogConfig().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setOutputFormat(other.getOutputFormat().orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        this.setOpenMode(other.getOpenMode().orNull());
        this.setCreationTime(other.getCreationTime().orNull());
        this.setDry(other.getDry().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLocationLayout(other.getStoreLocationLayout().orNull());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy().orNull());
        this.setRepositoryStoreLocationStrategy(other.getRepositoryStoreLocationStrategy().orNull());
        this.setFetchStrategy(other.getFetchStrategy().orNull());
        this.setCached(other.getCached().orNull());
        this.setIndexed(other.getIndexed().orNull());
        this.setTransitive(other.getTransitive().orNull());
        this.setBot(other.getBot().orNull());
        this.setStdin(other.getStdin().orNull());
        this.setStdout(other.getStdout().orNull());
        this.setStderr(other.getStderr().orNull());
        this.setExecutorService(other.getExecutorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(other.getExcludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(other.getRepositories().orNull());
        this.setApplicationArguments(other.getApplicationArguments().orNull());
        this.setCustomOptions(other.getCustomOptions().orNull());
        this.setExpireTime(other.getExpireTime().orNull());
        this.setErrors(other.getErrors().orNull());
        this.setSkipErrors(other.getSkipErrors().orNull());
        this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        this.setLocale(other.getLocale().orNull());
        this.setTheme(other.getTheme().orNull());
        this.setDependencySolver(other.getDependencySolver().orNull());
        this.setIsolation(other.getIsolation().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAllPresent(NutsWorkspaceOptions other) {
        if (other != null) {
            if (other.getApiVersion().isPresent()) {
                this.setApiVersion(other.getApiVersion().orNull());
            }
            if (other.getRuntimeId().isPresent()) {
                this.setRuntimeId(other.getRuntimeId().orNull());
            }
            if (other.getJavaCommand().isPresent()) {
                this.setJavaCommand(other.getJavaCommand().orNull());
            }
            if (other.getJavaOptions().isPresent()) {
                this.setJavaOptions(other.getJavaOptions().orNull());
            }
            if (other.getWorkspace().isPresent()) {
                this.setWorkspace(other.getWorkspace().orNull());
            }
            if (other.getName().isPresent()) {
                this.setName(other.getName().orNull());
            }
            if (other.getSkipCompanions().isPresent()) {
                this.setSkipCompanions(other.getSkipCompanions().orNull());
            }
            if (other.getSkipWelcome().isPresent()) {
                this.setSkipWelcome(other.getSkipWelcome().orNull());
            }
            if (other.getSkipBoot().isPresent()) {
                this.setSkipBoot(other.getSkipBoot().orNull());
            }
            if (other.getGlobal().isPresent()) {
                this.setGlobal(other.getGlobal().orNull());
            }
            if (other.getGui().isPresent()) {
                this.setGui(other.getGui().orNull());
            }
            if (other.getUserName().isPresent()) {
                this.setUserName(other.getUserName().orNull());
            }
            if (other.getCredentials().isPresent()) {
                this.setCredentials(other.getCredentials().orNull());
            }
            if (other.getTerminalMode().isPresent()) {
                this.setTerminalMode(other.getTerminalMode().orNull());
            }
            if (other.getReadOnly().isPresent()) {
                this.setReadOnly(other.getReadOnly().orNull());
            }
            if (other.getTrace().isPresent()) {
                this.setTrace(other.getTrace().orNull());
            }
            if (other.getProgressOptions().isPresent()) {
                this.setProgressOptions(other.getProgressOptions().orNull());
            }
            if (other.getLogConfig().isPresent()) {
                this.setLogConfig(other.getLogConfig().orNull());
            }
            if (other.getConfirm().isPresent()) {
                this.setConfirm(other.getConfirm().orNull());
            }
            if (other.getConfirm().isPresent()) {
                this.setConfirm(other.getConfirm().orNull());
            }
            if (other.getOutputFormat().isPresent()) {
                this.setOutputFormat(other.getOutputFormat().orNull());
            }
            if (other.getOutputFormatOptions().isPresent()) {
                this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
            }
            if (other.getOpenMode().isPresent()) {
                this.setOpenMode(other.getOpenMode().orNull());
            }
            if (other.getCreationTime().isPresent()) {
                this.setCreationTime(other.getCreationTime().orNull());
            }
            if (other.getDry().isPresent()) {
                this.setDry(other.getDry().orNull());
            }
            if (other.getClassLoaderSupplier().isPresent()) {
                this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
            }
            if (other.getExecutorOptions().isPresent()) {
                this.setExecutorOptions(other.getExecutorOptions().orNull());
            }
            if (other.getRecover().isPresent()) {
                this.setRecover(other.getRecover().orNull());
            }
            if (other.getReset().isPresent()) {
                this.setReset(other.getReset().orNull());
            }
            if (other.getCommandVersion().isPresent()) {
                this.setCommandVersion(other.getCommandVersion().orNull());
            }
            if (other.getCommandHelp().isPresent()) {
                this.setCommandHelp(other.getCommandHelp().orNull());
            }
            if (other.getDebug().isPresent()) {
                this.setDebug(other.getDebug().orNull());
            }
            if (other.getInherited().isPresent()) {
                this.setInherited(other.getInherited().orNull());
            }
            if (other.getExecutionType().isPresent()) {
                this.setExecutionType(other.getExecutionType().orNull());
            }
            if (other.getRunAs().isPresent()) {
                this.setRunAs(other.getRunAs().orNull());
            }
            if (other.getArchetype().isPresent()) {
                this.setArchetype(other.getArchetype().orNull());
            }
            if (other.getStoreLocationStrategy().isPresent()) {
                this.setStoreLocationStrategy(other.getStoreLocationStrategy().orNull());
            }
            if (other.getHomeLocations().isPresent()) {
                this.setHomeLocations(other.getHomeLocations().orNull());
            }

            if (other.getStoreLocations().isPresent()) {
                this.setStoreLocations(other.getStoreLocations().orNull());
            }
            if (other.getStoreLocationLayout().isPresent()) {
                this.setStoreLocationLayout(other.getStoreLocationLayout().orNull());
            }
            if (other.getStoreLocationStrategy().isPresent()) {
                this.setStoreLocationStrategy(other.getStoreLocationStrategy().orNull());
            }
            if (other.getRepositoryStoreLocationStrategy().isPresent()) {
                this.setRepositoryStoreLocationStrategy(other.getRepositoryStoreLocationStrategy().orNull());
            }
            if (other.getFetchStrategy().isPresent()) {
                this.setFetchStrategy(other.getFetchStrategy().orNull());
            }
            if (other.getCached().isPresent()) {
                this.setCached(other.getCached().orNull());
            }
            if (other.getIndexed().isPresent()) {
                this.setIndexed(other.getIndexed().orNull());
            }
            if (other.getTransitive().isPresent()) {
                this.setTransitive(other.getTransitive().orNull());
            }
            if (other.getBot().isPresent()) {
                this.setBot(other.getBot().orNull());
            }
            if (other.getStdin().isPresent()) {
                this.setStdin(other.getStdin().orNull());
            }
            if (other.getStdout().isPresent()) {
                this.setStdout(other.getStdout().orNull());
            }
            if (other.getStderr().isPresent()) {
                this.setStderr(other.getStderr().orNull());
            }
            if (other.getExecutorService().isPresent()) {
                this.setExecutorService(other.getExecutorService().orNull());
            }
            if (other.getExcludedExtensions().isPresent()) {
                this.setExcludedExtensions(other.getExcludedExtensions().orNull());
            }
            if (other.getRepositories().isPresent()) {
                this.setRepositories(other.getRepositories().orNull());
            }
            if (other.getApplicationArguments().isPresent()) {
                this.setApplicationArguments(other.getApplicationArguments().orNull());
            }
            if (other.getCustomOptions().isPresent()) {
                this.setCustomOptions(other.getCustomOptions().orNull());
            }
            if (other.getExpireTime().isPresent()) {
                this.setExpireTime(other.getExpireTime().orNull());
            }
            if (other.getErrors().isPresent()) {
                this.setErrors(other.getErrors().orNull());
            }
            if (other.getSkipErrors().isPresent()) {
                this.setSkipErrors(other.getSkipErrors().orNull());
            }
            if (other.getSwitchWorkspace().isPresent()) {
                this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
            }
            if (other.getLocale().isPresent()) {
                this.setLocale(other.getLocale().orNull());
            }
            if (other.getTheme().isPresent()) {
                this.setTheme(other.getTheme().orNull());
            }
            if (other.getDependencySolver().isPresent()) {
                this.setDependencySolver(other.getDependencySolver().orNull());
            }
            if (other.getIsolation().isPresent()) {
                this.setIsolation(other.getIsolation().orNull());
            }
            if (other.getInitLaunchers().isPresent()) {
                this.setInitLaunchers(other.getInitLaunchers().orNull());
            }
            if (other.getInitJava().isPresent()) {
                this.setInitJava(other.getInitJava().orNull());
            }
            if (other.getInitScripts().isPresent()) {
                this.setInitScripts(other.getInitScripts().orNull());
            }
            if (other.getInitLaunchers().isPresent()) {
                this.setInitLaunchers(other.getInitLaunchers().orNull());
            }
            if (other.getDesktopLauncher().isPresent()) {
                this.setDesktopLauncher(other.getDesktopLauncher().orNull());
            }
            if (other.getMenuLauncher().isPresent()) {
                this.setMenuLauncher(other.getMenuLauncher().orNull());
            }
            if (other.getUserLauncher().isPresent()) {
                this.setUserLauncher(other.getUserLauncher().orNull());
            }
        }
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandLine(String commandLine, NutsSession session) {
        setCommandLine(NutsCommandLine.parseDefault(commandLine).get(session).toStringArray(), session);
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandLine(String[] args, NutsSession session) {
        PrivateNutsArgumentsParser.parseNutsArguments(args, this, session);
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setUserName(String username) {
        this.userName = username;
        return this;
    }

    /**
     * set store location
     *
     * @param location location
     * @param value    new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value) {
        if (NutsBlankable.isBlank(value)) {
            if (storeLocations != null) {
                storeLocations.remove(location);
            }
        } else {
            if (storeLocations == null) {
                storeLocations = new HashMap<>();
            }
            storeLocations.put(location, value);
        }
        return this;
    }

    /**
     * set home location
     *
     * @param location location
     * @param value    new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setHomeLocation(NutsHomeLocation location, String value) {
        if (NutsBlankable.isBlank(value)) {
            if (homeLocations != null) {
                homeLocations.remove(location);
            }
        } else {
            if (homeLocations == null) {
                homeLocations = new HashMap<>();
            }
            homeLocations.put(location, value);
        }
        return this;
    }

    /**
     * add output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = NutsUtilStrings.trim(option);
                    if (!option.isEmpty()) {
                        if (outputFormatOptions == null) {
                            outputFormatOptions = new ArrayList<>();
                        }
                        outputFormatOptions.add(option);
                    }
                }
            }
        }
        return this;
    }


    @Override
    public NutsOptional<String> getDependencySolver() {
        return NutsOptional.of(dependencySolver);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    @Override
    public String toString() {
        return toCommandLine().toString();
    }

    @Override
    public NutsWorkspaceOptions build() {
        return new DefaultNutsWorkspaceOptions(
                getApiVersion().orNull(), getRuntimeId().orNull(), getWorkspace().orNull(),
                getName().orNull(), getJavaCommand().orNull(), getJavaOptions().orNull(),
                getOutLinePrefix().orNull(), getErrLinePrefix().orNull(), getUserName().orNull(),
                getCredentials().orNull(), getProgressOptions().orNull(), getDependencySolver().orNull(),
                getDebug().orNull(), getArchetype().orNull(), getLocale().orNull(), getTheme().orNull(),
                getLogConfig().orNull(), getConfirm().orNull(), getOutputFormat().orNull(), getOpenMode().orNull(),
                getExecutionType().orNull(), getStoreLocationStrategy().orNull(), getRepositoryStoreLocationStrategy().orNull(),
                getStoreLocationLayout().orNull(), getTerminalMode().orNull(), getFetchStrategy().orNull(),
                getRunAs().orNull(), getCreationTime().orNull(), getExpireTime().orNull(),
                getSkipCompanions().orNull(), getSkipWelcome().orNull(), getSkipBoot().orNull(),
                getGlobal().orNull(), getGui().orNull(), getReadOnly().orNull(), getTrace().orNull(),
                getDry().orNull(), getRecover().orNull(), getReset().orNull(), getCommandVersion().orNull(),
                getCommandHelp().orNull(), getCommandHelp().orNull(), getSwitchWorkspace().orNull(), getCached().orNull(),
                getIndexed().orNull(), getTransitive().orNull(), getBot().orNull(), getSkipErrors().orNull(),
                getIsolation().orNull(), getInitLaunchers().orNull(), getInitScripts().orNull(), getInitPlatforms().orNull(),
                getInitJava().orNull(), getStdin().orNull(), getStdout().orNull(), getStdout().orNull(), getExecutorService().orNull(),
                getClassLoaderSupplier().orNull(), getApplicationArguments().orNull(), getOutputFormatOptions().orNull(),
                getCustomOptions().orNull(), getExcludedExtensions().orNull(), getRepositories().orNull(),
                getExecutorOptions().orNull(), getErrors().orNull(), getStoreLocations().orNull(), getHomeLocations().orNull(),
                getDesktopLauncher().orNull(), getMenuLauncher().orNull(), getUserLauncher().orNull());
    }

    @Override
    public NutsWorkspaceOptionsBuilder builder() {
        return new DefaultNutsWorkspaceOptionsBuilder().setAll(this);
    }

    @Override
    public NutsWorkspaceOptions readOnly() {
        return build();
    }

    @Override
    public NutsCommandLine toCommandLine() {
        return build().toCommandLine();
    }

    @Override
    public NutsCommandLine toCommandLine(NutsWorkspaceOptionsConfig config) {
        return build().toCommandLine(config);
    }

    @Override
    public NutsOptional<NutsWorkspaceIsolation> getIsolation() {
        return NutsOptional.of(isolation);
    }

    @Override
    public NutsOptional<Boolean> getInitLaunchers() {
        return NutsOptional.of(initLaunchers);
    }

    @Override
    public NutsOptional<Boolean> getInitScripts() {
        return NutsOptional.of(initScripts);
    }

    @Override
    public NutsOptional<Boolean> getInitPlatforms() {
        return NutsOptional.of(initPlatforms);
    }

    @Override
    public NutsOptional<Boolean> getInitJava() {
        return NutsOptional.of(initJava);
    }

    @Override
    public NutsWorkspaceOptionsBuilder unsetRuntimeOptions() {
        setCommandHelp(null);
        setCommandVersion(null);
        setOpenMode(null);
        setExecutionType(null);
        setRunAs(null);
        setReset(null);
        setRecover(null);
        setDry(null);
        setExecutorOptions(null);
        setApplicationArguments(null);
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder unsetCreationOptions() {
        setName(null);
        setArchetype(null);
        setStoreLocationLayout(null);
        setStoreLocationStrategy(null);
        setRepositoryStoreLocationStrategy(null);
        setStoreLocations(null);
        setHomeLocations(null);
        setSwitchWorkspace(null);
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder unsetExportedOptions() {
        setJavaCommand(null);
        setJavaOptions(null);
        setWorkspace(null);
        setUserName(null);
        setCredentials(null);
        setApiVersion(null);
        setRuntimeId(null);
        setTerminalMode(null);
        setLogConfig(null);
        setExcludedExtensions(null);
        setRepositories(null);
        setGlobal(null);
        setGui(null);
        setReadOnly(null);
        setTrace(null);
        setProgressOptions(null);
        setDependencySolver(null);
        setDebug(null);
        setSkipCompanions(null);
        setSkipWelcome(null);
        setSkipBoot(null);
        setOutLinePrefix(null);
        setErrLinePrefix(null);
        setCached(null);
        setIndexed(null);
        setTransitive(null);
        setBot(null);
        setFetchStrategy(null);
        setConfirm(null);
        setOutputFormat(null);
        setOutputFormatOptions((List<String>) null);
        setExpireTime(null);
        setTheme(null);
        setLocale(null);
        setInitLaunchers(null);
        setInitPlatforms(null);
        setInitScripts(null);
        setInitJava(null);
        setDesktopLauncher(null);
        setMenuLauncher(null);
        setUserLauncher(null);
        return this;
    }
}
