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
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public class DefaultNBootOptions implements NBootOptions {

    private static final long serialVersionUID = 1;

    private final List<String> bootRepositories;
    /**
     * special
     */
    private final NClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private final List<NBootDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private final List<NClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private final NBootWorkspaceFactory bootWorkspaceFactory;

    /**
     * special
     */
    private final List<URL> classWorldURLs;

    /**
     * special
     */
    private final ClassLoader classWorldLoader;

    /**
     * special
     */
    private final String uuid;

    /**
     * special
     */
    private final Set<String> extensionsSet;

    /**
     * special
     */
    private final NBootDescriptor runtimeBootDescriptor;

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

    public DefaultNBootOptions(List<String> outputFormatOptions, List<String> customOptions, NVersion apiVersion,
                               NId runtimeId, String javaCommand, String javaOptions, String workspace,
                               String outLinePrefix, String errLinePrefix, String name, Boolean installCompanions,
                               Boolean skipWelcome, Boolean skipBoot, Boolean system, Boolean gui,
                               Boolean dry, Boolean showStacktrace, Boolean recover, Boolean reset, Boolean resetHard, Boolean commandVersion, Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, NIsolationLevel isolationLevel, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, List<String> excludedExtensions, List<String> repositories, String userName,
                               char[] credentials, NTerminalMode terminalMode, Boolean readOnly,
                               Boolean trace, String progressOptions, String dependencySolver,
                               NLogConfig logConfig, NConfirmationMode confirm, NContentType outputFormat,
                               List<String> applicationArguments, NOpenMode openMode, Instant creationTime,
                               Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions,
                               String debug, NExecutionType executionType, NRunAs runAs,
                               String archetype, Map<NStoreType, String> storeLocations,
                               Map<NHomeLocation, String> homeLocations, NOsFamily storeLayout,
                               NStoreStrategy storeStrategy,
                               NStoreStrategy repositoryStoreStrategy, NFetchStrategy fetchStrategy,
                               InputStream stdin,
                               PrintStream stdout, PrintStream stderr, ExecutorService executorService,
                               Instant expireTime, List<NMsg> errors, Boolean skipErrors, String locale,
                               String theme, String uuid, List<String> bootRepositories, NClassLoaderNode runtimeBootDependencyNode,
                               List<NBootDescriptor> extensionBootDescriptors, List<NClassLoaderNode> extensionBootDependencyNodes,
                               List<URL> classWorldURLs, Set<String> extensionsSet, NBootWorkspaceFactory bootWorkspaceFactory, NBootDescriptor runtimeBootDescriptor, ClassLoader classWorldLoader,
                               NSupportMode desktopLauncher, NSupportMode menuLauncher, NSupportMode userLauncher, Boolean previewRepo, Boolean sharedInstance) {
        ;
        this.outputFormatOptions = NReservedLangUtils.unmodifiableOrNullList(outputFormatOptions);
        this.customOptions = NReservedLangUtils.unmodifiableOrNullList(customOptions);
        this.excludedExtensions = NReservedLangUtils.unmodifiableOrNullList(excludedExtensions);
        this.repositories = NReservedLangUtils.unmodifiableOrNullList(repositories);
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
        this.bootRepositories = bootRepositories;
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        this.extensionBootDescriptors = NReservedLangUtils.unmodifiableOrNullList(extensionBootDescriptors);
        this.extensionBootDependencyNodes = NReservedLangUtils.unmodifiableOrNullList(extensionBootDependencyNodes);
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        this.classWorldURLs = NReservedLangUtils.unmodifiableOrNullList(classWorldURLs);
        this.classWorldLoader = classWorldLoader;
        this.uuid = uuid;
        this.extensionsSet = NReservedLangUtils.unmodifiableOrNullSet(extensionsSet);
        this.runtimeBootDescriptor = runtimeBootDescriptor;
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
    public NOptional<String> getHomeLocation(NHomeLocation location) {
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
    public NOptional<List<NArg>> customOptionArgs() {
        return NOptional.ofNamed(customOptions == null ? null : customOptions.stream().map(x -> NArg.of(x)).collect(Collectors.toList()), "customOptions");
    }

    @Override
    public NOptional<NArg> customOptionArg(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .findFirst(), key);
    }

    @Override
    public NOptional<String> customOption(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                        .map(x->x.image())
                .findFirst(), key);
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
    public NBootOptions toReadOnly() {
        return this;
    }

    @Override
    public NCmdLine toCmdLine() {
        return toCmdLine(new NWorkspaceOptionsConfig());
    }

    @Override
    public NCmdLine toCmdLine(NWorkspaceOptionsConfig config) {
        return toWorkspaceOptions().toCmdLine(config);
    }

    @Override
    public NBootOptionsBuilder builder() {
        return NBootOptionsBuilder.of().copyFrom(this);
    }

    @Override
    public NOptional<List<String>> bootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    @Override
    public NOptional<NClassLoaderNode> runtimeBootDependencyNode() {
        return NOptional.ofNamed(runtimeBootDependencyNode, "runtimeBootDependencyNode");
    }

    @Override
    public NOptional<List<NBootDescriptor>> extensionBootDescriptors() {
        return NOptional.ofNamed(extensionBootDescriptors, "extensionBootDescriptors");
    }

    @Override
    public NOptional<List<NClassLoaderNode>> extensionBootDependencyNodes() {
        return NOptional.ofNamed(extensionBootDependencyNodes, "extensionBootDependencyNodes");
    }

    @Override
    public NOptional<NBootWorkspaceFactory> bootWorkspaceFactory() {
        return NOptional.ofNamed(bootWorkspaceFactory, "bootWorkspaceFactory");
    }

    @Override
    public NOptional<List<URL>> classWorldURLs() {
        return NOptional.ofNamed(classWorldURLs, "classWorldURLs");
    }

    @Override
    public NOptional<ClassLoader> classWorldLoader() {
        return NOptional.ofNamed(classWorldLoader, "classWorldLoader");
    }

    @Override
    public NOptional<String> uuid() {
        return NOptional.ofNamed(uuid, "uuid");
    }

    @Override
    public NOptional<Set<String>> extensionsSet() {
        return NOptional.ofNamed(extensionsSet, "extensionsSet");
    }

    @Override
    public NOptional<NBootDescriptor> runtimeBootDescriptor() {
        return NOptional.ofNamed(runtimeBootDescriptor, "runtimeBootDescriptor");
    }

    /// ///////////

    public NBootOptionsInfo toBootOptions() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.setApiVersion(this.apiVersion().map(Object::toString).orNull());
        r.setRuntimeId(this.runtimeId().map(Object::toString).orNull());
        r.setJavaCommand(this.javaCommand().orNull());
        r.setJavaOptions(this.javaOptions().orNull());
        r.setWorkspace(this.workspace().orNull());
        r.setName(this.name().orNull());
        r.setInstallCompanions(this.installCompanions().orNull());
        r.setSkipWelcome(this.skipWelcome().orNull());
        r.setSkipBoot(this.skipBoot().orNull());
        r.setSystem(this.system().orNull());
        r.setGui(this.gui().orNull());
        r.setUserName(this.userName().orNull());
        r.setCredential(this.credential().orNull());
        r.setTerminalMode(this.terminalMode().map(NTerminalMode::id).orNull());
        r.setReadOnly(this.readOnly().orNull());
        r.setTrace(this.trace().orNull());
        r.setProgressOptions(this.progressOptions().orNull());
        {
            NLogConfig c = this.logConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.setLogFileBase(c.logFileBase());
                v.setLogFileLevel(c.logFileLevel());
                v.setLogTermLevel(c.logTermLevel());
                v.setLogFileSize(c.logFileSize());
                v.setLogFileCount(c.logFileCount());
                v.setLogFileName(c.logFileName());
                v.setLogFileBase(c.logFileBase());
            }
            r.setLogConfig(v);
        }
        r.setConfirm(this.confirm().map(NConfirmationMode::id).orNull());
        r.setConfirm(this.confirm().map(NConfirmationMode::id).orNull());
        r.setOutputFormat(this.outputFormat().map(NContentType::id).orNull());
        r.setOutputFormatOptions(this.outputFormatOptions().orNull());
        r.setOpenMode(this.openMode().map(NOpenMode::id).orNull());
        r.setCreationTime(this.creationTime().orNull());
        r.setDry(this.dry().orNull());
        r.setShowStacktrace(this.showStacktrace().orNull());
        r.setClassLoaderSupplier(this.classLoaderSupplier().orNull());
        r.setExecutorOptions(this.executorOptions().orNull());
        r.setRecover(this.recover().orNull());
        r.setReset(this.reset().orNull());
        r.setResetHard(this.resetHard().orNull());
        r.setCommandVersion(this.commandVersion().orNull());
        r.setCommandHelp(this.commandHelp().orNull());
        r.setDebug(this.debug().orNull());
        r.setInherited(this.inherited().orNull());
        r.setExecutionType(this.executionType().map(NExecutionType::id).orNull());
        r.setRunAs(this.runAs().map(NRunAs::toString).orNull());
        r.setArchetype(this.archetype().orNull());
        r.setStoreStrategy(this.storeStrategy().map(NStoreStrategy::id).orNull());
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
            r.setHomeLocations(v);
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
            r.setStoreLocations(v);
        }
        r.setStoreLayout(this.storeLayout().map(Enum::toString).orNull());
        r.setStoreStrategy(this.storeStrategy().map(Enum::toString).orNull());
        r.setRepositoryStoreStrategy(this.repositoryStoreStrategy().map(Enum::toString).orNull());
        r.setFetchStrategy(this.fetchStrategy().map(Enum::toString).orNull());
        r.setCached(this.cached().orNull());
        r.setIndexed(this.indexed().orNull());
        r.setTransitive(this.transitive().orNull());
        r.setBot(this.bot().orNull());
        r.setStdin(this.stdin().orNull());
        r.setStdout(this.stdout().orNull());
        r.setStderr(this.stderr().orNull());
        r.setExecutorService(this.executorService().orNull());
