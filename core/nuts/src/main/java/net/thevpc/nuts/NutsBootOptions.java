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

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class NutsBootOptions implements Serializable, Cloneable {
    private static final long serialVersionUID = 1;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> outputFormatOptions = new ArrayList<>();
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
     * out line prefix, option-type : exported (inherited in child
     * workspaces)
     */
    private String outLinePrefix = null;
    /**
     * err line prefix, option-type : exported (inherited in child
     * workspaces)
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
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
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
     * if true no real execution, wil dry exec
     * option-type : runtime (available only for the current workspace instance)
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
     * option-type : runtime (available only for the current workspace instance)
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
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private InputStream stdin = null;
    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private PrintStream stdout = null;
    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private PrintStream stderr = null;
    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private ExecutorService executorService = null;
    /**
     * option-type : runtime (available only for the current workspace instance)
     */
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

    public NutsBootOptions() {
    }

    public NutsBootOptions copy() {
        try {
            NutsBootOptions t = (NutsBootOptions) clone();
            t.logConfig = logConfig == null ? null : t.logConfig;
            t.storeLocations = new LinkedHashMap<>(storeLocations);
            t.homeLocations = new LinkedHashMap<>(homeLocations);
            t.setExcludedExtensions(PrivateNutsUtilCollections.nonNullList(t.getExcludedExtensions()));
//            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            t.setRepositories(PrivateNutsUtilCollections.nonNullList(t.getRepositories()));
            t.setApplicationArguments(PrivateNutsUtilCollections.nonNullList(t.getApplicationArguments()));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("should never Happen", e);
        }
    }

    
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    
    public List<String> getApplicationArguments() {
        return PrivateNutsUtilCollections.unmodifiableList(applicationArguments);
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setApplicationArguments(String... applicationArguments) {
        this.applicationArguments = PrivateNutsUtilCollections.nonNullList(
                Arrays.asList(applicationArguments)
        );
        return this;
    }

    public NutsBootOptions setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = PrivateNutsUtilCollections.nonNullList(applicationArguments);
        return this;
    }

    
    public String getArchetype() {
        return archetype;
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return classLoaderSupplier;
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    
    public NutsConfirmationMode getConfirm() {
        return confirm;
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    
    public boolean isDry() {
        return dry != null && dry;
    }

    
    public Boolean getDry() {
        return dry;
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    
    public List<String> getExcludedExtensions() {
        return excludedExtensions;
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    
    public NutsRunAs getRunAs() {
        return runAs;
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NutsBootOptions setRunAs(NutsRunAs runAs) {
        this.runAs = runAs == null ? NutsRunAs.CURRENT_USER : runAs;
        return this;
    }

    
    public List<String> getExecutorOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(executorOptions);
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    
    public String getHomeLocation(NutsHomeLocation location) {
        return homeLocations.get(location);
    }

    
    public Map<NutsHomeLocation, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    
    public NutsBootOptions setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    
    public String getJavaCommand() {
        return javaCommand;
    }

    
    public NutsBootOptions setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    
    public String getJavaOptions() {
        return javaOptions;
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    
    public NutsLogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    
    public String getName() {
        return name;
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    
    public NutsOpenMode getOpenMode() {
        return openMode;
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setOpenMode(NutsOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    
    public NutsContentType getOutputFormat() {
        return outputFormat;
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setOutputFormat(NutsContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    
    public List<String> getOutputFormatOptions() {
        return outputFormatOptions;
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setOutputFormatOptions(List<String> options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    
    public char[] getCredentials() {
        return credentials;
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    /**
     * set repositoryStoreLocationStrategy
     *
     * @param repositoryStoreLocationStrategy new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    
    public String getRuntimeId() {
        return runtimeId;
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    
    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations.get(folder);
    }

    
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    /**
     * set storeLocationLayout
     *
     * @param storeLocationLayout new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    /**
     * set storeLocationStrategy
     *
     * @param storeLocationStrategy new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    
    public Map<NutsStoreLocation, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }

    
    public NutsBootOptions setStoreLocations(Map<NutsStoreLocation, String> storeLocations) {
        this.storeLocations.clear();
        if (storeLocations != null) {
            this.storeLocations.putAll(storeLocations);
        }
        return this;
    }

    
    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setTerminalMode(NutsTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    
    public List<String> getRepositories() {
        return PrivateNutsUtilCollections.unmodifiableList(repositories);
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setRepositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    
    public String getUserName() {
        return userName;
    }

    
    public String getWorkspace() {
        return workspace;
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    
    public NutsBootOptions setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    
    public String getDebug() {
        return debug;
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    
    public boolean isGlobal() {
        return global != null && global;
    }

    
    public Boolean getGlobal() {
        return global;
    }

    /**
     * set global
     *
     * @param global new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setGlobal(Boolean global) {
        this.global = global;
        return this;
    }

    
    public boolean isGui() {
        return gui != null && gui;
    }

    
    public Boolean getGui() {
        return gui;
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    
    public boolean isInherited() {
        return inherited != null && inherited;
    }

    
    public Boolean getInherited() {
        return inherited;
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    
    public boolean isReadOnly() {
        return readOnly != null && readOnly;
    }

    
    public Boolean getReadOnly() {
        return readOnly;
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    
    public boolean isRecover() {
        return recover != null && recover;
    }

    
    public Boolean getRecover() {
        return recover;
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setRecover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    
    public boolean isReset() {
        return reset != null && reset;
    }

    
    public Boolean getReset() {
        return reset;
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setReset(Boolean reset) {
        this.reset = reset;
        return this;
    }

    
    public boolean isCommandVersion() {
        return commandVersion != null && commandVersion;
    }

    
    public Boolean getCommandVersion() {
        return commandVersion;
    }

    
    public NutsBootOptions setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    
    public boolean isCommandHelp() {
        return commandHelp != null && commandHelp;
    }

    
    public Boolean getCommandHelp() {
        return commandHelp;
    }

    
    public NutsBootOptions setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    
    public boolean isSkipCompanions() {
        return skipCompanions != null && skipCompanions;
    }

    
    public Boolean getSkipCompanions() {
        return skipCompanions;
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setSkipCompanions(Boolean skipInstallCompanions) {
        this.skipCompanions = skipInstallCompanions;
        return this;
    }

    
    public boolean isSkipWelcome() {
        return skipWelcome != null && skipWelcome;
    }

    
    public Boolean getSkipWelcome() {
        return skipWelcome;
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setSkipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    
    public String getOutLinePrefix() {
        return outLinePrefix;
    }

    
    public NutsBootOptions setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    
    public String getErrLinePrefix() {
        return errLinePrefix;
    }

    
    public NutsBootOptions setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    
    public boolean isSkipBoot() {
        return skipBoot != null && skipBoot;
    }

    
    public Boolean getSkipBoot() {
        return skipBoot;
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setSkipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    
    public boolean isTrace() {
        return trace == null || trace;
    }

    
    public Boolean getTrace() {
        return trace;
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public String getProgressOptions() {
        return progressOptions;
    }

    
    public NutsBootOptions setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    public boolean isCached() {
        return cached == null || cached;
    }

    
    public Boolean getCached() {
        return cached;
    }

    
    public NutsBootOptions setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    public boolean isIndexed() {
        return indexed == null || indexed;
    }

    
    public Boolean getIndexed() {
        return indexed;
    }

    
    public NutsBootOptions setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    
    public boolean isTransitive() {
        return transitive == null || transitive;
    }

    
    public Boolean getTransitive() {
        return transitive;
    }

    
    public NutsBootOptions setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    
    public boolean isBot() {
        return bot != null && bot;
    }

    
    public Boolean getBot() {
        return bot;
    }

    
    public NutsBootOptions setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    
    public NutsFetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    
    public NutsBootOptions setFetchStrategy(NutsFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy == null ? NutsFetchStrategy.ONLINE : fetchStrategy;
        return this;
    }


    public InputStream getStdin() {
        return stdin;
    }


    public NutsBootOptions setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }


    public PrintStream getStdout() {
        return stdout;
    }


    public NutsBootOptions setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }


    public PrintStream getStderr() {
        return stderr;
    }


    public NutsBootOptions setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }

    
    public NutsBootOptions setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    
    public Instant getExpireTime() {
        return expireTime;
    }

    
    public NutsBootOptions setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    
    public boolean isSkipErrors() {
        return skipErrors != null && skipErrors;
    }

    
    public Boolean getSkipErrors() {
        return skipErrors;
    }

    
    public NutsBootOptions setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    
    public boolean isSwitchWorkspace() {
        return switchWorkspace != null && switchWorkspace;
    }

    
    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    public NutsBootOptions setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    
    public List<NutsMessage> getErrors() {
        return PrivateNutsUtilCollections.unmodifiableList(errors);
    }

    
    public NutsBootOptions setErrors(List<NutsMessage> errors) {
        this.errors = PrivateNutsUtilCollections.nonNullList(errors);
        return this;
    }

    
    public List<String> getCustomOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(customOptions);
    }

    
    public NutsBootOptions setCustomOptions(List<String> properties) {
        this.customOptions = PrivateNutsUtilCollections.nonNullList(properties);
        return this;
    }

    
    public String getLocale() {
        return locale;
    }

    
    public NutsBootOptions setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    
    public String getTheme() {
        return theme;
    }

    
    public NutsBootOptions setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    
    public NutsBootOptions setAll(NutsBootOptions other) {
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
        this.setExcludedExtensions(PrivateNutsUtilCollections.nonNullList(other.getExcludedExtensions()));
        this.setRepositories(PrivateNutsUtilCollections.nonNullList(other.getRepositories()));
        this.setApplicationArguments(PrivateNutsUtilCollections.nonNullList(other.getApplicationArguments()));
        this.setCustomOptions(PrivateNutsUtilCollections.nonNullList(other.getCustomOptions()));
        this.setExpireTime(other.getExpireTime());
        this.setErrors(other.getErrors());
        this.setSkipErrors(other.getSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        this.setLocale(other.getLocale());
        this.setTheme(other.getTheme());
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions setUsername(String username) {
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
    
    public NutsBootOptions setStoreLocation(NutsStoreLocation location, String value) {
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
    
    public NutsBootOptions setHomeLocation(NutsHomeLocation location, String value) {
        if (NutsBlankable.isBlank(value)) {
            homeLocations.remove(location);
        } else {
            homeLocations.put(location, value);
        }
        return this;
    }

    public NutsBootOptions addOutputFormatOptions(String... options) {
        if(options!=null){
            addOutputFormatOptions(Arrays.asList(options));
        }
        return this;
    }
    /**
     * add output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    
    public NutsBootOptions addOutputFormatOptions(List<String> options) {
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

    
    public String getDependencySolver() {
        return dependencySolver;
    }

    
    public NutsBootOptions setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    
    public String toString() {
        return NutsApiUtils.defaultToString(this);
    }

    /**
     * bootRepositories list (; separated) where to look for runtime
     * dependencies
     * special
     */
    private String bootRepositories;
    /**
     * special
     */
    private NutsClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private List<NutsDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private List<NutsClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private NutsBootWorkspaceFactory bootWorkspaceFactory;

    /**
     * special
     */
    private List<URL> classWorldURLs;

    /**
     * special
     */
    private ClassLoader classWorldLoader;

    /**
     * special
     */
    private String uuid;

    /**
     * special
     */
    private Set<String> extensionsSet;

    /**
     * special
     */
    private NutsDescriptor runtimeBootDescriptor;


    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsBootOptions setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public NutsClassLoaderNode getRuntimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }

    public NutsBootOptions setRuntimeBootDependencyNode(NutsClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public List<NutsDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public NutsBootOptions setExtensionBootDescriptors(List<NutsDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    public List<NutsClassLoaderNode> getExtensionBootDependencyNodes() {
        return extensionBootDependencyNodes;
    }

    public NutsBootOptions setExtensionBootDependencyNodes(List<NutsClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = extensionBootDependencyNodes;
        return this;
    }

    public NutsBootWorkspaceFactory getBootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }

    public NutsBootOptions setBootWorkspaceFactory(NutsBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public List<URL> getClassWorldURLs() {
        return classWorldURLs;
    }

    public NutsBootOptions setClassWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = PrivateNutsUtilCollections.nonNullList(classWorldURLs);
        return this;
    }

    public ClassLoader getClassWorldLoader() {
        return classWorldLoader;
    }

    public NutsBootOptions setClassWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsBootOptions setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Set<String> getExtensionsSet() {
        return extensionsSet;
    }

    public NutsBootOptions setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = extensionsSet;
        return this;
    }

    public NutsDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public NutsBootOptions setRuntimeBootDescriptor(NutsDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }
}
