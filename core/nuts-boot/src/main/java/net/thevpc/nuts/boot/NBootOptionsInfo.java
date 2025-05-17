/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.io.InputStream;
import java.io.PrintStream;
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
public final class NBootOptionsInfo {

    private static final long serialVersionUID = 1;
//    /**
//     * bootRepositories list (; separated) where to look for runtime
//     * dependencies special
//     */
//    private String bootRepositories;
    /**
     * special
     */
    private NBootClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private List<NBootDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private List<NBootClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private NBootWorkspaceFactory bootWorkspaceFactory;

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
    private NBootDescriptor runtimeBootDescriptor;

    /// /////////////////////////////////
    ///

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> outputFormatOptions;

    private List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private String apiVersion;

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
    private Boolean installCompanions;

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
     * if true consider system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean system;

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

    private List<String> bootRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String userName;

    /**
     * option-type : runtime
     */
    private Boolean sharedInstance;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private char[] credentials;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String terminalMode;

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
    private NBootLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Instant creationTime;

    /**
     * if true no real execution,
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean dry;

    /**
     * if true show exception stacktrace
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean showStacktrace;

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
     * @since 0.8.5
     * reset ALL workspaces
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean resetHard;

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
    private String executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private String runAs;

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
    private Map<String, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NBootHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String storeLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String storeStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String repositoryStoreStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String fetchStrategy;

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
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean previewRepo;

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
    private List<String> errors;
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
    private String isolationLevel;
    private String desktopLauncher;
    private String menuLauncher;
    private String userLauncher;


    public String getDesktopLauncher() {
        return desktopLauncher;
    }


    public String getMenuLauncher() {
        return menuLauncher;
    }


    public String getUserLauncher() {
        return userLauncher;
    }


    public NBootOptionsInfo setInitLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }


    public NBootOptionsInfo setInitScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }


    public NBootOptionsInfo setInitPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }


    public NBootOptionsInfo setInitJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }


    public NBootOptionsInfo setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }


    public NBootOptionsInfo setDesktopLauncher(String desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }


    public NBootOptionsInfo setMenuLauncher(String menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }


    public NBootOptionsInfo setUserLauncher(String userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }


    public NBootOptionsInfo copy() {
        return new NBootOptionsInfo().copyFrom(this);
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

    public NBootOptionsInfo setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }


    public List<String> getApplicationArguments() {
        return applicationArguments;
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
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

    public NBootOptionsInfo setArchetype(String archetype) {
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

    public NBootOptionsInfo setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }


    public String getConfirm() {
        return confirm;
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setConfirm(String confirm) {
        this.confirm = confirm;
        return this;
    }


    public Boolean getDry() {
        return dry;
    }


    public Boolean getShowStacktrace() {
        return showStacktrace;
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    /**
     * set dry
     *
     * @param showStacktrace showStacktrace
     * @return {@code this} instance
     * @since 0.8.4
     */