//        r.setBootRepositories(this.getBootRepositories());

        r.setExcludedExtensions(this.excludedExtensions().orNull());
//        r.setExcludedRepositories(this.getExcludedRepositories() == null ? null : Arrays.copyOf(this.getExcludedRepositories(), this.getExcludedRepositories().length));
        r.setRepositories(this.repositories().orNull());
        r.setApplicationArguments(this.applicationArguments().orNull());
        r.setCustomOptions(this.customOptions().orNull());
        r.setExpireTime(this.expireTime().orNull());
        r.setErrors(this.errors().isNotPresent() ? new ArrayList<>() : this.errors().get().stream().map(NMsg::toString).collect(Collectors.toList()));
        r.setSkipErrors(this.skipErrors().orNull());
        r.setSwitchWorkspace(this.switchWorkspace().orNull());
        r.setLocale(this.locale().orNull());
        r.setTheme(this.theme().orNull());
        r.setDependencySolver(this.dependencySolver().orNull());
        r.setIsolationLevel(this.isolationLevel().map(NIsolationLevel::id).orNull());
        r.setInitLaunchers(this.initLaunchers().orNull());
        r.setInitJava(this.initJava().orNull());
        r.setInitScripts(this.initScripts().orNull());
        r.setInitPlatforms(this.initPlatforms().orNull());
        r.setDesktopLauncher(this.desktopLauncher().map(NSupportMode::id).orNull());
        r.setMenuLauncher(this.menuLauncher().map(NSupportMode::id).orNull());
        r.setUserLauncher(this.userLauncher().map(NSupportMode::id).orNull());
        r.setSharedInstance(this.sharedInstance().orNull());
        r.setPreviewRepo(this.previewRepo().orNull());

        r.setBootRepositories(this.bootRepositories().orNull());
        r.setRuntimeBootDependencyNode(convertNode(this.runtimeBootDependencyNode().orNull()));
        r.setExtensionBootDescriptors(this.extensionBootDescriptors().orNull());
        r.setExtensionBootDependencyNodes(convertNodes(this.extensionBootDependencyNodes().orNull()));
        r.setBootWorkspaceFactory(this.bootWorkspaceFactory().orNull());
        r.setClassWorldURLs(this.classWorldURLs().orNull());
        r.setClassWorldLoader(this.classWorldLoader().orNull());
        r.setUuid(this.uuid().orNull());
        r.setExtensionsSet(this.extensionsSet().orNull());
        r.setRuntimeBootDescriptor(this.runtimeBootDescriptor().orNull());

        return r;
    }

    private List<NBootClassLoaderNode> convertNodes(List<NClassLoaderNode> dependencies) {
        return dependencies == null ? null : dependencies.stream().map(this::convertNode).collect(Collectors.toList());
    }

    private NBootClassLoaderNode convertNode(NClassLoaderNode n) {
        if (n == null) {
            return null;
        }
        List<NClassLoaderNode> dependencies = n.dependencies();
        List<NBootClassLoaderNode> children = convertNodes(dependencies);
        return new NBootClassLoaderNode(
                n.id() == null ? null : n.id().toString(),
                n.url(),
                n.isEnabled(),
                n.isIncludedInClasspath(),
                children == null ? null : children.toArray(new NBootClassLoaderNode[0])
        );
    }

    @Override
    public NWorkspaceOptions toWorkspaceOptions() {
        NWorkspaceOptionsBuilder b = NWorkspaceOptionsBuilder.of();
        b.apiVersion(this.apiVersion().orNull());
        b.runtimeId(this.runtimeId().orNull());
        b.javaCommand(this.javaCommand().orNull());
        b.javaOptions(this.javaOptions().orNull());
        b.workspace(this.workspace().orNull());
        b.name(this.name().orNull());
        b.installCompanions(this.installCompanions().orNull());
        b.skipWelcome(this.skipWelcome().orNull());
        b.skipBoot(this.skipBoot().orNull());
        b.system(this.system().orNull());
        b.gui(this.gui().orNull());
        b.setUserName(this.userName().orNull());
        b.credential(this.credential().orNull());
        b.terminalMode(this.terminalMode().orNull());
        b.readOnly(this.readOnly().orNull());
        b.trace(this.trace().orNull());
        b.progressOptions(this.progressOptions().orNull());
        b.logConfig(this.logConfig().orNull());
        b.confirm(this.confirm().orNull());
        b.confirm(this.confirm().orNull());
        b.outputFormat(this.outputFormat().orNull());
        b.outputFormatOptions(this.outputFormatOptions().orNull());
        b.openMode(this.openMode().orNull());
        b.creationTime(this.creationTime().orNull());
        b.dry(this.dry().orNull());
        b.showStacktrace(this.showStacktrace().orNull());
        b.classLoaderSupplier(this.classLoaderSupplier().orNull());
        b.executorOptions(this.executorOptions().orNull());
        b.recover(this.recover().orNull());
        b.reset(this.reset().orNull());
        b.resetHard(this.resetHard().orNull());
        b.commandVersion(this.commandVersion().orNull());
        b.commandHelp(this.commandHelp().orNull());
        b.debug(this.debug().orNull());
        b.inherited(this.inherited().orNull());
        b.executionType(this.executionType().orNull());
        b.runAs(this.runAs().orNull());
        b.archetype(this.archetype().orNull());
        b.storeStrategy(this.storeStrategy().orNull());
        b.homeLocations(this.homeLocations().orNull());
        b.storeLocations(this.storeLocations().orNull());
        b.storeLayout(this.storeLayout().orNull());
        b.storeStrategy(this.storeStrategy().orNull());
        b.repositoryStoreStrategy(this.repositoryStoreStrategy().orNull());
        b.fetchStrategy(this.fetchStrategy().orNull());
        b.cached(this.cached().orNull());
        b.indexed(this.indexed().orNull());
        b.transitive(this.transitive().orNull());
        b.bot(this.bot().orNull());
        b.stdin(this.stdin().orNull());
        b.stdout(this.stdout().orNull());
        b.stderr(this.stderr().orNull());
        b.executorService(this.executorService().orNull());
        b.excludedExtensions(this.excludedExtensions().orNull());
        b.repositories(this.repositories().orNull());
        b.applicationArguments(this.applicationArguments().orNull());
        b.customOptions(this.customOptions().orNull());
        b.expireTime(this.expireTime().orNull());
        b.errors(this.errors().orNull());
        b.skipErrors(this.skipErrors().orNull());
        b.switchWorkspace(this.switchWorkspace().orNull());
        b.locale(this.locale().orNull());
        b.theme(this.theme().orNull());
        b.dependencySolver(this.dependencySolver().orNull());
        b.isolationLevel(this.isolationLevel().orNull());
        b.initLaunchers(this.initLaunchers().orNull());
        b.initJava(this.initJava().orNull());
        b.initScripts(this.initScripts().orNull());
        b.initPlatforms(this.initPlatforms().orNull());
        b.desktopLauncher(this.desktopLauncher().orNull());
        b.menuLauncher(this.menuLauncher().orNull());
        b.userLauncher(this.userLauncher().orNull());
        b.sharedInstance(this.sharedInstance().orNull());
        b.previewRepo(this.previewRepo().orNull());
        return b.build();
    }

}
