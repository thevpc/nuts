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

import net.thevpc.nuts.boot.internal.util.NBootUtils;

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
    /**
     * special
     */
    private NBootClassLoaderNode runtimeBootDependencyNode;

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
    private char[] credential;

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


    public String desktopLauncher() {
        return desktopLauncher;
    }


    public String menuLauncher() {
        return menuLauncher;
    }


    public String userLauncher() {
        return userLauncher;
    }


    public NBootOptionsInfo initLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }


    public NBootOptionsInfo initScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }


    public NBootOptionsInfo initPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }


    public NBootOptionsInfo initJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }


    public NBootOptionsInfo isolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }


    public NBootOptionsInfo desktopLauncher(String desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }


    public NBootOptionsInfo menuLauncher(String menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }


    public NBootOptionsInfo userLauncher(String userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }


    public NBootOptionsInfo copy() {
        return new NBootOptionsInfo().copyFrom(this);
    }


    public String apiVersion() {
        return apiVersion;
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }


    public List<String> applicationArguments() {
        return applicationArguments;
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo applicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }


    public String archetype() {
        return archetype;
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo archetype(String archetype) {
        this.archetype = archetype;
        return this;
    }


    public Supplier<ClassLoader> classLoaderSupplier() {
        return classLoaderSupplier;
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo classLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }


    public String confirm() {
        return confirm;
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo confirm(String confirm) {
        this.confirm = confirm;
        return this;
    }


    public Boolean dry() {
        return dry;
    }


    public Boolean showStacktrace() {
        return showStacktrace;
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo dry(Boolean dry) {
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

    public NBootOptionsInfo showStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }


    public Instant creationTime() {
        return creationTime;
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo creationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }


    public List<String> excludedExtensions() {
        return excludedExtensions;
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo excludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }


    public String executionType() {
        return executionType;
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo executionType(String executionType) {
        this.executionType = executionType;
        return this;
    }


    public String runAs() {
        return runAs;
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NBootOptionsInfo runAs(String runAs) {
        this.runAs = runAs;
        return this;
    }


    public List<String> executorOptions() {
        return executorOptions;
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo executorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }


    public String homeLocation(NBootHomeLocation location) {
        return homeLocations == null ? null : homeLocations.get(location);
    }


    public Map<NBootHomeLocation, String> homeLocations() {
        return homeLocations;
    }


    public NBootOptionsInfo homeLocations(Map<NBootHomeLocation, String> homeLocations) {
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


    public String javaCommand() {
        return javaCommand;
    }


    public NBootOptionsInfo javaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }


    public String javaOptions() {
        return javaOptions;
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo javaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }


    public NBootLogConfig logConfig() {
        return logConfig;
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo logConfig(NBootLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }


    public String name() {
        return name;
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo name(String workspaceName) {
        this.name = workspaceName;
        return this;
    }


    public String openMode() {
        return openMode;
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo openMode(String openMode) {
        this.openMode = openMode;
        return this;
    }


    public String outputFormat() {
        return outputFormat;
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo outputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }


    public List<String> outputFormatOptions() {
        return outputFormatOptions;
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo outputFormatOptions(List<String> options) {
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

    public NBootOptionsInfo outputFormatOptions(String[] options) {
        if (outputFormatOptions == null) {
            outputFormatOptions = new ArrayList<>();
        }
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }


    public char[] credential() {
        return credential;
    }

    /**
     * set password
     *
     * @param credential new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo credential(char[] credential) {
        this.credential = credential;
        return this;
    }


    public String repositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo repositoryStoreStrategy(String repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }


    public String runtimeId() {
        return runtimeId;
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo runtimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }


    public String storeType(String folder) {
        return storeLocations == null ? null : storeLocations.get(NBootUtils.enumId(folder));
    }


    public String storeLayout() {
        return storeLayout;
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo storeLayout(String storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }


    public String storeStrategy() {
        return storeStrategy;
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo storeStrategy(String storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }


    public Map<String, String> storeLocations() {
        return storeLocations;
    }


    public NBootOptionsInfo storeLocations(Map<String, String> storeLocations) {
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


    public String terminalMode() {
        return terminalMode;
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo terminalMode(String terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }


    public List<String> repositories() {
        return repositories;
    }

    public List<String> bootRepositories() {
        return bootRepositories;
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo repositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    /**
     * set initRepositories
     *
     * @param bootRepositories new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo bootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }


    public String userName() {
        return userName;
    }


    public String workspace() {
        return workspace;
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */

    public NBootOptionsInfo workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }


    public String debug() {
        return debug;
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo debug(String debug) {
        this.debug = debug;
        return this;
    }


    public Boolean system() {
        return system;
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo system(Boolean system) {
        this.system = system;
        return this;
    }


    public Boolean gui() {
        return gui;
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo gui(Boolean gui) {
        this.gui = gui;
        return this;
    }


    public Boolean inherited() {
        return inherited;
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo inherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }


    public Boolean readOnly() {
        return readOnly;
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }


    public Boolean recover() {
        return recover;
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo recover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    public Boolean resetHard() {
        return resetHard;
    }

    public void resetHard(Boolean resetHard) {
        this.resetHard = resetHard;
    }

    public Boolean reset() {
        return reset;
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo reset(Boolean reset) {
        this.reset = reset;
        return this;
    }


    public Boolean commandVersion() {
        return commandVersion;
    }


    public NBootOptionsInfo commandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }


    public Boolean commandHelp() {
        return commandHelp;
    }


    public NBootOptionsInfo commandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }


    public Boolean installCompanions() {
        return installCompanions;
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo installCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
    }


    public Boolean skipWelcome() {
        return skipWelcome;
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo skipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }


    public String outLinePrefix() {
        return outLinePrefix;
    }


    public NBootOptionsInfo outLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }


    public String errLinePrefix() {
        return errLinePrefix;
    }


    public NBootOptionsInfo errLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }


    public Boolean skipBoot() {
        return skipBoot;
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo skipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }


    public Boolean trace() {
        return trace;
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo trace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public String progressOptions() {
        return progressOptions;
    }


    public NBootOptionsInfo progressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }


    public Boolean cached() {
        return cached;
    }


    public NBootOptionsInfo cached(Boolean cached) {
        this.cached = cached;
        return this;
    }


    public Boolean indexed() {
        return indexed;
    }


    public NBootOptionsInfo indexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }


    public Boolean transitive() {
        return transitive;
    }


    public NBootOptionsInfo transitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }


    public Boolean bot() {
        return bot;
    }


    public NBootOptionsInfo bot(Boolean bot) {
        this.bot = bot;
        return this;
    }


    public Boolean previewRepo() {
        return previewRepo;
    }


    public NBootOptionsInfo previewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }


    public String fetchStrategy() {
        return fetchStrategy;
    }


    public NBootOptionsInfo setchStrategy(String fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }


    public InputStream stdin() {
        return stdin;
    }


    public NBootOptionsInfo stdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }


    public PrintStream stdout() {
        return stdout;
    }


    public NBootOptionsInfo stdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }


    public PrintStream stderr() {
        return stderr;
    }


    public NBootOptionsInfo stderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }


    public ExecutorService executorService() {
        return executorService;
    }


    public NBootOptionsInfo executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }


    public Instant expireTime() {
        return expireTime;
    }


    public NBootOptionsInfo expireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }


    public Boolean skipErrors() {
        return skipErrors;
    }


    public NBootOptionsInfo skipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }


    public Boolean switchWorkspace() {
        return switchWorkspace;
    }

    public NBootOptionsInfo switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }


    public List<String> errors() {
        return errors;
    }


    public NBootOptionsInfo errors(List<String> errors) {
        this.errors = errors;
        return this;
    }


    public List<String> customOptions() {
        return customOptions;
    }


    public NBootOptionsInfo customOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }


    public String locale() {
        return locale;
    }


    public NBootOptionsInfo locale(String locale) {
        this.locale = locale;
        return this;
    }


    public String theme() {
        return theme;
    }


    public NBootOptionsInfo theme(String theme) {
        this.theme = theme;
        return this;
    }


    public NBootOptionsInfo copyFrom(NBootOptionsInfo other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion());
        this.runtimeId(other.runtimeId());
        this.javaCommand(other.javaCommand());
        this.javaOptions(other.javaOptions());
        this.workspace(other.workspace());
        this.name(other.name());
        this.installCompanions(other.installCompanions());
        this.skipWelcome(other.skipWelcome());
        this.skipBoot(other.skipBoot());
        this.system(other.system());
        this.gui(other.gui());
        this.userName(other.userName());
        this.credential(other.credential());
        this.terminalMode(other.terminalMode());
        this.readOnly(other.readOnly());
        this.trace(other.trace());
        this.progressOptions(other.progressOptions());
        this.logConfig(other.logConfig());
        this.confirm(other.confirm());
        this.confirm(other.confirm());
        this.outputFormat(other.outputFormat());
        this.outputFormatOptions(other.outputFormatOptions());
        this.openMode(other.openMode());
        this.creationTime(other.creationTime());
        this.dry(other.dry());
        this.showStacktrace(other.showStacktrace());
        this.classLoaderSupplier(other.classLoaderSupplier());
        this.executorOptions(other.executorOptions());
        this.recover(other.recover());
        this.reset(other.reset());
        this.resetHard(other.resetHard());
        this.commandVersion(other.commandVersion());
        this.commandHelp(other.commandHelp());
        this.debug(other.debug());
        this.inherited(other.inherited());
        this.executionType(other.executionType());
        this.runAs(other.runAs());
        this.archetype(other.archetype());
        this.storeStrategy(other.storeStrategy());
        this.homeLocations(other.homeLocations());
        this.storeLocations(other.storeLocations());
        this.storeLayout(other.storeLayout());
        this.storeStrategy(other.storeStrategy());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy());
        this.setchStrategy(other.fetchStrategy());
        this.cached(other.cached());
        this.indexed(other.indexed());
        this.transitive(other.transitive());
        this.bot(other.bot());
        this.stdin(other.stdin());
        this.stdout(other.stdout());
        this.stderr(other.stderr());
        this.executorService(other.executorService());
        this.excludedExtensions(other.excludedExtensions());
        this.repositories(other.repositories());
        this.bootRepositories(other.bootRepositories());
        this.applicationArguments(other.applicationArguments());
        this.customOptions(other.customOptions());
        this.expireTime(other.expireTime());
        this.errors(other.errors());
        this.skipErrors(other.skipErrors());
        this.switchWorkspace(other.switchWorkspace());
        this.locale(other.locale());
        this.theme(other.theme());
        this.dependencySolver(other.dependencySolver());
        this.isolationLevel(other.isolationLevel());
        this.initLaunchers(other.initLaunchers());
        this.initJava(other.initJava());
        this.initScripts(other.initScripts());
        this.initPlatforms(other.initPlatforms());
        this.desktopLauncher(other.desktopLauncher());
        this.menuLauncher(other.menuLauncher());
        this.userLauncher(other.userLauncher());
        this.sharedInstance(other.sharedInstance());
        this.previewRepo(other.previewRepo());
        this.runtimeBootDependencyNode(other.runtimeBootDependencyNode());
        this.bootWorkspaceFactory(other.bootWorkspaceFactory());
        this.classWorldURLs(other.classWorldURLs());
        this.classWorldLoader(other.classWorldLoader());
        this.uuid(other.uuid());
        this.runtimeBootDescriptor(other.runtimeBootDescriptor());
        return this;
    }


    public NBootOptionsInfo copyFromPresent(NBootOptionsInfo o) {
        if (o != null) {
            if (o.apiVersion() != null) {
                this.apiVersion(o.apiVersion());
            }
            if (o.runtimeId() != null) {
                this.runtimeId(o.runtimeId());
            }
            if (o.javaCommand() != null) {
                this.javaCommand(o.javaCommand());
            }
            if (o.javaOptions() != null) {
                this.javaOptions(o.javaOptions());
            }
            if (o.workspace() != null) {
                this.workspace(o.workspace());
            }
            if (o.name() != null) {
                this.name(o.name());
            }
            if (o.installCompanions() != null) {
                this.installCompanions(o.installCompanions());
            }
            if (o.skipWelcome() != null) {
                this.skipWelcome(o.skipWelcome());
            }
            if (o.skipBoot() != null) {
                this.skipBoot(o.skipBoot());
            }
            if (o.system() != null) {
                this.system(o.system());
            }
            if (o.gui() != null) {
                this.gui(o.gui());
            }
            if (o.userName() != null) {
                this.userName(o.userName());
            }
            if (o.credential() != null) {
                this.credential(o.credential());
            }
            if (o.terminalMode() != null) {
                this.terminalMode(o.terminalMode());
            }
            if (o.readOnly() != null) {
                this.readOnly(o.readOnly());
            }
            if (o.trace() != null) {
                this.trace(o.trace());
            }
            if (o.progressOptions() != null) {
                this.progressOptions(o.progressOptions());
            }
            if (o.logConfig() != null) {
                this.logConfig(o.logConfig());
            }
            if (o.confirm() != null) {
                this.confirm(o.confirm());
            }
            if (o.confirm() != null) {
                this.confirm(o.confirm());
            }
            if (o.outputFormat() != null) {
                this.outputFormat(o.outputFormat());
            }
            if (o.outputFormatOptions() != null) {
                this.outputFormatOptions(o.outputFormatOptions());
            }
            if (o.openMode() != null) {
                this.openMode(o.openMode());
            }
            if (o.creationTime() != null) {
                this.creationTime(o.creationTime());
            }
            if (o.dry() != null) {
                this.dry(o.dry());
            }
            if (o.showStacktrace() != null) {
                this.showStacktrace(o.showStacktrace());
            }
            if (o.classLoaderSupplier() != null) {
                this.classLoaderSupplier(o.classLoaderSupplier());
            }
            if (o.executorOptions() != null) {
                this.executorOptions(o.executorOptions());
            }
            if (o.recover() != null) {
                this.recover(o.recover());
            }
            if (o.reset() != null) {
                this.reset(o.reset());
            }
            if (o.resetHard() != null) {
                this.resetHard(o.resetHard());
            }
            if (o.commandVersion() != null) {
                this.commandVersion(o.commandVersion());
            }
            if (o.commandHelp() != null) {
                this.commandHelp(o.commandHelp());
            }
            if (o.debug() != null) {
                this.debug(o.debug());
            }
            if (o.inherited() != null) {
                this.inherited(o.inherited());
            }
            if (o.executionType() != null) {
                this.executionType(o.executionType());
            }
            if (o.runAs() != null) {
                this.runAs(o.runAs());
            }
            if (o.archetype() != null) {
                this.archetype(o.archetype());
            }
            if (o.storeStrategy() != null) {
                this.storeStrategy(o.storeStrategy());
            }
            if (o.homeLocations() != null) {
                this.homeLocations(o.homeLocations());
            }

            if (o.storeLocations() != null) {
                this.storeLocations(o.storeLocations());
            }
            if (o.storeLayout() != null) {
                this.storeLayout(o.storeLayout());
            }
            if (o.storeStrategy() != null) {
                this.storeStrategy(o.storeStrategy());
            }
            if (o.repositoryStoreStrategy() != null) {
                this.repositoryStoreStrategy(o.repositoryStoreStrategy());
            }
            if (o.fetchStrategy() != null) {
                this.setchStrategy(o.fetchStrategy());
            }
            if (o.cached() != null) {
                this.cached(o.cached());
            }
            if (o.indexed() != null) {
                this.indexed(o.indexed());
            }
            if (o.transitive() != null) {
                this.transitive(o.transitive());
            }
            if (o.bot() != null) {
                this.bot(o.bot());
            }
            if (o.stdin() != null) {
                this.stdin(o.stdin());
            }
            if (o.stdout() != null) {
                this.stdout(o.stdout());
            }
            if (o.stderr() != null) {
                this.stderr(o.stderr());
            }
            if (o.executorService() != null) {
                this.executorService(o.executorService());
            }
            if (o.excludedExtensions() != null) {
                this.excludedExtensions(o.excludedExtensions());
            }
            if (o.repositories() != null) {
                this.repositories(o.repositories());
            }
            if (o.bootRepositories() != null) {
                this.bootRepositories(o.bootRepositories());
            }
            if (o.applicationArguments() != null) {
                this.applicationArguments(o.applicationArguments());
            }
            if (o.customOptions() != null) {
                this.customOptions(o.customOptions());
            }
            if (o.expireTime() != null) {
                this.expireTime(o.expireTime());
            }
            if (o.errors() != null) {
                this.errors(o.errors());
            }
            if (o.skipErrors() != null) {
                this.skipErrors(o.skipErrors());
            }
            if (o.switchWorkspace() != null) {
                this.switchWorkspace(o.switchWorkspace());
            }
            if (o.locale() != null) {
                this.locale(o.locale());
            }
            if (o.theme() != null) {
                this.theme(o.theme());
            }
            if (o.dependencySolver() != null) {
                this.dependencySolver(o.dependencySolver());
            }
            if (o.isolationLevel() != null) {
                this.isolationLevel(o.isolationLevel());
            }
            if (o.initLaunchers() != null) {
                this.initLaunchers(o.initLaunchers());
            }
            if (o.initJava() != null) {
                this.initJava(o.initJava());
            }
            if (o.initScripts() != null) {
                this.initScripts(o.initScripts());
            }
            if (o.initLaunchers() != null) {
                this.initLaunchers(o.initLaunchers());
            }
            if (o.desktopLauncher() != null) {
                this.desktopLauncher(o.desktopLauncher());
            }
            if (o.menuLauncher() != null) {
                this.menuLauncher(o.menuLauncher());
            }
            if (o.userLauncher() != null) {
                this.userLauncher(o.userLauncher());
            }
            if (o.previewRepo() != null) {
                this.previewRepo(o.previewRepo());
            }
            if (o.sharedInstance() != null) {
                this.sharedInstance(o.sharedInstance());
            }
            if (o.runtimeBootDependencyNode() != null) {
                runtimeBootDependencyNode(o.runtimeBootDependencyNode());
            }
            if (o.bootWorkspaceFactory() != null) {
                bootWorkspaceFactory(o.bootWorkspaceFactory());
            }
            if (o.classWorldURLs() != null) {
                classWorldURLs(o.classWorldURLs());
            }
            if (o.classWorldLoader() != null) {
                classWorldLoader(o.classWorldLoader());
            }
            if (o.uuid() != null) {
                uuid(o.uuid());
            }
            if (o.runtimeBootDescriptor() != null) {
                runtimeBootDescriptor(o.runtimeBootDescriptor());
            }
        }
        return this;
    }

    public Boolean sharedInstance() {
        return sharedInstance;
    }


    public NBootOptionsInfo sharedInstance(Boolean sharedInstance) {
        this.sharedInstance = sharedInstance;
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */

    public NBootOptionsInfo userName(String username) {
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


    public String dependencySolver() {
        return dependencySolver;
    }


    public NBootOptionsInfo dependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    public String isolationLevel() {
        return isolationLevel;
    }


    public Boolean initLaunchers() {
        return initLaunchers;
    }


    public Boolean initScripts() {
        return initScripts;
    }


    public Boolean initPlatforms() {
        return initPlatforms;
    }


    public Boolean initJava() {
        return initJava;
    }


    public NBootOptionsInfo unsetRuntimeOptions() {
        commandHelp(null);
        commandVersion(null);
        openMode(null);
        executionType(null);
        runAs(null);
        reset(null);
        resetHard(null);
        recover(null);
        dry(null);
        showStacktrace(null);
        executorOptions(null);
        applicationArguments(null);
        return this;
    }


    public NBootOptionsInfo unsetCreationOptions() {
        name(null);
        archetype(null);
        storeLayout(null);
        storeStrategy(null);
        repositoryStoreStrategy(null);
        storeLocations(null);
        homeLocations(null);
        switchWorkspace(null);
        return this;
    }


    public NBootOptionsInfo unsetExportedOptions() {
        javaCommand(null);
        javaOptions(null);
        workspace(null);
        userName(null);
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
        setchStrategy(null);
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

    /// /////////////////////////////////

    public NBootClassLoaderNode runtimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }


    public NBootOptionsInfo runtimeBootDependencyNode(NBootClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public NBootWorkspaceFactory bootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }


    public NBootOptionsInfo bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public List<URL> classWorldURLs() {
        return classWorldURLs;
    }


    public NBootOptionsInfo classWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = NBootUtils.nonNullList(classWorldURLs);
        return this;
    }

    public ClassLoader classWorldLoader() {
        return classWorldLoader;
    }


    public NBootOptionsInfo classWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public String uuid() {
        return uuid;
    }


    public NBootOptionsInfo uuid(String uuid) {
        this.uuid = NBootUtils.trimToNull(uuid);
        return this;
    }

    public NBootDescriptor runtimeBootDescriptor() {
        return runtimeBootDescriptor;
    }


    public NBootOptionsInfo runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public NBootOptionsInfo resetOptions() {
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
        credential = null;
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
}
