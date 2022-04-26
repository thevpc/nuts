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
    private List<String> outputFormatOptions = new ArrayList<>();

    private List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private String apiVersion = null;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * child workspaces)
     */
    private String runtimeId;

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
    private String workspace = null;

    /**
     * out line prefix, option-type : exported (inherited in child workspaces)
     */
    private String outLinePrefix = null;

    /**
     * err line prefix, option-type : exported (inherited in child workspaces)
     */
    private String errLinePrefix = null;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private String name = null;

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
    private String userName = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private char[] credentials = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsTerminalMode terminalMode = null;

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
    private String progressOptions = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String dependencySolver = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsConfirmationMode confirm = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsContentType outputFormat = null;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsOpenMode openMode = NutsOpenMode.OPEN_OR_CREATE;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private long creationTime;

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
    private NutsRunAs runAs = NutsRunAs.CURRENT_USER;

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
    private Map<NutsStoreLocation, String> storeLocations = new HashMap<>();

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NutsHomeLocation, String> homeLocations = new HashMap<>();

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsOsFamily storeLocationLayout = null;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy storeLocationStrategy = null;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsFetchStrategy fetchStrategy = NutsFetchStrategy.ONLINE;

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
    private InputStream stdin = null;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stdout = null;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stderr = null;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private ExecutorService executorService = null;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
