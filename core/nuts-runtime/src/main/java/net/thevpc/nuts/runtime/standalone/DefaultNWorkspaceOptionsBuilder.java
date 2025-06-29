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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.cmdline.NWorkspaceCmdLineParser;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Workspace creation/opening options class.
 * <p>
 * %category Config
 *
 * @since 0.5.4
 */
public class DefaultNWorkspaceOptionsBuilder implements NWorkspaceOptionsBuilder {

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
    private NVersion apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * nuts runtime id (or version) to boot option-type : exported (inherited iton
     * child workspaces)
     */
    private NId runtimeId;

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
    private NTerminalMode terminalMode;

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
    private NLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NOpenMode openMode;

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
    private NExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private NRunAs runAs;

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
    private Map<NStoreType, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NOsFamily storeLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NStoreStrategy storeStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NStoreStrategy repositoryStoreStrategy;


    /**
     * repositories used for the creation of the workspace.
     * irrelevant if the workspace is alreayd created
     * option-type : create (inherited in child workspaces)
     */
    private List<String> bootRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NFetchStrategy fetchStrategy;

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
    private List<NMsg> errors;
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
    private NIsolationLevel isolationLevel;
    private NSupportMode desktopLauncher;
    private NSupportMode menuLauncher;
    private NSupportMode userLauncher;

    public DefaultNWorkspaceOptionsBuilder() {

    }

    @Override
    public NWorkspaceOptionsBuilder resetOptions() {
        outputFormatOptions = null;
        customOptions = null;
        apiVersion = null;
        runtimeId = null;
        javaCommand = null;
        javaOptions = null;
        workspace = null;
        outLinePrefix = null;
        errLinePrefix = null;
        name = null;
        installCompanions = null;
        skipWelcome = null;
        skipBoot = null;
        system = null;
        gui = null;
        excludedExtensions = null;
        repositories = null;
        userName = null;
        sharedInstance = null;
        credentials = null;
        terminalMode = null;
        readOnly = null;
        trace = null;
        progressOptions = null;
        dependencySolver = null;
        logConfig = null;
        confirm = null;
        outputFormat = null;
        applicationArguments = null;
        openMode = null;
        creationTime = null;
        dry = null;
        showStacktrace = null;
        classLoaderSupplier = null;
        executorOptions = null;
        recover = null;
        reset = null;
        resetHard = null;
        commandHelp = null;
        debug = null;
        inherited = null;
        executionType = null;
        runAs = null;
        archetype = null;
        switchWorkspace = null;
        storeLocations = null;
        homeLocations = null;
        storeLayout = null;
        storeStrategy = null;
        repositoryStoreStrategy = null;
        bootRepositories = null;
        fetchStrategy = null;
        cached = null;
        indexed = null;
        transitive = null;
        bot = null;
        previewRepo = null;
        stdin = null;
        stdout = null;
        stderr = null;
        executorService = null;
        expireTime = null;
        //errors are not rest
        //errors = null;
        skipErrors = null;
        locale = null;
        theme = null;
        initLaunchers = null;
        initScripts = null;
        initPlatforms = null;
        initJava = null;
        isolationLevel = null;
        desktopLauncher = null;
        menuLauncher = null;
        userLauncher = null;
        return this;
    }

    @Override
    public NOptional<NSupportMode> getDesktopLauncher() {
        return NOptional.ofNamed(desktopLauncher, "desktopLauncher");
    }

    @Override
    public NOptional<NSupportMode> getMenuLauncher() {
        return NOptional.ofNamed(menuLauncher, "menuLauncher");
    }

    @Override
    public NOptional<NSupportMode> getUserLauncher() {
        return NOptional.ofNamed(userLauncher, "userLauncher");
    }

