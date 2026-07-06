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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.core.NWorkspaceCmdLineParser;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NContentType;
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
@NScore(fixed = NScorable.DEFAULT_SCORE)
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
    public NOptional<NSupportMode> desktopLauncher() {
        return NOptional.ofNamed(desktopLauncher, "desktopLauncher");
    }

    @Override
    public NOptional<NSupportMode> menuLauncher() {
        return NOptional.ofNamed(menuLauncher, "menuLauncher");
    }

    @Override
    public NOptional<NSupportMode> userLauncher() {
        return NOptional.ofNamed(userLauncher, "userLauncher");
    }

    @Override
    public NWorkspaceOptionsBuilder initLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder initScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder initPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder initJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder isolationLevel(NIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder desktopLauncher(NSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder menuLauncher(NSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder userLauncher(NSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copy() {
        return new DefaultNWorkspaceOptionsBuilder().copyFrom(this);
    }


    @Override
    public NOptional<NVersion> apiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder apiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NOptional<List<String>> applicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder applicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NOptional<String> archetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder archetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NOptional<NConfirmationMode> confirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder confirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NOptional<Boolean> dry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> showStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder dry(Boolean dry) {
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
    public NWorkspaceOptionsBuilder showStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    @Override
    public NOptional<Instant> creationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder creationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public NOptional<List<String>> excludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder excludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public NOptional<NExecutionType> executionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder executionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NOptional<NRunAs> runAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NWorkspaceOptionsBuilder runAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NOptional<List<String>> executorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder executorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "homeLocations[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NWorkspaceOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations) {
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
    public NOptional<String> javaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NWorkspaceOptionsBuilder javaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NOptional<String> javaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder javaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public NOptional<NLogConfig> logConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder logConfig(NLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }

    @Override
    public NOptional<String> name() {
        return NOptional.ofNamed(name, "name");
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder name(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public NOptional<NOpenMode> openMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder openMode(NOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NOptional<NContentType> outputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder outputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NOptional<List<String>> outputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder outputFormatOptions(List<String> options) {
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
    public NOptional<char[]> credential() {
        return NOptional.ofNamed(credentials, "credentials");
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder credential(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    @Override
    public NOptional<NId> runtimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder runtimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public NOptional<String> storeType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> storeLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder storeLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> storeStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder storeStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    @Override
    public NOptional<Map<NStoreType, String>> storeLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NWorkspaceOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations) {
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
    public NOptional<NTerminalMode> terminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder terminalMode(NTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NOptional<List<String>> repositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }

    @Override
    public NOptional<List<String>> bootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder repositories(List<String> repositories) {
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
    public NWorkspaceOptionsBuilder bootRepositories(List<String> repositories) {
        this.bootRepositories = repositories;
        return this;
    }

    @Override
    public NOptional<String> userName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> workspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NOptional<String> debug() {
        return NOptional.ofNamed(debug, "debug");
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder debug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NOptional<Boolean> system() {
        return NOptional.ofNamed(system, "system");
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder system(Boolean system) {
        this.system = system;
        return this;
    }

    @Override
    public NOptional<Boolean> gui() {
        return NOptional.ofNamed(gui, "gui");
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder gui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public NOptional<Boolean> inherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder inherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NOptional<Boolean> readOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public NOptional<Boolean> recover() {
        return NOptional.ofNamed(recover, "recover");
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder recover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public NOptional<Boolean> reset() {
        return NOptional.ofNamed(reset, "reset");
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder reset(Boolean reset) {
        this.reset = reset;
        return this;
    }


    @Override
    public NOptional<Boolean> resetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }

    @Override
    public NWorkspaceOptionsBuilder resetHard(Boolean resetHard) {
        this.resetHard = resetHard;
        return this;
    }


    @Override
    public NOptional<Boolean> commandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NWorkspaceOptionsBuilder commandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NOptional<Boolean> commandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NWorkspaceOptionsBuilder commandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NOptional<Boolean> installCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder installCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public NOptional<Boolean> skipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder skipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NOptional<String> outLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder outLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<String> errLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder errLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> skipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder skipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    @Override
    public NOptional<Boolean> trace() {
        return NOptional.ofNamed(trace, "trace");
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NWorkspaceOptionsBuilder trace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public NOptional<String> progressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder progressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> cached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NWorkspaceOptionsBuilder cached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NOptional<Boolean> indexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NWorkspaceOptionsBuilder indexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NOptional<Boolean> transitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NWorkspaceOptionsBuilder transitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NOptional<Boolean> bot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NWorkspaceOptionsBuilder bot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NOptional<Boolean> previewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    @Override
    public NWorkspaceOptionsBuilder previewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }

    @Override
    public NOptional<NFetchStrategy> fetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NWorkspaceOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NOptional<InputStream> stdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NWorkspaceOptionsBuilder stdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NOptional<PrintStream> stdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NWorkspaceOptionsBuilder stdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NOptional<PrintStream> stderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NWorkspaceOptionsBuilder stderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NOptional<ExecutorService> executorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NWorkspaceOptionsBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NOptional<Instant> expireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NWorkspaceOptionsBuilder expireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NOptional<Boolean> skipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NWorkspaceOptionsBuilder skipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NOptional<Boolean> switchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    public NWorkspaceOptionsBuilder switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NOptional<List<NMsg>> errors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NWorkspaceOptionsBuilder errors(List<NMsg> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NOptional<List<String>> customOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder customOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NOptional<String> locale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NWorkspaceOptionsBuilder locale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NOptional<String> theme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NWorkspaceOptionsBuilder theme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion().orNull());
        this.runtimeId(other.runtimeId().orNull());
        this.javaCommand(other.javaCommand().orNull());
        this.javaOptions(other.javaOptions().orNull());
        this.workspace(other.workspace().orNull());
        this.name(other.name().orNull());
        this.installCompanions(other.installCompanions().orNull());
        this.skipWelcome(other.skipWelcome().orNull());
        this.skipBoot(other.skipBoot().orNull());
        this.system(other.system().orNull());
        this.gui(other.gui().orNull());
        this.setUserName(other.userName().orNull());
        this.credential(other.credential().orNull());
        this.terminalMode(other.terminalMode().orNull());
        this.readOnly(other.readOnly().orNull());
        this.trace(other.trace().orNull());
        this.progressOptions(other.progressOptions().orNull());
        this.logConfig(other.logConfig().orNull());
        this.confirm(other.confirm().orNull());
        this.confirm(other.confirm().orNull());
        this.outputFormat(other.outputFormat().orNull());
        this.outputFormatOptions(other.outputFormatOptions().orNull());
        this.openMode(other.openMode().orNull());
        this.creationTime(other.creationTime().orNull());
        this.dry(other.dry().orNull());
        this.showStacktrace(other.showStacktrace().orNull());
        this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        this.executorOptions(other.executorOptions().orNull());
        this.recover(other.recover().orNull());
        this.reset(other.reset().orNull());
        this.resetHard(other.resetHard().orNull());
        this.commandVersion(other.commandVersion().orNull());
        this.commandHelp(other.commandHelp().orNull());
        this.debug(other.debug().orNull());
        this.inherited(other.inherited().orNull());
        this.executionType(other.executionType().orNull());
        this.runAs(other.runAs().orNull());
        this.archetype(other.archetype().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.homeLocations(other.homeLocations().orNull());
        this.storeLocations(other.storeLocations().orNull());
        this.storeLayout(other.storeLayout().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        this.fetchStrategy(other.fetchStrategy().orNull());
        this.cached(other.cached().orNull());
        this.indexed(other.indexed().orNull());
        this.transitive(other.transitive().orNull());
        this.bot(other.bot().orNull());
        this.stdin(other.stdin().orNull());
        this.stdout(other.stdout().orNull());
        this.stderr(other.stderr().orNull());
        this.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.repositories().orNull());
        this.applicationArguments(other.applicationArguments().orNull());
        this.customOptions(other.customOptions().orNull());
        this.expireTime(other.expireTime().orNull());
        this.errors(other.errors().orNull());
        this.skipErrors(other.skipErrors().orNull());
        this.switchWorkspace(other.switchWorkspace().orNull());
        this.locale(other.locale().orNull());
        this.theme(other.theme().orNull());
        this.dependencySolver(other.dependencySolver().orNull());
        this.isolationLevel(other.isolationLevel().orNull());
        this.initLaunchers(other.initLaunchers().orNull());
        this.initJava(other.initJava().orNull());
        this.initScripts(other.initScripts().orNull());
        this.initPlatforms(other.initPlatforms().orNull());
        this.desktopLauncher(other.desktopLauncher().orNull());
        this.menuLauncher(other.menuLauncher().orNull());
        this.userLauncher(other.userLauncher().orNull());
        this.sharedInstance(other.sharedInstance().orNull());
        this.previewRepo(other.previewRepo().orNull());
        return this;
    }


    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptionsBuilder other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion().orNull());
        this.runtimeId(other.runtimeId().orNull());
        this.javaCommand(other.javaCommand().orNull());
        this.javaOptions(other.javaOptions().orNull());
        this.workspace(other.workspace().orNull());
        this.name(other.name().orNull());
        this.installCompanions(other.installCompanions().orNull());
        this.skipWelcome(other.skipWelcome().orNull());
        this.skipBoot(other.skipBoot().orNull());
        this.system(other.system().orNull());
        this.gui(other.gui().orNull());
        this.setUserName(other.userName().orNull());
        this.credential(other.credential().orNull());
        this.terminalMode(other.terminalMode().orNull());
        this.readOnly(other.readOnly().orNull());
        this.trace(other.trace().orNull());
        this.progressOptions(other.progressOptions().orNull());
        this.logConfig(other.logConfig().orNull());
        this.confirm(other.confirm().orNull());
        this.confirm(other.confirm().orNull());
        this.outputFormat(other.outputFormat().orNull());
        this.outputFormatOptions(other.outputFormatOptions().orNull());
        this.openMode(other.openMode().orNull());
        this.creationTime(other.creationTime().orNull());
        this.dry(other.dry().orNull());
        this.showStacktrace(other.showStacktrace().orNull());
        this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        this.executorOptions(other.executorOptions().orNull());
        this.recover(other.recover().orNull());
        this.reset(other.reset().orNull());
        this.resetHard(other.resetHard().orNull());
        this.commandVersion(other.commandVersion().orNull());
        this.commandHelp(other.commandHelp().orNull());
        this.debug(other.debug().orNull());
        this.inherited(other.inherited().orNull());
        this.executionType(other.executionType().orNull());
        this.runAs(other.runAs().orNull());
        this.archetype(other.archetype().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.homeLocations(other.homeLocations().orNull());
        this.storeLocations(other.storeLocations().orNull());
        this.storeLayout(other.storeLayout().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        this.fetchStrategy(other.fetchStrategy().orNull());
        this.cached(other.cached().orNull());
        this.indexed(other.indexed().orNull());
        this.transitive(other.transitive().orNull());
        this.bot(other.bot().orNull());
        this.stdin(other.stdin().orNull());
        this.stdout(other.stdout().orNull());
        this.stderr(other.stderr().orNull());
        this.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.repositories().orNull());
        this.applicationArguments(other.applicationArguments().orNull());
        this.customOptions(other.customOptions().orNull());
        this.expireTime(other.expireTime().orNull());
        this.errors(other.errors().orNull());
        this.skipErrors(other.skipErrors().orNull());
        this.switchWorkspace(other.switchWorkspace().orNull());
        this.locale(other.locale().orNull());
        this.theme(other.theme().orNull());
        this.dependencySolver(other.dependencySolver().orNull());
        this.isolationLevel(other.isolationLevel().orNull());
        this.initLaunchers(other.initLaunchers().orNull());
        this.initJava(other.initJava().orNull());
        this.initScripts(other.initScripts().orNull());
        this.initPlatforms(other.initPlatforms().orNull());
        this.desktopLauncher(other.desktopLauncher().orNull());
        this.menuLauncher(other.menuLauncher().orNull());
        this.userLauncher(other.userLauncher().orNull());
        this.sharedInstance(other.sharedInstance().orNull());
        this.previewRepo(other.previewRepo().orNull());
        return this;
    }

    public NBootOptionsInfo toBootOptionsInfo() {
        return build().toBootOptionsInfo();
    }

    public NWorkspaceOptionsBuilder copyFrom(NBootOptionsInfo other) {
        this.apiVersion(other.getApiVersion() == null ? null : NVersion.get(other.getApiVersion()).orNull());
        this.runtimeId(other.getRuntimeId() == null ? null :
                other.getRuntimeId().contains("#") ? NId.get(other.getRuntimeId()).orNull() :
                        NId.getRuntime(other.getRuntimeId()).orNull()
        );
        this.javaCommand(other.getJavaCommand());
        this.javaOptions(other.getJavaOptions());
        this.workspace(other.getWorkspace());
        this.name(other.getName());
        this.installCompanions(other.getInstallCompanions());
        this.skipWelcome(other.getSkipWelcome());
        this.skipBoot(other.getSkipBoot());
        this.system(other.getSystem());
        this.gui(other.getGui());
        this.setUserName(other.getUserName());
        this.credential(other.getCredential());
        this.terminalMode(NTerminalMode.parse(other.getTerminalMode()).orNull());
        this.readOnly(other.getReadOnly());
        this.trace(other.getTrace());
        this.progressOptions(other.getProgressOptions());
        {
            NBootLogConfig c = other.getLogConfig();
            NLogConfig v = null;
            if (c != null) {
                v = new NLogConfig();
                v.logFileBase(c.getLogFileBase());
                v.logFileLevel(c.getLogFileLevel());
                v.logTermLevel(c.getLogTermLevel());
                v.logFileSize(c.getLogFileSize());
                v.logFileCount(c.getLogFileCount());
                v.logFileName(c.getLogFileName());
                v.logFileBase(c.getLogFileBase());
            }
            this.logConfig(v);
        }
        this.confirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.confirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.outputFormat(NContentType.parse(other.getOutputFormat()).orNull());
        this.outputFormatOptions(other.getOutputFormatOptions());
        this.openMode(NOpenMode.parse(other.getOpenMode()).orNull());
        this.creationTime(other.getCreationTime());
        this.dry(other.getDry());
        this.showStacktrace(other.getShowStacktrace());
        this.classLoaderSupplier(other.getClassLoaderSupplier());
        this.executorOptions(other.getExecutorOptions());
        this.recover(other.getRecover());
        this.reset(other.getReset());
        this.resetHard(other.getResetHard());
        this.commandVersion(other.getCommandVersion());
        this.commandHelp(other.getCommandHelp());
        this.debug(other.getDebug());
        this.inherited(other.getInherited());
        this.executionType(NExecutionType.parse(other.getExecutionType()).orNull());
        this.runAs(NRunAs.parse(other.getRunAs()).orNull());
        this.archetype(other.getArchetype());
        this.storeStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
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
            this.homeLocations(v);
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
            this.storeLocations(v);
        }
        this.storeLayout(NOsFamily.parse(other.getStoreLayout()).orNull());
        this.storeStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        this.repositoryStoreStrategy(NStoreStrategy.parse(other.getRepositoryStoreStrategy()).orNull());
        this.fetchStrategy(NFetchStrategy.parse(other.getFetchStrategy()).orNull());
        this.cached(other.getCached());
        this.indexed(other.getIndexed());
        this.transitive(other.getTransitive());
        this.bot(other.getBot());
        this.stdin(other.getStdin());
        this.stdout(other.getStdout());
        this.stderr(other.getStderr());
        this.executorService(other.getExecutorService());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.getExcludedExtensions());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.getRepositories());
        this.bootRepositories(other.getBootRepositories());
        this.applicationArguments(other.getApplicationArguments());
        this.customOptions(other.getCustomOptions());
        this.expireTime(other.getExpireTime());
        this.errors(other.getErrors() == null ? new ArrayList<>() : other.getErrors().stream().map(x -> NMsg.ofPlain(x)).collect(Collectors.toList()));
        this.skipErrors(other.getSkipErrors());
        this.switchWorkspace(other.getSwitchWorkspace());
        this.locale(other.getLocale());
        this.theme(other.getTheme());
        this.dependencySolver(other.getDependencySolver());
        this.isolationLevel(NIsolationLevel.parse(other.getIsolationLevel()).orNull());
        this.initLaunchers(other.getInitLaunchers());
        this.initJava(other.getInitJava());
        this.initScripts(other.getInitScripts());
        this.initPlatforms(other.getInitPlatforms());
        this.desktopLauncher(NSupportMode.parse(other.getDesktopLauncher()).orNull());
        this.menuLauncher(NSupportMode.parse(other.getMenuLauncher()).orNull());
        this.userLauncher(NSupportMode.parse(other.getUserLauncher()).orNull());
        this.sharedInstance(other.getSharedInstance());
        this.previewRepo(other.getPreviewRepo());
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other, NAssignmentPolicy assignmentPolicy) {
        if (other != null) {
            if (assignmentPolicy == null) {
                assignmentPolicy = NAssignmentPolicy.ANY;
            }
            assignmentPolicy.applyOptionalValue(this::apiVersion, other::apiVersion, this::apiVersion);
            assignmentPolicy.applyOptionalValue(this::runtimeId, other::runtimeId, this::runtimeId);
            assignmentPolicy.applyOptionalValue(this::javaOptions, other::javaOptions, this::javaOptions);
            assignmentPolicy.applyOptionalValue(this::javaCommand, other::javaCommand, this::javaCommand);
            assignmentPolicy.applyOptionalValue(this::workspace, other::workspace, this::workspace);
            assignmentPolicy.applyOptionalValue(this::name, other::name, this::name);
            assignmentPolicy.applyOptionalValue(this::installCompanions, other::installCompanions, this::installCompanions);
            assignmentPolicy.applyOptionalValue(this::skipWelcome, other::skipWelcome, this::skipWelcome);
            assignmentPolicy.applyOptionalValue(this::skipBoot, other::skipBoot, this::skipBoot);
            assignmentPolicy.applyOptionalValue(this::system, other::system, this::system);
            assignmentPolicy.applyOptionalValue(this::gui, other::gui, this::gui);
            assignmentPolicy.applyOptionalValue(this::userName, other::userName, this::setUserName);
            assignmentPolicy.applyOptionalValue(this::credential, other::credential, this::credential);
            assignmentPolicy.applyOptionalValue(this::terminalMode, other::terminalMode, this::terminalMode);
            assignmentPolicy.applyOptionalValue(this::readOnly, other::readOnly, this::readOnly);
            assignmentPolicy.applyOptionalValue(this::trace, other::trace, this::trace);
            assignmentPolicy.applyOptionalValue(this::progressOptions, other::progressOptions, this::progressOptions);
            assignmentPolicy.applyOptionalValue(this::logConfig, other::logConfig, this::logConfig);
            assignmentPolicy.applyOptionalValue(this::confirm, other::confirm, this::confirm);
            assignmentPolicy.applyOptionalValue(this::confirm, other::confirm, this::confirm);
            assignmentPolicy.applyOptionalValue(this::outputFormat, other::outputFormat, this::outputFormat);
            assignmentPolicy.applyOptionalValue(this::outputFormatOptions, other::outputFormatOptions, this::outputFormatOptions);
            assignmentPolicy.applyOptionalValue(this::openMode, other::openMode, this::openMode);
            assignmentPolicy.applyOptionalValue(this::creationTime, other::creationTime, this::creationTime);
            assignmentPolicy.applyOptionalValue(this::dry, other::dry, this::dry);
            assignmentPolicy.applyOptionalValue(this::showStacktrace, other::showStacktrace, this::showStacktrace);
            assignmentPolicy.applyOptionalValue(this::classLoaderSupplier, other::classLoaderSupplier, this::classLoaderSupplier);
            assignmentPolicy.applyOptionalValue(this::executorOptions, other::executorOptions, this::executorOptions);
            assignmentPolicy.applyOptionalValue(this::recover, other::recover, this::recover);
            assignmentPolicy.applyOptionalValue(this::reset, other::reset, this::reset);
            assignmentPolicy.applyOptionalValue(this::resetHard, other::resetHard, this::resetHard);
            assignmentPolicy.applyOptionalValue(this::commandVersion, other::commandVersion, this::commandVersion);
            assignmentPolicy.applyOptionalValue(this::commandHelp, other::commandHelp, this::commandHelp);
            assignmentPolicy.applyOptionalValue(this::debug, other::debug, this::debug);
            assignmentPolicy.applyOptionalValue(this::inherited, other::inherited, this::inherited);
            assignmentPolicy.applyOptionalValue(this::executionType, other::executionType, this::executionType);
            assignmentPolicy.applyOptionalValue(this::runAs, other::runAs, this::runAs);
            assignmentPolicy.applyOptionalValue(this::archetype, other::archetype, this::archetype);
            assignmentPolicy.applyOptionalValue(this::storeStrategy, other::storeStrategy, this::storeStrategy);
            assignmentPolicy.applyOptionalValue(this::homeLocations, other::homeLocations, this::homeLocations);
            assignmentPolicy.applyOptionalValue(this::storeLocations, other::storeLocations, this::storeLocations);
            assignmentPolicy.applyOptionalValue(this::storeLayout, other::storeLayout, this::storeLayout);
            assignmentPolicy.applyOptionalValue(this::storeStrategy, other::storeStrategy, this::storeStrategy);
            assignmentPolicy.applyOptionalValue(this::repositoryStoreStrategy, other::repositoryStoreStrategy, this::repositoryStoreStrategy);
            assignmentPolicy.applyOptionalValue(this::fetchStrategy, other::fetchStrategy, this::fetchStrategy);
            assignmentPolicy.applyOptionalValue(this::cached, other::cached, this::cached);
            assignmentPolicy.applyOptionalValue(this::indexed, other::indexed, this::indexed);
            assignmentPolicy.applyOptionalValue(this::transitive, other::transitive, this::transitive);
            assignmentPolicy.applyOptionalValue(this::bot, other::bot, this::bot);
            assignmentPolicy.applyOptionalValue(this::stdin, other::stdin, this::stdin);
            assignmentPolicy.applyOptionalValue(this::stdout, other::stdout, this::stdout);
            assignmentPolicy.applyOptionalValue(this::stderr, other::stderr, this::stderr);
            assignmentPolicy.applyOptionalValue(this::executorService, other::executorService, this::executorService);
            assignmentPolicy.applyOptionalValue(this::excludedExtensions, other::excludedExtensions, this::excludedExtensions);
            assignmentPolicy.applyOptionalValue(this::repositories, other::repositories, this::repositories);
            assignmentPolicy.applyOptionalValue(this::bootRepositories, other::bootRepositories, this::bootRepositories);
            assignmentPolicy.applyOptionalValue(this::applicationArguments, other::applicationArguments, this::applicationArguments);
            assignmentPolicy.applyOptionalValue(this::customOptions, other::customOptions, this::customOptions);
            assignmentPolicy.applyOptionalValue(this::expireTime, other::expireTime, this::expireTime);
            assignmentPolicy.applyOptionalValue(this::errors, other::errors, this::errors);
            assignmentPolicy.applyOptionalValue(this::skipErrors, other::skipErrors, this::skipErrors);
            assignmentPolicy.applyOptionalValue(this::switchWorkspace, other::switchWorkspace, this::switchWorkspace);
            assignmentPolicy.applyOptionalValue(this::locale, other::locale, this::locale);
            assignmentPolicy.applyOptionalValue(this::theme, other::theme, this::theme);
            assignmentPolicy.applyOptionalValue(this::dependencySolver, other::dependencySolver, this::dependencySolver);
            assignmentPolicy.applyOptionalValue(this::isolationLevel, other::isolationLevel, this::isolationLevel);
            assignmentPolicy.applyOptionalValue(this::initLaunchers, other::initLaunchers, this::initLaunchers);
            assignmentPolicy.applyOptionalValue(this::initJava, other::initJava, this::initJava);
            assignmentPolicy.applyOptionalValue(this::initScripts, other::initScripts, this::initScripts);
            assignmentPolicy.applyOptionalValue(this::initLaunchers, other::initLaunchers, this::initLaunchers);
            assignmentPolicy.applyOptionalValue(this::desktopLauncher, other::desktopLauncher, this::desktopLauncher);
            assignmentPolicy.applyOptionalValue(this::menuLauncher, other::menuLauncher, this::menuLauncher);
            assignmentPolicy.applyOptionalValue(this::userLauncher, other::userLauncher, this::userLauncher);
            assignmentPolicy.applyOptionalValue(this::previewRepo, other::previewRepo, this::previewRepo);
            assignmentPolicy.applyOptionalValue(this::sharedInstance, other::sharedInstance, this::sharedInstance);
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

    public NOptional<Boolean> sharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
    }

    @Override
    public NWorkspaceOptionsBuilder sharedInstance(Boolean sharedInstance) {
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
                    option = NStringUtils.strip(option);
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
    public NOptional<String> dependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public NWorkspaceOptionsBuilder dependencySolver(String dependencySolver) {
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
                apiVersion().orNull(), runtimeId().orNull(), workspace().orNull(),
                name().orNull(), javaCommand().orNull(), javaOptions().orNull(),
                outLinePrefix().orNull(), errLinePrefix().orNull(), userName().orNull(),
                credential().orNull(), progressOptions().orNull(), dependencySolver().orNull(),
                debug().orNull(), archetype().orNull(), locale().orNull(), theme().orNull(),
                logConfig().orNull(), confirm().orNull(), outputFormat().orNull(), openMode().orNull(),
                executionType().orNull(), storeStrategy().orNull(), repositoryStoreStrategy().orNull(),
                storeLayout().orNull(), terminalMode().orNull(), fetchStrategy().orNull(),
                runAs().orNull(), creationTime().orNull(), expireTime().orNull(),
                installCompanions().orNull(), skipWelcome().orNull(), skipBoot().orNull(),
                system().orNull(), gui().orNull(), readOnly().orNull(), trace().orNull(),
                dry().orNull(), showStacktrace().orNull(), recover().orNull(), reset().orNull(), resetHard().orNull(), commandVersion().orNull(),
                commandHelp().orNull(), commandHelp().orNull(), switchWorkspace().orNull(), cached().orNull(),
                indexed().orNull(), transitive().orNull(), bot().orNull(), skipErrors().orNull(),
                isolationLevel().orNull(), initLaunchers().orNull(), initScripts().orNull(), initPlatforms().orNull(),
                initJava().orNull(), stdin().orNull(), stdout().orNull(), stdout().orNull(), executorService().orNull(),
                classLoaderSupplier().orNull(), applicationArguments().orNull(), outputFormatOptions().orNull(),
                customOptions().orNull(), excludedExtensions().orNull(), repositories().orNull(), bootRepositories().orNull(),
                executorOptions().orNull(), errors().orNull(), storeLocations().orNull(), homeLocations().orNull(),
                desktopLauncher().orNull(), menuLauncher().orNull(), userLauncher().orNull()
                , previewRepo().orNull()
                , sharedInstance().orNull()
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
    public NOptional<NIsolationLevel> isolationLevel() {
        return NOptional.ofNamed(isolationLevel, "isolationLevel");
    }

    @Override
    public NOptional<Boolean> initLaunchers() {
        return NOptional.ofNamed(initLaunchers, "initLaunchers");
    }

    @Override
    public NOptional<Boolean> initScripts() {
        return NOptional.ofNamed(initScripts, "initScripts");
    }

    @Override
    public NOptional<Boolean> initPlatforms() {
        return NOptional.ofNamed(initPlatforms, "initPlatforms");
    }

    @Override
    public NOptional<Boolean> initJava() {
        return NOptional.ofNamed(initJava, "initJava");
    }

    @Override
    public NWorkspaceOptionsBuilder unsetRuntimeOptions() {
        commandHelp(null);
        commandVersion(null);
        openMode(null);
        executionType(null);
        runAs(null);
        reset(null);
        recover(null);
        dry(null);
        showStacktrace(null);
        executorOptions(null);
        applicationArguments(null);
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder unsetCreationOptions() {
        name(null);
        archetype(null);
        storeLayout(null);
        storeStrategy(null);
        repositoryStoreStrategy(null);
        storeLocations(null);
        homeLocations(null);
        switchWorkspace(null);
        bootRepositories(null);
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder unsetExportedOptions() {
        javaCommand(null);
        javaOptions(null);
        workspace(null);
        setUserName(null);
        credential(null);
        apiVersion(null);
        runtimeId(null);
        terminalMode(null);
        logConfig(null);
        excludedExtensions(null);
        repositories(null);
        system(null);
        gui(null);
        readOnly(null);
        trace(null);
        progressOptions(null);
        dependencySolver(null);
        debug(null);
        installCompanions(null);
        skipWelcome(null);
        skipBoot(null);
        outLinePrefix(null);
        errLinePrefix(null);
        cached(null);
        indexed(null);
        transitive(null);
        bot(null);
        fetchStrategy(null);
        confirm(null);
        outputFormat(null);
        outputFormatOptions((List<String>) null);
        expireTime(null);
        theme(null);
        locale(null);
        initLaunchers(null);
        initPlatforms(null);
        initScripts(null);
        initJava(null);
        desktopLauncher(null);
        menuLauncher(null);
        userLauncher(null);
        return this;
    }

}