//    private String bootRepositories = null;
    private Instant expireTime = null;
    private List<NutsMessage> errors = new ArrayList<>();
    private Boolean skipErrors;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String locale;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String theme;

    public DefaultNutsWorkspaceOptionsBuilder() {

    }

    public DefaultNutsWorkspaceOptionsBuilder(List<String> outputFormatOptions, List<String> customOptions, String apiVersion, String runtimeId, String javaCommand, String javaOptions, String workspace, String outLinePrefix, String errLinePrefix, String name, Boolean skipCompanions, Boolean skipWelcome, Boolean skipBoot, Boolean global, Boolean gui, List<String> excludedExtensions, List<String> repositories, String userName, char[] credentials, NutsTerminalMode terminalMode, Boolean readOnly, Boolean trace, String progressOptions, String dependencySolver, NutsLogConfig logConfig, NutsConfirmationMode confirm, NutsContentType outputFormat, List<String> applicationArguments, NutsOpenMode openMode, long creationTime, Boolean dry, Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions, Boolean recover, Boolean reset, Boolean commandVersion, Boolean commandHelp, String debug, Boolean inherited, NutsExecutionType executionType, NutsRunAs runAs, String archetype, Boolean switchWorkspace, Map<NutsStoreLocation, String> storeLocations, Map<NutsHomeLocation, String> homeLocations, NutsOsFamily storeLocationLayout, NutsStoreLocationStrategy storeLocationStrategy, NutsStoreLocationStrategy repositoryStoreLocationStrategy, NutsFetchStrategy fetchStrategy, Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, InputStream stdin, PrintStream stdout, PrintStream stderr, ExecutorService executorService, Instant expireTime, List<NutsMessage> errors, Boolean skipErrors, String locale, String theme) {
        this.outputFormatOptions = PrivateNutsUtilCollections.nonNullList(outputFormatOptions);
        this.customOptions = PrivateNutsUtilCollections.nonNullList(customOptions);
        this.apiVersion = NutsUtilStrings.trimToNull(apiVersion);
        this.runtimeId = NutsUtilStrings.trimToNull(runtimeId);
        this.javaCommand = NutsUtilStrings.trimToNull(javaCommand);
        this.javaOptions = NutsUtilStrings.trimToNull(javaOptions);
        this.workspace = NutsUtilStrings.trimToNull(workspace);
        this.outLinePrefix = NutsUtilStrings.trimToNull(outLinePrefix);
        this.errLinePrefix = NutsUtilStrings.trimToNull(errLinePrefix);
        this.name = NutsUtilStrings.trimToNull(name);
        this.skipCompanions = skipCompanions;
        this.skipWelcome = skipWelcome;
        this.skipBoot = skipBoot;
        this.global = global;
        this.gui = gui;
        this.excludedExtensions = PrivateNutsUtilCollections.nonNullList(excludedExtensions);
        this.repositories = PrivateNutsUtilCollections.nonNullList(repositories);
        this.userName = NutsUtilStrings.trimToNull(userName);
        this.credentials = credentials == null ? null : Arrays.copyOf(credentials, credentials.length);
        this.terminalMode = terminalMode;
        this.readOnly = readOnly;
        this.trace = trace;
        this.progressOptions = NutsUtilStrings.trimToNull(progressOptions);
        this.dependencySolver = NutsUtilStrings.trimToNull(dependencySolver);
        this.logConfig = logConfig == null ? null : logConfig.copy();
        this.confirm = confirm;
        this.outputFormat = outputFormat;
        this.applicationArguments = PrivateNutsUtilCollections.nonNullList(applicationArguments);
        this.openMode = openMode == null ? NutsOpenMode.OPEN_OR_CREATE : openMode;
        this.creationTime = creationTime;
        this.dry = dry;
        this.classLoaderSupplier = classLoaderSupplier;
        this.executorOptions = PrivateNutsUtilCollections.nonNullList(executorOptions);
        this.recover = recover;
        this.reset = reset;
        this.commandVersion = commandVersion;
        this.commandHelp = commandHelp;
        this.debug = NutsUtilStrings.trimToNull(debug);
        this.inherited = inherited;
        this.executionType = executionType;
        this.runAs = runAs == null ? NutsRunAs.CURRENT_USER : runAs;
        this.archetype = NutsUtilStrings.trimToNull(archetype);
        this.switchWorkspace = switchWorkspace;
        this.storeLocations = PrivateNutsUtilCollections.unmodifiableMap(storeLocations);
        this.homeLocations = PrivateNutsUtilCollections.unmodifiableMap(homeLocations);
        this.storeLocationLayout = storeLocationLayout;
        this.storeLocationStrategy = storeLocationStrategy;
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        this.fetchStrategy = fetchStrategy == null ? NutsFetchStrategy.ONLINE : fetchStrategy;
        this.cached = cached;
        this.indexed = indexed;
        this.transitive = transitive;
        this.bot = bot;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executorService = executorService;
        this.expireTime = expireTime;
        this.errors = PrivateNutsUtilCollections.nonNullList(errors);
        this.skipErrors = skipErrors;
        this.locale = NutsUtilStrings.trimToNull(locale);
        this.theme = NutsUtilStrings.trimToNull(theme);
    }

    @Override
    public NutsWorkspaceOptionsBuilder copy() {
        return new DefaultNutsWorkspaceOptionsBuilder().setAll(this);
    }


    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public List<String> getApplicationArguments() {
        return applicationArguments;
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = PrivateNutsUtilCollections.nonNullList(applicationArguments);
        return this;
    }

    @Override
    public String getArchetype() {
        return archetype;
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
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return classLoaderSupplier;
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
    public NutsConfirmationMode getConfirm() {
        return confirm;
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
    public boolean isDry() {
        return dry != null && dry;
    }

    @Override
    public Boolean getDry() {
        return dry;
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
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public List<String> getExcludedExtensions() {
        return excludedExtensions;
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = PrivateNutsUtilCollections.nonNullList(excludedExtensions);
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
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
    public NutsRunAs getRunAs() {
        return runAs;
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NutsWorkspaceOptionsBuilder setRunAs(NutsRunAs runAs) {
        this.runAs = runAs == null ? NutsRunAs.CURRENT_USER : runAs;
        return this;
    }

    @Override
    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = PrivateNutsUtilCollections.nonNullList(executorOptions);
        return this;
    }

    @Override
    public String getHomeLocation(NutsHomeLocation location) {
        return homeLocations.get(location);
    }

    @Override
    public Map<NutsHomeLocation, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    @Override
    public String getJavaCommand() {
        return javaCommand;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public String getJavaOptions() {
        return javaOptions;
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
    public NutsLogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    @Override
    public String getName() {
        return name;
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
    public NutsOpenMode getOpenMode() {
        return openMode;
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
    public NutsContentType getOutputFormat() {
        return outputFormat;
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
    public List<String> getOutputFormatOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(outputFormatOptions);
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOutputFormatOptions(List<String> options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(PrivateNutsUtilCollections.nonNullList(options).toArray(new String[0]));
    }

    public NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public char[] getCredentials() {
        return credentials;
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
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
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
    public String getRuntimeId() {
        return runtimeId;
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations.get(folder);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
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
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
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
    public Map<NutsStoreLocation, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocations(Map<NutsStoreLocation, String> storeLocations) {
        this.storeLocations.clear();
        if (storeLocations != null) {
            this.storeLocations.putAll(storeLocations);
        }
        return this;
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
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
    public List<String> getRepositories() {
        return PrivateNutsUtilCollections.unmodifiableList(repositories);
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRepositories(List<String> repositories) {
        this.repositories = PrivateNutsUtilCollections.nonNullList(repositories);
        return this;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getWorkspace() {
        return workspace;
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
    public String getDebug() {
        return debug;
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
    public boolean isGlobal() {
        return global != null && global;
    }

    @Override
    public Boolean getGlobal() {
        return global;
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
    public boolean isGui() {
        return gui != null && gui;
    }

    @Override
    public Boolean getGui() {
        return gui;
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
    public boolean isInherited() {
        return inherited != null && inherited;
    }

    @Override
    public Boolean getInherited() {
        return inherited;
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
    public boolean isReadOnly() {
        return readOnly != null && readOnly;
    }

    @Override
    public Boolean getReadOnly() {
        return readOnly;
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
    public boolean isRecover() {
        return recover != null && recover;
    }

    @Override
    public Boolean getRecover() {
        return recover;
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
    public boolean isReset() {
        return reset != null && reset;
    }

    @Override
    public Boolean getReset() {
        return reset;
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
    public boolean isCommandVersion() {
        return commandVersion != null && commandVersion;
    }

    @Override
    public Boolean getCommandVersion() {
        return commandVersion;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public boolean isCommandHelp() {
        return commandHelp != null && commandHelp;
    }

    @Override
    public Boolean getCommandHelp() {
        return commandHelp;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public boolean isSkipCompanions() {
        return skipCompanions != null && skipCompanions;
    }

    @Override
    public Boolean getSkipCompanions() {
        return skipCompanions;
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
    public boolean isSkipWelcome() {
        return skipWelcome != null && skipWelcome;
    }

    @Override
    public Boolean getSkipWelcome() {
        return skipWelcome;
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
    public String getOutLinePrefix() {
        return outLinePrefix;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public String getErrLinePrefix() {
        return errLinePrefix;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public boolean isSkipBoot() {
        return skipBoot != null && skipBoot;
    }

    @Override
    public Boolean getSkipBoot() {
        return skipBoot;
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
    public boolean isTrace() {
        return trace == null || trace;
    }

    @Override
    public Boolean getTrace() {
        return trace;
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

    public String getProgressOptions() {
        return progressOptions;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    public boolean isCached() {
        return cached == null || cached;
    }

    @Override
    public Boolean getCached() {
        return cached;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    public boolean isIndexed() {
        return indexed == null || indexed;
    }

    @Override
    public Boolean getIndexed() {
        return indexed;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive == null || transitive;
    }

    @Override
    public Boolean getTransitive() {
        return transitive;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isBot() {
        return bot != null && bot;
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy == null ? NutsFetchStrategy.ONLINE : fetchStrategy;
        return this;
    }

    @Override
    public InputStream getStdin() {
        return stdin;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public PrintStream getStdout() {
        return stdout;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public PrintStream getStderr() {
        return stderr;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public boolean isSkipErrors() {
        return skipErrors != null && skipErrors;
    }

    @Override
    public Boolean getSkipErrors() {
        return skipErrors;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public boolean isSwitchWorkspace() {
        return switchWorkspace != null && switchWorkspace;
    }

    @Override
    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    public NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public List<NutsMessage> getErrors() {
        return errors;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setErrors(List<NutsMessage> errors) {
        this.errors = PrivateNutsUtilCollections.nonNullList(errors);
        return this;
    }

    @Override
    public List<String> getCustomOptions() {
        return customOptions;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCustomOptions(List<String> properties) {
        this.customOptions = PrivateNutsUtilCollections.nonNullList(properties);
        return this;
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public String getTheme() {
        return theme;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other) {
        this.setApiVersion(other.getApiVersion());
        this.setRuntimeId(other.getRuntimeId());
        this.setJavaCommand(other.getJavaCommand());
        this.setJavaOptions(other.getJavaOptions());
        this.setWorkspace(other.getWorkspace());
        this.setName(other.getName());
        this.setSkipCompanions(other.getSkipCompanions());
        this.setSkipWelcome(other.getSkipWelcome());
        this.setSkipBoot(other.getSkipBoot());
        this.setGlobal(other.getGlobal());
        this.setGui(other.getGui());
        this.setUsername(other.getUserName());
        this.setCredentials(other.getCredentials());
        this.setTerminalMode(other.getTerminalMode());
        this.setReadOnly(other.getReadOnly());
        this.setTrace(other.getTrace());
        this.setProgressOptions(other.getProgressOptions());
        this.setLogConfig(other.getLogConfig() == null ? null : new NutsLogConfig(other.getLogConfig())); //TODO
        this.setConfirm(other.getConfirm());
        this.setConfirm(other.getConfirm());
        this.setOutputFormat(other.getOutputFormat());
        this.setOutputFormatOptions(other.getOutputFormatOptions());
        this.setOpenMode(other.getOpenMode());
        this.setCreationTime(other.getCreationTime());
        this.setDry(other.getDry());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier());
        this.setExecutorOptions(other.getExecutorOptions());
        this.setRecover(other.getRecover());
        this.setReset(other.getReset());
        this.setCommandVersion(other.getCommandVersion());
        this.setCommandHelp(other.getCommandHelp());
        this.setDebug(other.getDebug());
        this.setInherited(other.getInherited());
        this.setExecutionType(other.getExecutionType());
        this.setRunAs(other.getRunAs());
        this.setArchetype(other.getArchetype());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy());
        this.setHomeLocations(other.getHomeLocations());
        this.setStoreLocations(other.getStoreLocations());
        this.setStoreLocationLayout(other.getStoreLocationLayout());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy());
        this.setRepositoryStoreLocationStrategy(other.getRepositoryStoreLocationStrategy());
        this.setFetchStrategy(other.getFetchStrategy());
        this.setCached(other.getCached());
        this.setIndexed(other.getIndexed());
        this.setTransitive(other.getTransitive());
        this.setBot(other.getBot());
        this.setStdin(other.getStdin());
        this.setStdout(other.getStdout());
        this.setStderr(other.getStderr());
        this.setExecutorService(other.getExecutorService());
//        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(PrivateNutsUtilCollections.nonNullList(other.getExcludedExtensions()));
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(PrivateNutsUtilCollections.nonNullList(other.getRepositories()));
        this.setApplicationArguments(PrivateNutsUtilCollections.nonNullList(other.getApplicationArguments()));
        this.setCustomOptions(PrivateNutsUtilCollections.nonNullList(other.getCustomOptions()));
        this.setExpireTime(other.getExpireTime());
        this.setErrors(other.getErrors());
        this.setSkipErrors(other.getSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        this.setLocale(other.getLocale());
        this.setTheme(other.getTheme());
        this.setDependencySolver(other.getDependencySolver());
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
    public NutsWorkspaceOptionsBuilder setUsername(String username) {
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
            storeLocations.remove(location);
        } else {
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
            homeLocations.remove(location);
        } else {
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
                        outputFormatOptions.add(option);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NutsWorkspaceOptions build() {
        return new DefaultNutsWorkspaceOptions(
                getOutputFormatOptions(), getCustomOptions(), getApiVersion(), getRuntimeId(), getJavaCommand(), getJavaOptions()
                , getWorkspace(), getOutLinePrefix(), getErrLinePrefix(), getName(), getSkipCompanions(), getSkipWelcome(), getSkipBoot()
                , getGlobal(), getGui(), getExcludedExtensions(), getRepositories(), getUserName(), getCredentials(), getTerminalMode()
                , getReadOnly(), getTrace(), getProgressOptions(), getDependencySolver(), getLogConfig(), getConfirm(), getOutputFormat()
                , getApplicationArguments(), getOpenMode(), getCreationTime(), getDry(), getClassLoaderSupplier(), getExecutorOptions()
                , getRecover(), getReset(), getCommandVersion(), getCommandHelp(), getDebug(), getCommandHelp(), getExecutionType()
                , getRunAs(), getArchetype(), getSwitchWorkspace(), getStoreLocations(), getHomeLocations(), getStoreLocationLayout()
                , getStoreLocationStrategy(), getRepositoryStoreLocationStrategy(), getFetchStrategy(), getCached(), getCached()
                , getTransitive(), getBot(), getStdin(), getStdout(), getStdout(), getExecutorService(), getExpireTime(), getErrors()
                , getSkipErrors(), getLocale(), getTheme());
    }

    @Override
    public String getDependencySolver() {
        return dependencySolver;
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

}
