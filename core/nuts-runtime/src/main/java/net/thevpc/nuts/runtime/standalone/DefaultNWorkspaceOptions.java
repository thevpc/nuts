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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.internal.NReservedWorkspaceOptionsToCmdLineBuilder;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
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
public class DefaultNWorkspaceOptions implements Serializable, NWorkspaceOptions {

    public static NWorkspaceOptions BLANK = new DefaultNWorkspaceOptions(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null,
            null,
            null, null, null,
            null, null, null, null, null,null);

    private static final long serialVersionUID = 1;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> outputFormatOptions;

    private final List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private final NVersion apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * child workspaces)
     */
    private final NId runtimeId;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String javaCommand;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String javaOptions;

    /**
     * workspace folder location path option-type : exported (inherited in child
     * workspaces)
     */
    private final String workspace;

    /**
     * out line prefix, option-type : exported (inherited in child workspaces)
     */
    private final String outLinePrefix;

    /**
     * err line prefix, option-type : exported (inherited in child workspaces)
     */
    private final String errLinePrefix;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private final String name;

    /**
     * if true, do not install nuts companion tools upon workspace creation
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean installCompanions;

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * defaults to false option-type : exported (inherited in child workspaces)
     *
     * @since 0.5.5
     */
    private final Boolean skipWelcome;

    /**
     * if true, do not bootstrap workspace after reset/recover. When
     * reset/recover is not active this option is not accepted and an error will
     * be thrown
     *
     * @since 0.6.0
     */
    private final Boolean skipBoot;

    /**
     * if true consider system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean system;

    /**
     * if true consider GUI/Swing mode
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean gui;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> excludedExtensions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> repositories;
    private final List<String> bootRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String userName;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final char[] credentials;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NTerminalMode terminalMode;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean readOnly;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean trace;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String progressOptions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String dependencySolver;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final NOpenMode openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Instant creationTime;

    /**
     * if true no real execution, option-type : runtime (available only for the
     * current workspace instance)
     */
    private final Boolean dry;

    /**
     * if true show exception stacktrace, option-type : runtime (available only for the
     * current workspace instance)
     */
    private final Boolean showStacktrace;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Supplier<ClassLoader> classLoaderSupplier;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final List<String> executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean recover;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean reset;

    /**
     * @since 0.8.5
     * reset ALL workspaces
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean resetHard;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean commandVersion;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean commandHelp;

    /**
     * option-type : runtime / exported (depending on the value)
     */
    private final String debug;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean inherited;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final NExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private final NRunAs runAs;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final String archetype;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @since 0.8.0
     */
    private final Boolean switchWorkspace;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final Map<NStoreType, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final Map<NHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NOsFamily storeLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NStoreStrategy storeStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NStoreStrategy repositoryStoreStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NFetchStrategy fetchStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean cached;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean indexed;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean transitive;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean bot;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean previewRepo;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final InputStream stdin;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final PrintStream stdout;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final PrintStream stderr;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final ExecutorService executorService;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Instant expireTime;
    private final List<NMsg> errors;
    private final Boolean skipErrors;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String locale;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String theme;
    private final Boolean initLaunchers;
    private final Boolean initScripts;
    private final Boolean initPlatforms;
    private final Boolean initJava;
    private final Boolean sharedInstance;
    private final NIsolationLevel isolationLevel;
    private final NSupportMode desktopLauncher;
    private final NSupportMode menuLauncher;
    private final NSupportMode userLauncher;

    public DefaultNWorkspaceOptions(NVersion apiVersion, NId runtimeId, String workspace, String name, String javaCommand,
                                    String javaOptions, String outLinePrefix, String errLinePrefix, String userName,
                                    char[] credentials, String progressOptions, String dependencySolver,
                                    String debug, String archetype, String locale, String theme, NLogConfig logConfig,
                                    NConfirmationMode confirm, NContentType outputFormat, NOpenMode openMode,
                                    NExecutionType executionType, NStoreStrategy storeStrategy,
                                    NStoreStrategy repositoryStoreStrategy, NOsFamily storeLayout,
                                    NTerminalMode terminalMode, NFetchStrategy fetchStrategy, NRunAs runAs,
                                    Instant creationTime, Instant expireTime, Boolean installCompanions, Boolean skipWelcome,
                                    Boolean skipBoot, Boolean system, Boolean gui, Boolean readOnly,
                                    Boolean trace, Boolean dry, Boolean showStacktrace, Boolean recover, Boolean reset, Boolean resetHard, Boolean commandVersion,
                                    Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached,
                                    Boolean indexed, Boolean transitive, Boolean bot, Boolean skipErrors,
                                    NIsolationLevel isolationLevel, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, InputStream stdin, PrintStream stdout, PrintStream stderr,
                                    ExecutorService executorService, Supplier<ClassLoader> classLoaderSupplier,
                                    List<String> applicationArguments, List<String> outputFormatOptions,
                                    List<String> customOptions, List<String> excludedExtensions, List<String> repositories, List<String> bootRepositories,
                                    List<String> executorOptions, List<NMsg> errors, Map<NStoreType, String> storeLocations,
                                    Map<NHomeLocation, String> homeLocations, NSupportMode desktopLauncher, NSupportMode menuLauncher,
                                    NSupportMode userLauncher,
                                    Boolean previewRepo,
                                    Boolean sharedInstance
    ) {
        this.outputFormatOptions = NReservedLangUtils.unmodifiableOrNullList(outputFormatOptions);
        this.customOptions = NReservedLangUtils.unmodifiableOrNullList(customOptions);
        this.excludedExtensions = NReservedLangUtils.unmodifiableOrNullList(excludedExtensions);
        this.repositories = NReservedLangUtils.unmodifiableOrNullList(repositories);
        this.bootRepositories = NReservedLangUtils.unmodifiableOrNullList(bootRepositories);
        this.applicationArguments = NReservedLangUtils.unmodifiableOrNullList(applicationArguments);
        this.errors = NReservedLangUtils.unmodifiableOrNullList(errors);
        this.executorOptions = NReservedLangUtils.unmodifiableOrNullList(executorOptions);

        this.storeLocations = NReservedLangUtils.unmodifiableOrNullMap(storeLocations);
        this.homeLocations = NReservedLangUtils.unmodifiableOrNullMap(homeLocations);

        this.apiVersion = apiVersion;
        this.runtimeId = runtimeId;
        this.javaCommand = javaCommand;
        this.javaOptions = javaOptions;
        this.workspace = workspace;
        this.outLinePrefix = outLinePrefix;
        this.errLinePrefix = errLinePrefix;
        this.name = name;
        this.installCompanions = installCompanions;
        this.skipWelcome = skipWelcome;
        this.skipBoot = skipBoot;
        this.system = system;
        this.gui = gui;
        this.userName = userName;
        this.credentials = credentials == null ? null : Arrays.copyOf(credentials, credentials.length);
        this.terminalMode = terminalMode;
        this.readOnly = readOnly;
        this.trace = trace;
        this.progressOptions = progressOptions;
        this.dependencySolver = dependencySolver;
        this.logConfig = logConfig == null ? null : logConfig.readOnly();
        this.confirm = confirm;
        this.outputFormat = outputFormat;
        this.openMode = openMode;
        this.creationTime = creationTime;
        this.dry = dry;
        this.showStacktrace = showStacktrace;
        this.classLoaderSupplier = classLoaderSupplier;
        this.recover = recover;
        this.reset = reset;
        this.resetHard = resetHard;
        this.commandVersion = commandVersion;
        this.commandHelp = commandHelp;
        this.debug = debug;
        this.inherited = inherited;
        this.executionType = executionType;
        this.runAs = runAs;
        this.archetype = archetype;
        this.switchWorkspace = switchWorkspace;
        this.storeLayout = storeLayout;
        this.storeStrategy = storeStrategy;
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        this.fetchStrategy = fetchStrategy;
        this.cached = cached;
        this.indexed = indexed;
        this.transitive = transitive;
        this.bot = bot;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executorService = executorService;
        this.expireTime = expireTime;
        this.skipErrors = skipErrors;
        this.locale = locale;
        this.theme = theme;
        this.isolationLevel = isolationLevel;
        this.initLaunchers = initLaunchers;
        this.initScripts = initScripts;
        this.initPlatforms = initPlatforms;
        this.initJava = initJava;
        this.desktopLauncher = desktopLauncher;
        this.menuLauncher = menuLauncher;
        this.userLauncher = userLauncher;
        this.previewRepo = previewRepo;
        this.sharedInstance = sharedInstance;
    }

    public NBootOptionsInfo toBootOptionsInfo() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.apiVersion(this.apiVersion().map(x -> x.toString()).orNull());
        r.runtimeId(this.runtimeId().map(x -> x.toString()).orNull());
        r.javaCommand(this.javaCommand().orNull());
        r.javaOptions(this.javaOptions().orNull());
        r.workspace(this.workspace().orNull());
        r.name(this.name().orNull());
        r.installCompanions(this.installCompanions().orNull());
        r.skipWelcome(this.skipWelcome().orNull());
        r.skipBoot(this.skipBoot().orNull());
        r.system(this.system().orNull());
        r.gui(this.gui().orNull());
        r.userName(this.userName().orNull());
        r.credential(this.credential().orNull());
        r.terminalMode(this.terminalMode().map(x -> x.id()).orNull());
        r.readOnly(this.readOnly().orNull());
        r.trace(this.trace().orNull());
        r.progressOptions(this.progressOptions().orNull());
        {
            NLogConfig c = this.logConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.logFileBase(c.logFileBase());
                v.logFileLevel(c.logFileLevel());
                v.logTermLevel(c.logTermLevel());
                v.logFileSize(c.logFileSize());
                v.logFileCount(c.logFileCount());
                v.logFileName(c.logFileName());
                v.logFileBase(c.logFileBase());
            }
            r.logConfig(v);
        }
        r.confirm(this.confirm().map(x -> x.id()).orNull());
        r.confirm(this.confirm().map(x -> x.id()).orNull());
        r.outputFormat(this.outputFormat().map(x -> x.id()).orNull());
        r.outputFormatOptions(this.outputFormatOptions().orNull());
        r.openMode(this.openMode().map(x -> x.id()).orNull());
        r.creationTime(this.creationTime().orNull());
        r.dry(this.dry().orNull());
        r.showStacktrace(this.showStacktrace().orNull());
        r.classLoaderSupplier(this.classLoaderSupplier().orNull());
        r.executorOptions(this.executorOptions().orNull());
        r.recover(this.recover().orNull());
        r.reset(this.reset().orNull());
        r.resetHard(this.resetHard().orNull());
        r.commandVersion(this.commandVersion().orNull());
        r.commandHelp(this.commandHelp().orNull());
        r.debug(this.debug().orNull());
        r.inherited(this.inherited().orNull());
        r.executionType(this.executionType().map(x -> x.id()).orNull());
        r.runAs(this.runAs().map(x -> x.toString()).orNull());
        r.archetype(this.archetype().orNull());
        r.storeStrategy(this.storeStrategy().map(x -> x.id()).orNull());
        {
            Map<NHomeLocation, String> c = this.homeLocations().orNull();
            Map<NBootHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NHomeLocation, String> e : c.entrySet()) {
                    v.put(NBootHomeLocation.of(
                            e.getKey().osFamily().id(),
                            e.getKey().storeType().id()
                    ), e.getValue());
                }
            }
            r.homeLocations(v);
        }
        {
            Map<NStoreType, String> c = this.storeLocations().orNull();
            Map<String, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NStoreType, String> e : c.entrySet()) {
                    v.put(e.getKey().id(), e.getValue());
                }
            }
            r.storeLocations(v);
        }
        r.storeLayout(this.storeLayout().map(x -> x.toString()).orNull());
        r.storeStrategy(this.storeStrategy().map(x -> x.toString()).orNull());
        r.repositoryStoreStrategy(this.repositoryStoreStrategy().map(x -> x.toString()).orNull());
        r.setchStrategy(this.fetchStrategy().map(x -> x.toString()).orNull());
        r.cached(this.cached().orNull());
        r.indexed(this.indexed().orNull());
        r.transitive(this.transitive().orNull());
        r.bot(this.bot().orNull());
        r.stdin(this.stdin().orNull());
        r.stdout(this.stdout().orNull());
        r.stderr(this.stderr().orNull());
        r.executorService(this.executorService().orNull());

        r.excludedExtensions(this.excludedExtensions().orNull());
        r.repositories(this.repositories().orNull());
        r.bootRepositories(this.bootRepositories().orNull());
        r.applicationArguments(this.applicationArguments().orNull());
        r.customOptions(this.customOptions().orNull());
        r.expireTime(this.expireTime().orNull());
        r.errors(this.errors().isNotPresent() ? new ArrayList<>() : this.errors().get().stream().map(x -> x.toString()).collect(Collectors.toList()));
        r.skipErrors(this.skipErrors().orNull());
        r.switchWorkspace(this.switchWorkspace().orNull());
        r.locale(this.locale().orNull());
        r.theme(this.theme().orNull());
        r.dependencySolver(this.dependencySolver().orNull());
        r.isolationLevel(this.isolationLevel().map(x -> x.id()).orNull());
        r.initLaunchers(this.initLaunchers().orNull());
        r.initJava(this.initJava().orNull());
        r.initScripts(this.initScripts().orNull());
        r.initPlatforms(this.initPlatforms().orNull());
        r.desktopLauncher(this.desktopLauncher().map(x -> x.id()).orNull());
        r.menuLauncher(this.menuLauncher().map(x -> x.id()).orNull());
        r.userLauncher(this.userLauncher().map(x -> x.id()).orNull());
        r.sharedInstance(this.sharedInstance().orNull());
        r.previewRepo(this.previewRepo().orNull());
        return r;
    }

    @Override
    public NOptional<Boolean> previewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    public NOptional<Boolean> sharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
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
    public NOptional<NVersion> apiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    @Override
    public NOptional<List<String>> applicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    @Override
    public NOptional<String> archetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    @Override
    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    @Override
    public NOptional<NConfirmationMode> confirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    @Override
    public NOptional<Boolean> dry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> showStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    @Override
    public NOptional<Instant> creationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    @Override
    public NOptional<List<String>> excludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    @Override
    public NOptional<NExecutionType> executionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    @Override
    public NOptional<NRunAs> runAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    @Override
    public NOptional<List<String>> executorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    @Override
    public NOptional<String> homeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "HomeLocation[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NOptional<String> javaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NOptional<String> javaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    @Override
    public NOptional<NLogConfig> logConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    @Override
    public NOptional<String> name() {
        return NOptional.ofNamed(name, "name");
    }

    @Override
    public NOptional<NOpenMode> openMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    @Override
    public NOptional<NContentType> outputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    @Override
    public NOptional<List<String>> outputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    @Override
    public NOptional<char[]> credential() {
        return NOptional.ofNamed(credentials == null ? null : Arrays.copyOf(credentials, credentials.length), "credentials");
    }

    @Override
    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    @Override
    public NOptional<NId> runtimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    @Override
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> storeLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    @Override
    public NOptional<NStoreStrategy> storeStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    @Override
    public NOptional<Map<NStoreType, String>> storeLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NOptional<NTerminalMode> terminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    @Override
    public NOptional<List<String>> repositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }

    @Override
    public NOptional<List<String>> bootRepositories() {
        return NOptional.ofNamed(bootRepositories, "initRepositories");
    }

    @Override
    public NOptional<String> userName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> workspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    @Override
    public NOptional<String> debug() {
        return NOptional.ofNamed(debug, "debug");
    }

    @Override
    public NOptional<Boolean> system() {
        return NOptional.ofNamed(system, "system");
    }

    @Override
    public NOptional<Boolean> gui() {
        return NOptional.ofNamed(gui, "gui");
    }

    @Override
    public NOptional<Boolean> inherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    @Override
    public NOptional<Boolean> readOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    @Override
    public NOptional<Boolean> recover() {
        return NOptional.ofNamed(recover, "recover");
    }

    @Override
    public NOptional<Boolean> reset() {
        return NOptional.ofNamed(reset, "reset");
    }

    @Override
    public NOptional<Boolean> resetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }

    @Override
    public NOptional<Boolean> commandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NOptional<Boolean> commandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NOptional<Boolean> installCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    @Override
    public NOptional<Boolean> skipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    @Override
    public NOptional<String> outLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NOptional<String> errLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NOptional<Boolean> skipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    @Override
    public NOptional<Boolean> trace() {
        return NOptional.ofNamed(trace, "trace");
    }

    public NOptional<String> progressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NOptional<Boolean> cached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NOptional<Boolean> indexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NOptional<Boolean> transitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NOptional<Boolean> bot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NOptional<NFetchStrategy> fetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NOptional<InputStream> stdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NOptional<PrintStream> stdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NOptional<PrintStream> stderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NOptional<ExecutorService> executorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NOptional<Instant> expireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NOptional<Boolean> skipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NOptional<Boolean> switchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    @Override
    public NOptional<List<NMsg>> errors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NOptional<List<String>> customOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NOptional<String> locale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NOptional<String> theme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NOptional<String> dependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public String toString() {
        return toCmdLine().toString();
    }

    @Override
    public NCmdLine toCmdLine() {
        return toCmdLine(new NWorkspaceOptionsConfig());
    }

    @Override
    public NCmdLine toCmdLine(NWorkspaceOptionsConfig config) {
        return new NReservedWorkspaceOptionsToCmdLineBuilder(config, this).toCmdLine();
    }

    @Override
    public NWorkspaceOptionsBuilder builder() {
        return NWorkspaceOptionsBuilder.of().copyFrom(this);
    }

}