    @Override
    public NWorkspaceOptionsBuilder setInitLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setInitScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setInitPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setInitJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setIsolationLevel(NIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setDesktopLauncher(NSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setMenuLauncher(NSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setUserLauncher(NSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copy() {
        return new DefaultNWorkspaceOptionsBuilder().copyFrom(this);
    }


    @Override
    public NOptional<NVersion> getApiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setApiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NOptional<List<String>> getApplicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NOptional<String> getArchetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public NOptional<Supplier<ClassLoader>> getClassLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NOptional<NConfirmationMode> getConfirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setConfirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NOptional<Boolean> getDry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> getShowStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setDry(Boolean dry) {
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
    @Override
    public NWorkspaceOptionsBuilder setShowStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    @Override
    public NOptional<Instant> getCreationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public NOptional<List<String>> getExcludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public NOptional<NExecutionType> getExecutionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NOptional<NRunAs> getRunAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NWorkspaceOptionsBuilder setRunAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NOptional<List<String>> getExecutorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "homeLocations[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> getHomeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NWorkspaceOptionsBuilder setHomeLocations(Map<NHomeLocation, String> homeLocations) {
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
    public NOptional<String> getJavaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NWorkspaceOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NOptional<String> getJavaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public NOptional<NLogConfig> getLogConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setLogConfig(NLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }

    @Override
    public NOptional<String> getName() {
        return NOptional.ofNamed(name, "name");
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public NOptional<NOpenMode> getOpenMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setOpenMode(NOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NOptional<NContentType> getOutputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setOutputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NOptional<List<String>> getOutputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setOutputFormatOptions(List<String> options) {
        if (options != null) {
            if (outputFormatOptions == null) {
                outputFormatOptions = new ArrayList<>();
            }
            this.outputFormatOptions.clear();
            return addOutputFormatOptions(NReservedLangUtils.nonNullList(options).toArray(new String[0]));
        } else {
            this.outputFormatOptions = null;
        }
        return this;
    }

    public NWorkspaceOptionsBuilder setOutputFormatOptions(String... options) {
        if (outputFormatOptions == null) {
            outputFormatOptions = new ArrayList<>();
        }
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NOptional<char[]> getCredentials() {
        return NOptional.ofNamed(credentials, "credentials");
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> getRepositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    @Override
    public NOptional<NId> getRuntimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setRuntimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> getStoreLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setStoreLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> getStoreStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    @Override
    public NOptional<Map<NStoreType, String>> getStoreLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NWorkspaceOptionsBuilder setStoreLocations(Map<NStoreType, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(NReservedLangUtils.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return this;
    }

    @Override
    public NOptional<NTerminalMode> getTerminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setTerminalMode(NTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NOptional<List<String>> getRepositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }

    @Override
    public NOptional<List<String>> getBootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setRepositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    /**
     * set initRepositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setBootRepositories(List<String> repositories) {
        this.bootRepositories = repositories;
        return this;
    }

    @Override
    public NOptional<String> getUserName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> getWorkspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NOptional<String> getDebug() {
        return NOptional.ofNamed(debug, "debug");
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NOptional<Boolean> getSystem() {
        return NOptional.ofNamed(system, "system");
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setSystem(Boolean system) {
        this.system = system;
        return this;
    }

    @Override
    public NOptional<Boolean> getGui() {
        return NOptional.ofNamed(gui, "gui");
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public NOptional<Boolean> getInherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NOptional<Boolean> getReadOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public NOptional<Boolean> getRecover() {
        return NOptional.ofNamed(recover, "recover");
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setRecover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public NOptional<Boolean> getReset() {
        return NOptional.ofNamed(reset, "reset");
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setReset(Boolean reset) {
        this.reset = reset;
        return this;
    }


    @Override
    public NOptional<Boolean> getResetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }

    @Override
    public NWorkspaceOptionsBuilder setResetHard(Boolean resetHard) {
        this.resetHard = resetHard;
        return this;
    }


    @Override
    public NOptional<Boolean> getCommandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NOptional<Boolean> getCommandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NOptional<Boolean> getInstallCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setInstallCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setSkipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NOptional<String> getOutLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<String> getErrLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setSkipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    @Override
    public NOptional<Boolean> getTrace() {
        return NOptional.ofNamed(trace, "trace");
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public NOptional<String> getProgressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> getCached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NWorkspaceOptionsBuilder setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NOptional<Boolean> getIndexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NWorkspaceOptionsBuilder setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NOptional<Boolean> getTransitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NWorkspaceOptionsBuilder setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NOptional<Boolean> getBot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NWorkspaceOptionsBuilder setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NOptional<Boolean> getPreviewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    @Override
    public NWorkspaceOptionsBuilder setPreviewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }

    @Override
    public NOptional<NFetchStrategy> getFetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NWorkspaceOptionsBuilder setFetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NOptional<InputStream> getStdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NWorkspaceOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NWorkspaceOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NWorkspaceOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NOptional<ExecutorService> getExecutorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NOptional<Instant> getExpireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NWorkspaceOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NWorkspaceOptionsBuilder setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NOptional<Boolean> getSwitchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    public NWorkspaceOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NOptional<List<NMsg>> getErrors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NWorkspaceOptionsBuilder setErrors(List<NMsg> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NOptional<List<String>> getCustomOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder setCustomOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NOptional<String> getLocale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NWorkspaceOptionsBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NOptional<String> getTheme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NWorkspaceOptionsBuilder setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setInstallCompanions(other.getInstallCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setSystem(other.getSystem().orNull());
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
        this.setShowStacktrace(other.getShowStacktrace().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setResetHard(other.getResetHard().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLayout(other.getStoreLayout().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
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
        this.setIsolationLevel(other.getIsolationLevel().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitPlatforms(other.getInitPlatforms().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        this.setSharedInstance(other.getSharedInstance().orNull());
        this.setPreviewRepo(other.getPreviewRepo().orNull());
        return this;
    }


    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptionsBuilder other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setInstallCompanions(other.getInstallCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setSystem(other.getSystem().orNull());
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
        this.setShowStacktrace(other.getShowStacktrace().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setResetHard(other.getResetHard().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLayout(other.getStoreLayout().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
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
        this.setIsolationLevel(other.getIsolationLevel().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitPlatforms(other.getInitPlatforms().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        this.setSharedInstance(other.getSharedInstance().orNull());
        this.setPreviewRepo(other.getPreviewRepo().orNull());
        return this;
    }

    public NBootOptionsInfo toBootOptionsInfo() {
        return build().toBootOptionsInfo();
    }

    public NWorkspaceOptionsBuilder copyFrom(NBootOptionsInfo other) {
        this.setApiVersion(other.getApiVersion() == null ? null : NVersion.get(other.getApiVersion()).orNull());
        this.setRuntimeId(other.getRuntimeId() == null ? null :
                other.getRuntimeId().contains("#") ? NId.get(other.getRuntimeId()).orNull() :
                        NId.getRuntime(other.getRuntimeId()).orNull()
        );
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
        this.setTerminalMode(NTerminalMode.parse(other.getTerminalMode()).orNull());
        this.setReadOnly(other.getReadOnly());
        this.setTrace(other.getTrace());
        this.setProgressOptions(other.getProgressOptions());
        {
            NBootLogConfig c = other.getLogConfig();
            NLogConfig v = null;
            if (c != null) {
                v = new NLogConfig();
                v.setLogFileBase(c.getLogFileBase());
                v.setLogFileLevel(c.getLogFileLevel());
                v.setLogFileFilter(c.getLogFileFilter());
                v.setLogTermLevel(c.getLogTermLevel());
                v.setLogTermFilter(c.getLogTermFilter());
                v.setLogFileSize(c.getLogFileSize());
                v.setLogFileCount(c.getLogFileCount());
                v.setLogFileName(c.getLogFileName());
                v.setLogFileBase(c.getLogFileBase());
            }
            this.setLogConfig(v);
        }
        this.setConfirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.setConfirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.setOutputFormat(NContentType.parse(other.getOutputFormat()).orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions());
        this.setOpenMode(NOpenMode.parse(other.getOpenMode()).orNull());
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
        this.setExecutionType(NExecutionType.parse(other.getExecutionType()).orNull());
        this.setRunAs(NRunAs.parse(other.getRunAs()).orNull());
        this.setArchetype(other.getArchetype());
        this.setStoreStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        {
            Map<NBootHomeLocation, String> c = other.getHomeLocations();
            Map<NHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NBootHomeLocation, String> e : c.entrySet()) {
                    v.put(NHomeLocation.of(
                            NOsFamily.parse(e.getKey().getOsFamily()).get(),
                            NStoreType.parse(e.getKey().getStoreLocation()).get()
                    ), e.getValue());
                }
            }
            this.setHomeLocations(v);
        }
        {
            Map<String, String> c = other.getStoreLocations();
            Map<NStoreType, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<String, String> e : c.entrySet()) {
                    v.put(NStoreType.parse(e.getKey()).get(), e.getValue());
                }
            }
            this.setStoreLocations(v);
        }
        this.setStoreLayout(NOsFamily.parse(other.getStoreLayout()).orNull());
        this.setStoreStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        this.setRepositoryStoreStrategy(NStoreStrategy.parse(other.getRepositoryStoreStrategy()).orNull());
        this.setFetchStrategy(NFetchStrategy.parse(other.getFetchStrategy()).orNull());
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
        this.setErrors(other.getErrors() == null ? new ArrayList<>() : other.getErrors().stream().map(x -> NMsg.ofPlain(x)).collect(Collectors.toList()));
        this.setSkipErrors(other.getSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        this.setLocale(other.getLocale());
        this.setTheme(other.getTheme());
        this.setDependencySolver(other.getDependencySolver());
        this.setIsolationLevel(NIsolationLevel.parse(other.getIsolationLevel()).orNull());
        this.setInitLaunchers(other.getInitLaunchers());
        this.setInitJava(other.getInitJava());
        this.setInitScripts(other.getInitScripts());
        this.setInitPlatforms(other.getInitPlatforms());
        this.setDesktopLauncher(NSupportMode.parse(other.getDesktopLauncher()).orNull());
        this.setMenuLauncher(NSupportMode.parse(other.getMenuLauncher()).orNull());
        this.setUserLauncher(NSupportMode.parse(other.getUserLauncher()).orNull());
        this.setSharedInstance(other.getSharedInstance());
        this.setPreviewRepo(other.getPreviewRepo());
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other, NMapStrategy strategy) {
        if (other != null) {
            if (strategy == null) {
                strategy = NMapStrategy.ANY;
            }
            strategy.applyOptional(this::getApiVersion, other::getApiVersion, this::setApiVersion);
            strategy.applyOptional(this::getRuntimeId, other::getRuntimeId, this::setRuntimeId);
            strategy.applyOptional(this::getJavaOptions, other::getJavaOptions, this::setJavaOptions);
            strategy.applyOptional(this::getJavaCommand, other::getJavaCommand, this::setJavaCommand);
            strategy.applyOptional(this::getWorkspace, other::getWorkspace, this::setWorkspace);
            strategy.applyOptional(this::getName, other::getName, this::setName);
            strategy.applyOptional(this::getInstallCompanions, other::getInstallCompanions, this::setInstallCompanions);
            strategy.applyOptional(this::getSkipWelcome, other::getSkipWelcome, this::setSkipWelcome);
            strategy.applyOptional(this::getSkipBoot, other::getSkipBoot, this::setSkipBoot);
            strategy.applyOptional(this::getSystem, other::getSystem, this::setSystem);
            strategy.applyOptional(this::getGui, other::getGui, this::setGui);
            strategy.applyOptional(this::getUserName, other::getUserName, this::setUserName);
            strategy.applyOptional(this::getCredentials, other::getCredentials, this::setCredentials);
            strategy.applyOptional(this::getTerminalMode, other::getTerminalMode, this::setTerminalMode);
            strategy.applyOptional(this::getReadOnly, other::getReadOnly, this::setReadOnly);
            strategy.applyOptional(this::getTrace, other::getTrace, this::setTrace);
            strategy.applyOptional(this::getProgressOptions, other::getProgressOptions, this::setProgressOptions);
            strategy.applyOptional(this::getLogConfig, other::getLogConfig, this::setLogConfig);
            strategy.applyOptional(this::getConfirm, other::getConfirm, this::setConfirm);
            strategy.applyOptional(this::getConfirm, other::getConfirm, this::setConfirm);
            strategy.applyOptional(this::getOutputFormat, other::getOutputFormat, this::setOutputFormat);
            strategy.applyOptional(this::getOutputFormatOptions, other::getOutputFormatOptions, this::setOutputFormatOptions);
            strategy.applyOptional(this::getOpenMode, other::getOpenMode, this::setOpenMode);
            strategy.applyOptional(this::getCreationTime, other::getCreationTime, this::setCreationTime);
            strategy.applyOptional(this::getDry, other::getDry, this::setDry);
            strategy.applyOptional(this::getShowStacktrace, other::getShowStacktrace, this::setShowStacktrace);
            strategy.applyOptional(this::getClassLoaderSupplier, other::getClassLoaderSupplier, this::setClassLoaderSupplier);
            strategy.applyOptional(this::getExecutorOptions, other::getExecutorOptions, this::setExecutorOptions);
            strategy.applyOptional(this::getRecover, other::getRecover, this::setRecover);
            strategy.applyOptional(this::getReset, other::getReset, this::setReset);
            strategy.applyOptional(this::getResetHard, other::getResetHard, this::setResetHard);
            strategy.applyOptional(this::getCommandVersion, other::getCommandVersion, this::setCommandVersion);
            strategy.applyOptional(this::getCommandHelp, other::getCommandHelp, this::setCommandHelp);
            strategy.applyOptional(this::getDebug, other::getDebug, this::setDebug);
            strategy.applyOptional(this::getInherited, other::getInherited, this::setInherited);
            strategy.applyOptional(this::getExecutionType, other::getExecutionType, this::setExecutionType);
            strategy.applyOptional(this::getRunAs, other::getRunAs, this::setRunAs);
            strategy.applyOptional(this::getArchetype, other::getArchetype, this::setArchetype);
            strategy.applyOptional(this::getStoreStrategy, other::getStoreStrategy, this::setStoreStrategy);
            strategy.applyOptional(this::getHomeLocations, other::getHomeLocations, this::setHomeLocations);
            strategy.applyOptional(this::getStoreLocations, other::getStoreLocations, this::setStoreLocations);
            strategy.applyOptional(this::getStoreLayout, other::getStoreLayout, this::setStoreLayout);
            strategy.applyOptional(this::getStoreStrategy, other::getStoreStrategy, this::setStoreStrategy);
            strategy.applyOptional(this::getRepositoryStoreStrategy, other::getRepositoryStoreStrategy, this::setRepositoryStoreStrategy);
            strategy.applyOptional(this::getFetchStrategy, other::getFetchStrategy, this::setFetchStrategy);
            strategy.applyOptional(this::getCached, other::getCached, this::setCached);
            strategy.applyOptional(this::getIndexed, other::getIndexed, this::setIndexed);
            strategy.applyOptional(this::getTransitive, other::getTransitive, this::setTransitive);
            strategy.applyOptional(this::getBot, other::getBot, this::setBot);
            strategy.applyOptional(this::getStdin, other::getStdin, this::setStdin);
            strategy.applyOptional(this::getStdout, other::getStdout, this::setStdout);
            strategy.applyOptional(this::getStderr, other::getStderr, this::setStderr);
            strategy.applyOptional(this::getExecutorService, other::getExecutorService, this::setExecutorService);
            strategy.applyOptional(this::getExcludedExtensions, other::getExcludedExtensions, this::setExcludedExtensions);
            strategy.applyOptional(this::getRepositories, other::getRepositories, this::setRepositories);
            strategy.applyOptional(this::getBootRepositories, other::getBootRepositories, this::setBootRepositories);
            strategy.applyOptional(this::getApplicationArguments, other::getApplicationArguments, this::setApplicationArguments);
            strategy.applyOptional(this::getCustomOptions, other::getCustomOptions, this::setCustomOptions);
            strategy.applyOptional(this::getExpireTime, other::getExpireTime, this::setExpireTime);
            strategy.applyOptional(this::getErrors, other::getErrors, this::setErrors);
            strategy.applyOptional(this::getSkipErrors, other::getSkipErrors, this::setSkipErrors);
            strategy.applyOptional(this::getSwitchWorkspace, other::getSwitchWorkspace, this::setSwitchWorkspace);
            strategy.applyOptional(this::getLocale, other::getLocale, this::setLocale);
            strategy.applyOptional(this::getTheme, other::getTheme, this::setTheme);
            strategy.applyOptional(this::getDependencySolver, other::getDependencySolver, this::setDependencySolver);
            strategy.applyOptional(this::getIsolationLevel, other::getIsolationLevel, this::setIsolationLevel);
            strategy.applyOptional(this::getInitLaunchers, other::getInitLaunchers, this::setInitLaunchers);
            strategy.applyOptional(this::getInitJava, other::getInitJava, this::setInitJava);
            strategy.applyOptional(this::getInitScripts, other::getInitScripts, this::setInitScripts);
            strategy.applyOptional(this::getInitLaunchers, other::getInitLaunchers, this::setInitLaunchers);
            strategy.applyOptional(this::getDesktopLauncher, other::getDesktopLauncher, this::setDesktopLauncher);
            strategy.applyOptional(this::getMenuLauncher, other::getMenuLauncher, this::setMenuLauncher);
            strategy.applyOptional(this::getUserLauncher, other::getUserLauncher, this::setUserLauncher);
            strategy.applyOptional(this::getPreviewRepo, other::getPreviewRepo, this::setPreviewRepo);
            strategy.applyOptional(this::getSharedInstance, other::getSharedInstance, this::setSharedInstance);
        }
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setCmdLine(String cmdLine) {
        setCmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setCmdLine(String[] args) {
        NWorkspaceCmdLineParser.parseNutsArguments(args, this);
        return this;
    }

    public NOptional<Boolean> getSharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
    }

    @Override
    public NWorkspaceOptionsBuilder setSharedInstance(Boolean sharedInstance) {
        this.sharedInstance = sharedInstance;
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder setUserName(String username) {
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
    public NWorkspaceOptionsBuilder setStoreLocation(NStoreType location, String value) {
        if (NBlankable.isBlank(value)) {
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
    public NWorkspaceOptionsBuilder setHomeLocation(NHomeLocation location, String value) {
        if (NBlankable.isBlank(value)) {
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
    public NWorkspaceOptionsBuilder addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = NStringUtils.trim(option);
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
    public NOptional<String> getDependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public NWorkspaceOptionsBuilder setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    @Override
    public String toString() {
        return toCmdLine().toString();
    }

    @Override
    public NWorkspaceOptions build() {
        return new DefaultNWorkspaceOptions(
                getApiVersion().orNull(), getRuntimeId().orNull(), getWorkspace().orNull(),
                getName().orNull(), getJavaCommand().orNull(), getJavaOptions().orNull(),
                getOutLinePrefix().orNull(), getErrLinePrefix().orNull(), getUserName().orNull(),
                getCredentials().orNull(), getProgressOptions().orNull(), getDependencySolver().orNull(),
                getDebug().orNull(), getArchetype().orNull(), getLocale().orNull(), getTheme().orNull(),
                getLogConfig().orNull(), getConfirm().orNull(), getOutputFormat().orNull(), getOpenMode().orNull(),
                getExecutionType().orNull(), getStoreStrategy().orNull(), getRepositoryStoreStrategy().orNull(),
                getStoreLayout().orNull(), getTerminalMode().orNull(), getFetchStrategy().orNull(),
                getRunAs().orNull(), getCreationTime().orNull(), getExpireTime().orNull(),
                getInstallCompanions().orNull(), getSkipWelcome().orNull(), getSkipBoot().orNull(),
                getSystem().orNull(), getGui().orNull(), getReadOnly().orNull(), getTrace().orNull(),
                getDry().orNull(), getShowStacktrace().orNull(), getRecover().orNull(), getReset().orNull(), getResetHard().orNull(), getCommandVersion().orNull(),
                getCommandHelp().orNull(), getCommandHelp().orNull(), getSwitchWorkspace().orNull(), getCached().orNull(),
                getIndexed().orNull(), getTransitive().orNull(), getBot().orNull(), getSkipErrors().orNull(),
                getIsolationLevel().orNull(), getInitLaunchers().orNull(), getInitScripts().orNull(), getInitPlatforms().orNull(),
                getInitJava().orNull(), getStdin().orNull(), getStdout().orNull(), getStdout().orNull(), getExecutorService().orNull(),
                getClassLoaderSupplier().orNull(), getApplicationArguments().orNull(), getOutputFormatOptions().orNull(),
                getCustomOptions().orNull(), getExcludedExtensions().orNull(), getRepositories().orNull(), getBootRepositories().orNull(),
                getExecutorOptions().orNull(), getErrors().orNull(), getStoreLocations().orNull(), getHomeLocations().orNull(),
                getDesktopLauncher().orNull(), getMenuLauncher().orNull(), getUserLauncher().orNull()
                , getPreviewRepo().orNull()
                , getSharedInstance().orNull()
        );
    }

    @Override
    public NWorkspaceOptionsBuilder builder() {
        return new DefaultNWorkspaceOptionsBuilder().copyFrom(this);
    }

    @Override
    public NCmdLine toCmdLine() {
        return build().toCmdLine();
    }

    @Override
    public NCmdLine toCmdLine(NWorkspaceOptionsConfig config) {
        return build().toCmdLine(config);
    }

    @Override
    public NOptional<NIsolationLevel> getIsolationLevel() {
        return NOptional.ofNamed(isolationLevel, "isolationLevel");
    }

    @Override
    public NOptional<Boolean> getInitLaunchers() {
        return NOptional.ofNamed(initLaunchers, "initLaunchers");
    }

    @Override
    public NOptional<Boolean> getInitScripts() {
        return NOptional.ofNamed(initScripts, "initScripts");
    }

    @Override
    public NOptional<Boolean> getInitPlatforms() {
        return NOptional.ofNamed(initPlatforms, "initPlatforms");
    }

    @Override
    public NOptional<Boolean> getInitJava() {
        return NOptional.ofNamed(initJava, "initJava");
    }

    @Override
    public NWorkspaceOptionsBuilder unsetRuntimeOptions() {
        setCommandHelp(null);
        setCommandVersion(null);
        setOpenMode(null);
        setExecutionType(null);
        setRunAs(null);
        setReset(null);
        setRecover(null);
        setDry(null);
        setShowStacktrace(null);
        setExecutorOptions(null);
        setApplicationArguments(null);
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder unsetCreationOptions() {
        setName(null);
        setArchetype(null);
        setStoreLayout(null);
        setStoreStrategy(null);
        setRepositoryStoreStrategy(null);
        setStoreLocations(null);
        setHomeLocations(null);
        setSwitchWorkspace(null);
        setBootRepositories(null);
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder unsetExportedOptions() {
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

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