    public NBootOptionsInfo setShowStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }


    public Instant getCreationTime() {
        return creationTime;
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setCreationTime(Instant creationTime) {
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

    public NBootOptionsInfo setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }


    public String getExecutionType() {
        return executionType;
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setExecutionType(String executionType) {
        this.executionType = executionType;
        return this;
    }


    public String getRunAs() {
        return runAs;
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NBootOptionsInfo setRunAs(String runAs) {
        this.runAs = runAs;
        return this;
    }


    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }


    public String getHomeLocation(NBootHomeLocation location) {
        return homeLocations == null ? null : homeLocations.get(location);
    }


    public Map<NBootHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }


    public NBootOptionsInfo setHomeLocations(Map<NBootHomeLocation, String> homeLocations) {
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


    public String getJavaCommand() {
        return javaCommand;
    }


    public NBootOptionsInfo setJavaCommand(String javaCommand) {
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

    public NBootOptionsInfo setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }


    public NBootLogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setLogConfig(NBootLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
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

    public NBootOptionsInfo setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }


    public String getOpenMode() {
        return openMode;
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setOpenMode(String openMode) {
        this.openMode = openMode;
        return this;
    }


    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setOutputFormat(String outputFormat) {
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

    public NBootOptionsInfo setOutputFormatOptions(List<String> options) {
        if (options != null) {
            if (outputFormatOptions == null) {
                outputFormatOptions = new ArrayList<>();
            }
            this.outputFormatOptions.clear();
            return addOutputFormatOptions(NBootUtils.nonNullList(options).toArray(new String[0]));
        } else {
            this.outputFormatOptions = null;
        }
        return this;
    }

    public NBootOptionsInfo setOutputFormatOptions(String... options) {
        if (outputFormatOptions == null) {
            outputFormatOptions = new ArrayList<>();
        }
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

    public NBootOptionsInfo setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }


    public String getRepositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setRepositoryStoreStrategy(String repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
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

    public NBootOptionsInfo setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }


    public String getStoreType(String folder) {
        return storeLocations == null ? null : storeLocations.get(NBootUtils.enumId(folder));
    }


    public String getStoreLayout() {
        return storeLayout;
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setStoreLayout(String storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }


    public String getStoreStrategy() {
        return storeStrategy;
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setStoreStrategy(String storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }


    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }


    public NBootOptionsInfo setStoreLocations(Map<String, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(NBootUtils.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return this;
    }


    public String getTerminalMode() {
        return terminalMode;
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setTerminalMode(String terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }


    public List<String> getRepositories() {
        return repositories;
    }

    public List<String> getBootRepositories() {
        return bootRepositories;
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setRepositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    /**
     * set initRepositories
     *
     * @param bootRepositories new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setBootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
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

    public NBootOptionsInfo setWorkspace(String workspace) {
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

    public NBootOptionsInfo setDebug(String debug) {
        this.debug = debug;
        return this;
    }


    public Boolean getSystem() {
        return system;
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setSystem(Boolean system) {
        this.system = system;
        return this;
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

    public NBootOptionsInfo setGui(Boolean gui) {
        this.gui = gui;
        return this;
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

    public NBootOptionsInfo setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
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

    public NBootOptionsInfo setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
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

    public NBootOptionsInfo setRecover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    public Boolean getResetHard() {
        return resetHard;
    }

    public void setResetHard(Boolean resetHard) {
        this.resetHard = resetHard;
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

    public NBootOptionsInfo setReset(Boolean reset) {
        this.reset = reset;
        return this;
    }


    public Boolean getCommandVersion() {
        return commandVersion;
    }


    public NBootOptionsInfo setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }


    public Boolean getCommandHelp() {
        return commandHelp;
    }


    public NBootOptionsInfo setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }


    public Boolean getInstallCompanions() {
        return installCompanions;
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setInstallCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
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

    public NBootOptionsInfo setSkipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }


    public String getOutLinePrefix() {
        return outLinePrefix;
    }


    public NBootOptionsInfo setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }


    public String getErrLinePrefix() {
        return errLinePrefix;
    }


    public NBootOptionsInfo setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
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

    public NBootOptionsInfo setSkipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
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

    public NBootOptionsInfo setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public String getProgressOptions() {
        return progressOptions;
    }


    public NBootOptionsInfo setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }


    public Boolean getCached() {
        return cached;
    }


    public NBootOptionsInfo setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }


    public Boolean getIndexed() {
        return indexed;
    }


    public NBootOptionsInfo setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }


    public Boolean getTransitive() {
        return transitive;
    }


    public NBootOptionsInfo setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }


    public Boolean getBot() {
        return bot;
    }


    public NBootOptionsInfo setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }


    public Boolean getPreviewRepo() {
        return previewRepo;
    }


    public NBootOptionsInfo setPreviewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }


    public String getFetchStrategy() {
        return fetchStrategy;
    }


    public NBootOptionsInfo setFetchStrategy(String fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }


    public InputStream getStdin() {
        return stdin;
    }


    public NBootOptionsInfo setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }


    public PrintStream getStdout() {
        return stdout;
    }


    public NBootOptionsInfo setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }


    public PrintStream getStderr() {
        return stderr;
    }


    public NBootOptionsInfo setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }


    public NBootOptionsInfo setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }


    public Instant getExpireTime() {
        return expireTime;
    }


    public NBootOptionsInfo setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }


    public Boolean getSkipErrors() {
        return skipErrors;
    }


    public NBootOptionsInfo setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }


    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    public NBootOptionsInfo setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }


    public List<String> getErrors() {
        return errors;
    }


    public NBootOptionsInfo setErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }


    public List<String> getCustomOptions() {
        return customOptions;
    }


    public NBootOptionsInfo setCustomOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }


    public String getLocale() {
        return locale;
    }


    public NBootOptionsInfo setLocale(String locale) {
        this.locale = locale;
        return this;
    }


    public String getTheme() {
        return theme;
    }


    public NBootOptionsInfo setTheme(String theme) {
        this.theme = theme;
        return this;
    }


    public NBootOptionsInfo copyFrom(NBootOptionsInfo other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion());
        this.setRuntimeId(other.getRuntimeId());
        this.setJavaCommand(other.getJavaCommand());
        this.setJavaOptions(other.getJavaOptions());
        this.setWorkspace(other.getWorkspace());
        this.setName(other.getName());
        this.setInstallCompanions(other.getInstallCompanions());
        this.setSkipWelcome(other.getSkipWelcome());
        this.setSkipBoot(other.getSkipBoot());
        this.setSystem(other.getSystem());
        this.setGui(other.getGui());
        this.setUserName(other.getUserName());
        this.setCredentials(other.getCredentials());
        this.setTerminalMode(other.getTerminalMode());
        this.setReadOnly(other.getReadOnly());
        this.setTrace(other.getTrace());
        this.setProgressOptions(other.getProgressOptions());
        this.setLogConfig(other.getLogConfig());
        this.setConfirm(other.getConfirm());
        this.setConfirm(other.getConfirm());
        this.setOutputFormat(other.getOutputFormat());
        this.setOutputFormatOptions(other.getOutputFormatOptions());
        this.setOpenMode(other.getOpenMode());
        this.setCreationTime(other.getCreationTime());
        this.setDry(other.getDry());
        this.setShowStacktrace(other.getShowStacktrace());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier());
        this.setExecutorOptions(other.getExecutorOptions());
        this.setRecover(other.getRecover());
        this.setReset(other.getReset());
        this.setResetHard(other.getResetHard());
        this.setCommandVersion(other.getCommandVersion());
        this.setCommandHelp(other.getCommandHelp());
        this.setDebug(other.getDebug());
        this.setInherited(other.getInherited());
        this.setExecutionType(other.getExecutionType());
        this.setRunAs(other.getRunAs());
        this.setArchetype(other.getArchetype());
        this.setStoreStrategy(other.getStoreStrategy());
        this.setHomeLocations(other.getHomeLocations());
        this.setStoreLocations(other.getStoreLocations());
        this.setStoreLayout(other.getStoreLayout());
        this.setStoreStrategy(other.getStoreStrategy());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy());
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

        this.setExcludedExtensions(other.getExcludedExtensions());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(other.getRepositories());
        this.setBootRepositories(other.getBootRepositories());
        this.setApplicationArguments(other.getApplicationArguments());
        this.setCustomOptions(other.getCustomOptions());
        this.setExpireTime(other.getExpireTime());
        this.setErrors(other.getErrors());
        this.setSkipErrors(other.getSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        this.setLocale(other.getLocale());
        this.setTheme(other.getTheme());
        this.setDependencySolver(other.getDependencySolver());
        this.setIsolationLevel(other.getIsolationLevel());
        this.setInitLaunchers(other.getInitLaunchers());
        this.setInitJava(other.getInitJava());
        this.setInitScripts(other.getInitScripts());
        this.setInitPlatforms(other.getInitPlatforms());
        this.setDesktopLauncher(other.getDesktopLauncher());
        this.setMenuLauncher(other.getMenuLauncher());
        this.setUserLauncher(other.getUserLauncher());
        this.setSharedInstance(other.getSharedInstance());
        this.setPreviewRepo(other.getPreviewRepo());
//        this.setBootRepositories(other.getBootRepositories());
        this.setRuntimeBootDependencyNode(other.getRuntimeBootDependencyNode());
        this.setExtensionBootDescriptors(other.getExtensionBootDescriptors());
        this.setExtensionBootDependencyNodes(other.getExtensionBootDependencyNodes());
        this.setBootWorkspaceFactory(other.getBootWorkspaceFactory());
        this.setClassWorldURLs(other.getClassWorldURLs());
        this.setClassWorldLoader(other.getClassWorldLoader());
        this.setUuid(other.getUuid());
        this.setExtensionsSet(other.getExtensionsSet());
        this.setRuntimeBootDescriptor(other.getRuntimeBootDescriptor());
        return this;
    }


    public NBootOptionsInfo setAllPresent(NBootOptionsInfo o) {
        if (o != null) {
            if (o.getApiVersion() != null) {
                this.setApiVersion(o.getApiVersion());
            }
            if (o.getRuntimeId() != null) {
                this.setRuntimeId(o.getRuntimeId());
            }
            if (o.getJavaCommand() != null) {
                this.setJavaCommand(o.getJavaCommand());
            }
            if (o.getJavaOptions() != null) {
                this.setJavaOptions(o.getJavaOptions());
            }
            if (o.getWorkspace() != null) {
                this.setWorkspace(o.getWorkspace());
            }
            if (o.getName() != null) {
                this.setName(o.getName());
            }
            if (o.getInstallCompanions() != null) {
                this.setInstallCompanions(o.getInstallCompanions());
            }
            if (o.getSkipWelcome() != null) {
                this.setSkipWelcome(o.getSkipWelcome());
            }
            if (o.getSkipBoot() != null) {
                this.setSkipBoot(o.getSkipBoot());
            }
            if (o.getSystem() != null) {
                this.setSystem(o.getSystem());
            }
            if (o.getGui() != null) {
                this.setGui(o.getGui());
            }
            if (o.getUserName() != null) {
                this.setUserName(o.getUserName());
            }
            if (o.getCredentials() != null) {
                this.setCredentials(o.getCredentials());
            }
            if (o.getTerminalMode() != null) {
                this.setTerminalMode(o.getTerminalMode());
            }
            if (o.getReadOnly() != null) {
                this.setReadOnly(o.getReadOnly());
            }
            if (o.getTrace() != null) {
                this.setTrace(o.getTrace());
            }
            if (o.getProgressOptions() != null) {
                this.setProgressOptions(o.getProgressOptions());
            }
            if (o.getLogConfig() != null) {
                this.setLogConfig(o.getLogConfig());
            }
            if (o.getConfirm() != null) {
                this.setConfirm(o.getConfirm());
            }
            if (o.getConfirm() != null) {
                this.setConfirm(o.getConfirm());
            }
            if (o.getOutputFormat() != null) {
                this.setOutputFormat(o.getOutputFormat());
            }
            if (o.getOutputFormatOptions() != null) {
                this.setOutputFormatOptions(o.getOutputFormatOptions());
            }
            if (o.getOpenMode() != null) {
                this.setOpenMode(o.getOpenMode());
            }
            if (o.getCreationTime() != null) {
                this.setCreationTime(o.getCreationTime());
            }
            if (o.getDry() != null) {
                this.setDry(o.getDry());
            }
            if (o.getShowStacktrace() != null) {
                this.setShowStacktrace(o.getShowStacktrace());
            }
            if (o.getClassLoaderSupplier() != null) {
                this.setClassLoaderSupplier(o.getClassLoaderSupplier());
            }
            if (o.getExecutorOptions() != null) {
                this.setExecutorOptions(o.getExecutorOptions());
            }
            if (o.getRecover() != null) {
                this.setRecover(o.getRecover());
            }
            if (o.getReset() != null) {
                this.setReset(o.getReset());
            }
            if (o.getResetHard() != null) {
                this.setResetHard(o.getResetHard());
            }
            if (o.getCommandVersion() != null) {
                this.setCommandVersion(o.getCommandVersion());
            }
            if (o.getCommandHelp() != null) {
                this.setCommandHelp(o.getCommandHelp());
            }
            if (o.getDebug() != null) {
                this.setDebug(o.getDebug());
            }
            if (o.getInherited() != null) {
                this.setInherited(o.getInherited());
            }
            if (o.getExecutionType() != null) {
                this.setExecutionType(o.getExecutionType());
            }
            if (o.getRunAs() != null) {
                this.setRunAs(o.getRunAs());
            }
            if (o.getArchetype() != null) {
                this.setArchetype(o.getArchetype());
            }
            if (o.getStoreStrategy() != null) {
                this.setStoreStrategy(o.getStoreStrategy());
            }
            if (o.getHomeLocations() != null) {
                this.setHomeLocations(o.getHomeLocations());
            }

            if (o.getStoreLocations() != null) {
                this.setStoreLocations(o.getStoreLocations());
            }
            if (o.getStoreLayout() != null) {
                this.setStoreLayout(o.getStoreLayout());
            }
            if (o.getStoreStrategy() != null) {
                this.setStoreStrategy(o.getStoreStrategy());
            }
            if (o.getRepositoryStoreStrategy() != null) {
                this.setRepositoryStoreStrategy(o.getRepositoryStoreStrategy());
            }
            if (o.getFetchStrategy() != null) {
                this.setFetchStrategy(o.getFetchStrategy());
            }
            if (o.getCached() != null) {
                this.setCached(o.getCached());
            }
            if (o.getIndexed() != null) {
                this.setIndexed(o.getIndexed());
            }
            if (o.getTransitive() != null) {
                this.setTransitive(o.getTransitive());
            }
            if (o.getBot() != null) {
                this.setBot(o.getBot());
            }
            if (o.getStdin() != null) {
                this.setStdin(o.getStdin());
            }
            if (o.getStdout() != null) {
                this.setStdout(o.getStdout());
            }
            if (o.getStderr() != null) {
                this.setStderr(o.getStderr());
            }
            if (o.getExecutorService() != null) {
                this.setExecutorService(o.getExecutorService());
            }
            if (o.getExcludedExtensions() != null) {
                this.setExcludedExtensions(o.getExcludedExtensions());
            }
            if (o.getRepositories() != null) {
                this.setRepositories(o.getRepositories());
            }
            if (o.getBootRepositories() != null) {
                this.setBootRepositories(o.getBootRepositories());
            }
            if (o.getApplicationArguments() != null) {
                this.setApplicationArguments(o.getApplicationArguments());
            }
            if (o.getCustomOptions() != null) {
                this.setCustomOptions(o.getCustomOptions());
            }
            if (o.getExpireTime() != null) {
                this.setExpireTime(o.getExpireTime());
            }
            if (o.getErrors() != null) {
                this.setErrors(o.getErrors());
            }
            if (o.getSkipErrors() != null) {
                this.setSkipErrors(o.getSkipErrors());
            }
            if (o.getSwitchWorkspace() != null) {
                this.setSwitchWorkspace(o.getSwitchWorkspace());
            }
            if (o.getLocale() != null) {
                this.setLocale(o.getLocale());
            }
            if (o.getTheme() != null) {
                this.setTheme(o.getTheme());
            }
            if (o.getDependencySolver() != null) {
                this.setDependencySolver(o.getDependencySolver());
            }
            if (o.getIsolationLevel() != null) {
                this.setIsolationLevel(o.getIsolationLevel());
            }
            if (o.getInitLaunchers() != null) {
                this.setInitLaunchers(o.getInitLaunchers());
            }
            if (o.getInitJava() != null) {
                this.setInitJava(o.getInitJava());
            }
            if (o.getInitScripts() != null) {
                this.setInitScripts(o.getInitScripts());
            }
            if (o.getInitLaunchers() != null) {
                this.setInitLaunchers(o.getInitLaunchers());
            }
            if (o.getDesktopLauncher() != null) {
                this.setDesktopLauncher(o.getDesktopLauncher());
            }
            if (o.getMenuLauncher() != null) {
                this.setMenuLauncher(o.getMenuLauncher());
            }
            if (o.getUserLauncher() != null) {
                this.setUserLauncher(o.getUserLauncher());
            }
            if (o.getPreviewRepo() != null) {
                this.setPreviewRepo(o.getPreviewRepo());
            }
            if (o.getSharedInstance() != null) {
                this.setSharedInstance(o.getSharedInstance());
            }
//            if (o.getBootRepositories() != null) {
//                setBootRepositories(o.getBootRepositories());
//            }
            if (o.getRuntimeBootDependencyNode() != null) {
                setRuntimeBootDependencyNode(o.getRuntimeBootDependencyNode());
            }
            if (o.getExtensionBootDescriptors() != null) {
                setExtensionBootDescriptors(o.getExtensionBootDescriptors());
            }
            if (o.getExtensionBootDependencyNodes() != null) {
                setExtensionBootDependencyNodes(o.getExtensionBootDependencyNodes());
            }
            if (o.getBootWorkspaceFactory() != null) {
                setBootWorkspaceFactory(o.getBootWorkspaceFactory());
            }
            if (o.getClassWorldURLs() != null) {
                setClassWorldURLs(o.getClassWorldURLs());
            }
            if (o.getClassWorldLoader() != null) {
                setClassWorldLoader(o.getClassWorldLoader());
            }
            if (o.getUuid() != null) {
                setUuid(o.getUuid());
            }
            if (o.getExtensionsSet() != null) {
                setExtensionsSet(o.getExtensionsSet());
            }
            if (o.getRuntimeBootDescriptor() != null) {
                setRuntimeBootDescriptor(o.getRuntimeBootDescriptor());
            }
        }
        return this;
    }

//
//    public NBootOptionsBuilderBoot setCmdLine(String cmdLine) {
//        setCmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
//        return this;
//    }

    //    public NBootOptionsBuilderBoot setCmdLine(String[] args) {
//        NWorkspaceCmdLineParser.parseNutsArguments(args, this);
//        return this;
//    }
    public Boolean getSharedInstance() {
        return sharedInstance;
    }


    public NBootOptionsInfo setSharedInstance(Boolean sharedInstance) {
        this.sharedInstance = sharedInstance;
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo setUserName(String username) {
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

    public NBootOptionsInfo setStoreLocation(String location, String value) {
        if (NBootUtils.isBlank(value)) {
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

    public NBootOptionsInfo setHomeLocation(NBootHomeLocation location, String value) {
        if (NBootUtils.isBlank(value)) {
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

    public NBootOptionsInfo addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = NBootUtils.trim(option);
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


    public String getDependencySolver() {
        return dependencySolver;
    }


    public NBootOptionsInfo setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }


//    public String toString() {
//        return toCmdLine().toString();
//    }


    public String getIsolationLevel() {
        return isolationLevel;
    }


    public Boolean getInitLaunchers() {
        return initLaunchers;
    }


    public Boolean getInitScripts() {
        return initScripts;
    }


    public Boolean getInitPlatforms() {
        return initPlatforms;
    }


    public Boolean getInitJava() {
        return initJava;
    }


    public NBootOptionsInfo unsetRuntimeOptions() {
        setCommandHelp(null);
        setCommandVersion(null);
        setOpenMode(null);
        setExecutionType(null);
        setRunAs(null);
        setReset(null);
        setResetHard(null);
        setRecover(null);
        setDry(null);
        setShowStacktrace(null);
        setExecutorOptions(null);
        setApplicationArguments(null);
        return this;
    }


    public NBootOptionsInfo unsetCreationOptions() {
        setName(null);
        setArchetype(null);
        setStoreLayout(null);
        setStoreStrategy(null);
        setRepositoryStoreStrategy(null);
        setStoreLocations(null);
        setHomeLocations(null);
        setSwitchWorkspace(null);
        return this;
    }


    public NBootOptionsInfo unsetExportedOptions() {
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
        setSystem(null);
        setGui(null);
        setReadOnly(null);
        setTrace(null);
        setProgressOptions(null);
        setDependencySolver(null);
        setDebug(null);
        setInstallCompanions(null);
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

    /// /////////////////////////////////

//    public String getBootRepositories() {
//        return bootRepositories;
//    }
//
//
//    public NBootOptionsInfo setBootRepositories(String bootRepositories) {
//        this.bootRepositories = NBootUtils.trimToNull(bootRepositories);
//        return this;
//    }
    public NBootClassLoaderNode getRuntimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }


    public NBootOptionsInfo setRuntimeBootDependencyNode(NBootClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public List<NBootDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }


    public NBootOptionsInfo setExtensionBootDescriptors(List<NBootDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = NBootUtils.nonNullList(extensionBootDescriptors);
        return this;
    }

    public List<NBootClassLoaderNode> getExtensionBootDependencyNodes() {
        return extensionBootDependencyNodes;
    }


    public NBootOptionsInfo setExtensionBootDependencyNodes(List<NBootClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = NBootUtils.nonNullList(extensionBootDependencyNodes);
        return this;
    }

    public NBootWorkspaceFactory getBootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }


    public NBootOptionsInfo setBootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public List<URL> getClassWorldURLs() {
        return classWorldURLs;
    }


    public NBootOptionsInfo setClassWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = NBootUtils.nonNullList(classWorldURLs);
        return this;
    }

    public ClassLoader getClassWorldLoader() {
        return classWorldLoader;
    }


    public NBootOptionsInfo setClassWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public String getUuid() {
        return uuid;
    }


    public NBootOptionsInfo setUuid(String uuid) {
        this.uuid = NBootUtils.trimToNull(uuid);
        return this;
    }

    public Set<String> getExtensionsSet() {
        return extensionsSet;
    }


    public NBootOptionsInfo setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = NBootUtils.nonNullSet(extensionsSet);
        return this;
    }

    public NBootDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }


    public NBootOptionsInfo setRuntimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }
}
