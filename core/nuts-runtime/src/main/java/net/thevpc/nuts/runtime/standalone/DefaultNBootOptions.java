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
import net.thevpc.nuts.util.NMsg;
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
    public NOptional<Boolean> getPreviewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    public NOptional<Boolean> getSharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
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
    public NOptional<NVersion> getApiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    @Override
    public NOptional<List<String>> getApplicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    @Override
    public NOptional<String> getArchetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    @Override
    public NOptional<Supplier<ClassLoader>> getClassLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    @Override
    public NOptional<NConfirmationMode> getConfirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    @Override
    public NOptional<Boolean> getDry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> getShowStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    @Override
    public NOptional<Instant> getCreationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    @Override
    public NOptional<List<String>> getExcludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    @Override
    public NOptional<NExecutionType> getExecutionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    @Override
    public NOptional<NRunAs> getRunAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    @Override
    public NOptional<List<String>> getExecutorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    @Override
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "HomeLocation[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> getHomeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NOptional<String> getJavaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NOptional<String> getJavaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    @Override
    public NOptional<NLogConfig> getLogConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    @Override
    public NOptional<String> getName() {
        return NOptional.ofNamed(name, "name");
    }

    @Override
    public NOptional<NOpenMode> getOpenMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    @Override
    public NOptional<NContentType> getOutputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    @Override
    public NOptional<List<String>> getOutputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    @Override
    public NOptional<char[]> getCredentials() {
        return NOptional.ofNamed(credentials == null ? null : Arrays.copyOf(credentials, credentials.length), "credentials");
    }

    @Override
    public NOptional<NStoreStrategy> getRepositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    @Override
    public NOptional<NId> getRuntimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    @Override
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> getStoreLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    @Override
    public NOptional<NStoreStrategy> getStoreStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    @Override
    public NOptional<Map<NStoreType, String>> getStoreLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NOptional<NTerminalMode> getTerminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    @Override
    public NOptional<List<String>> getRepositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }

    @Override
    public NOptional<String> getUserName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> getWorkspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    @Override
    public NOptional<String> getDebug() {
        return NOptional.ofNamed(debug, "debug");
    }

    @Override
    public NOptional<Boolean> getSystem() {
        return NOptional.ofNamed(system, "system");
    }

    @Override
    public NOptional<Boolean> getGui() {
        return NOptional.ofNamed(gui, "gui");
    }

    @Override
    public NOptional<Boolean> getInherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    @Override
    public NOptional<Boolean> getReadOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    @Override
    public NOptional<Boolean> getRecover() {
        return NOptional.ofNamed(recover, "recover");
    }

    @Override
    public NOptional<Boolean> getReset() {
        return NOptional.ofNamed(reset, "reset");
    }

    public NOptional<Boolean> getResetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }

    @Override
    public NOptional<Boolean> getCommandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NOptional<Boolean> getCommandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NOptional<Boolean> getInstallCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    @Override
    public NOptional<Boolean> getSkipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    @Override
    public NOptional<String> getOutLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NOptional<String> getErrLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NOptional<Boolean> getSkipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    @Override
    public NOptional<Boolean> getTrace() {
        return NOptional.ofNamed(trace, "trace");
    }

    public NOptional<String> getProgressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NOptional<Boolean> getCached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NOptional<Boolean> getIndexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NOptional<Boolean> getTransitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NOptional<Boolean> getBot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NOptional<NFetchStrategy> getFetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NOptional<InputStream> getStdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NOptional<PrintStream> getStdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NOptional<PrintStream> getStderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NOptional<ExecutorService> getExecutorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NOptional<Instant> getExpireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NOptional<Boolean> getSkipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NOptional<Boolean> getSwitchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    @Override
    public NOptional<List<NMsg>> getErrors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NOptional<List<String>> getCustomOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NOptional<List<NArg>> getCustomOptionArgs() {
        return NOptional.ofNamed(customOptions == null ? null : customOptions.stream().map(x -> NArg.of(x)).collect(Collectors.toList()), "customOptions");
    }

    @Override
    public NOptional<NArg> getCustomOptionArg(String key) {
        return NOptional.ofNamedOptional(getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .findFirst(), key);
    }

    @Override
    public NOptional<String> getCustomOption(String key) {
        return NOptional.ofNamedOptional(getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                        .map(x->x.image())
                .findFirst(), key);
    }

    @Override
    public NOptional<String> getLocale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NOptional<String> getTheme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NOptional<String> getDependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public String toString() {
        return toCmdLine().toString();
    }

    @Override
    public NBootOptions readOnly() {
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
    public NOptional<List<String>> getBootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    @Override
    public NOptional<NClassLoaderNode> getRuntimeBootDependencyNode() {
        return NOptional.ofNamed(runtimeBootDependencyNode, "runtimeBootDependencyNode");
    }

    @Override
    public NOptional<List<NBootDescriptor>> getExtensionBootDescriptors() {
        return NOptional.ofNamed(extensionBootDescriptors, "extensionBootDescriptors");
    }

    @Override
    public NOptional<List<NClassLoaderNode>> getExtensionBootDependencyNodes() {
        return NOptional.ofNamed(extensionBootDependencyNodes, "extensionBootDependencyNodes");
    }

    @Override
    public NOptional<NBootWorkspaceFactory> getBootWorkspaceFactory() {
        return NOptional.ofNamed(bootWorkspaceFactory, "bootWorkspaceFactory");
    }

    @Override
    public NOptional<List<URL>> getClassWorldURLs() {
        return NOptional.ofNamed(classWorldURLs, "classWorldURLs");
    }

    @Override
    public NOptional<ClassLoader> getClassWorldLoader() {
        return NOptional.ofNamed(classWorldLoader, "classWorldLoader");
    }

    @Override
    public NOptional<String> getUuid() {
        return NOptional.ofNamed(uuid, "uuid");
    }

    @Override
    public NOptional<Set<String>> getExtensionsSet() {
        return NOptional.ofNamed(extensionsSet, "extensionsSet");
    }

    @Override
    public NOptional<NBootDescriptor> getRuntimeBootDescriptor() {
        return NOptional.ofNamed(runtimeBootDescriptor, "runtimeBootDescriptor");
    }

    /// ///////////

    public NBootOptionsInfo toBootOptions() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.setApiVersion(this.getApiVersion().map(Object::toString).orNull());
        r.setRuntimeId(this.getRuntimeId().map(Object::toString).orNull());
        r.setJavaCommand(this.getJavaCommand().orNull());
        r.setJavaOptions(this.getJavaOptions().orNull());
        r.setWorkspace(this.getWorkspace().orNull());
        r.setName(this.getName().orNull());
        r.setInstallCompanions(this.getInstallCompanions().orNull());
        r.setSkipWelcome(this.getSkipWelcome().orNull());
        r.setSkipBoot(this.getSkipBoot().orNull());
        r.setSystem(this.getSystem().orNull());
        r.setGui(this.getGui().orNull());
        r.setUserName(this.getUserName().orNull());
        r.setCredentials(this.getCredentials().orNull());
        r.setTerminalMode(this.getTerminalMode().map(NTerminalMode::id).orNull());
        r.setReadOnly(this.getReadOnly().orNull());
        r.setTrace(this.getTrace().orNull());
        r.setProgressOptions(this.getProgressOptions().orNull());
        {
            NLogConfig c = this.getLogConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.setLogFileBase(c.getLogFileBase());
                v.setLogFileLevel(c.getLogFileLevel());
                v.setLogTermLevel(c.getLogTermLevel());
                v.setLogFileSize(c.getLogFileSize());
                v.setLogFileCount(c.getLogFileCount());
                v.setLogFileName(c.getLogFileName());
                v.setLogFileBase(c.getLogFileBase());
            }
            r.setLogConfig(v);
        }
        r.setConfirm(this.getConfirm().map(NConfirmationMode::id).orNull());
        r.setConfirm(this.getConfirm().map(NConfirmationMode::id).orNull());
        r.setOutputFormat(this.getOutputFormat().map(NContentType::id).orNull());
        r.setOutputFormatOptions(this.getOutputFormatOptions().orNull());
        r.setOpenMode(this.getOpenMode().map(NOpenMode::id).orNull());
        r.setCreationTime(this.getCreationTime().orNull());
        r.setDry(this.getDry().orNull());
        r.setShowStacktrace(this.getShowStacktrace().orNull());
        r.setClassLoaderSupplier(this.getClassLoaderSupplier().orNull());
        r.setExecutorOptions(this.getExecutorOptions().orNull());
        r.setRecover(this.getRecover().orNull());
        r.setReset(this.getReset().orNull());
        r.setResetHard(this.getResetHard().orNull());
        r.setCommandVersion(this.getCommandVersion().orNull());
        r.setCommandHelp(this.getCommandHelp().orNull());
        r.setDebug(this.getDebug().orNull());
        r.setInherited(this.getInherited().orNull());
        r.setExecutionType(this.getExecutionType().map(NExecutionType::id).orNull());
        r.setRunAs(this.getRunAs().map(NRunAs::toString).orNull());
        r.setArchetype(this.getArchetype().orNull());
        r.setStoreStrategy(this.getStoreStrategy().map(NStoreStrategy::id).orNull());
        {
            Map<NHomeLocation, String> c = this.getHomeLocations().orNull();
            Map<NBootHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NHomeLocation, String> e : c.entrySet()) {
                    v.put(NBootHomeLocation.of(
                            e.getKey().getOsFamily().id(),
                            e.getKey().getStoreLocation().id()
                    ), e.getValue());
                }
            }
            r.setHomeLocations(v);
        }
        {
            Map<NStoreType, String> c = this.getStoreLocations().orNull();
            Map<String, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NStoreType, String> e : c.entrySet()) {
                    v.put(e.getKey().id(), e.getValue());
                }
            }
            r.setStoreLocations(v);
        }
        r.setStoreLayout(this.getStoreLayout().map(Enum::toString).orNull());
        r.setStoreStrategy(this.getStoreStrategy().map(Enum::toString).orNull());
        r.setRepositoryStoreStrategy(this.getRepositoryStoreStrategy().map(Enum::toString).orNull());
        r.setFetchStrategy(this.getFetchStrategy().map(Enum::toString).orNull());
        r.setCached(this.getCached().orNull());
        r.setIndexed(this.getIndexed().orNull());
        r.setTransitive(this.getTransitive().orNull());
        r.setBot(this.getBot().orNull());
        r.setStdin(this.getStdin().orNull());
        r.setStdout(this.getStdout().orNull());
        r.setStderr(this.getStderr().orNull());
        r.setExecutorService(this.getExecutorService().orNull());
//        r.setBootRepositories(this.getBootRepositories());

        r.setExcludedExtensions(this.getExcludedExtensions().orNull());
//        r.setExcludedRepositories(this.getExcludedRepositories() == null ? null : Arrays.copyOf(this.getExcludedRepositories(), this.getExcludedRepositories().length));
        r.setRepositories(this.getRepositories().orNull());
        r.setApplicationArguments(this.getApplicationArguments().orNull());
        r.setCustomOptions(this.getCustomOptions().orNull());
        r.setExpireTime(this.getExpireTime().orNull());
        r.setErrors(this.getErrors().isNotPresent() ? new ArrayList<>() : this.getErrors().get().stream().map(NMsg::toString).collect(Collectors.toList()));
        r.setSkipErrors(this.getSkipErrors().orNull());
        r.setSwitchWorkspace(this.getSwitchWorkspace().orNull());
        r.setLocale(this.getLocale().orNull());
        r.setTheme(this.getTheme().orNull());
        r.setDependencySolver(this.getDependencySolver().orNull());
        r.setIsolationLevel(this.getIsolationLevel().map(NIsolationLevel::id).orNull());
        r.setInitLaunchers(this.getInitLaunchers().orNull());
        r.setInitJava(this.getInitJava().orNull());
        r.setInitScripts(this.getInitScripts().orNull());
        r.setInitPlatforms(this.getInitPlatforms().orNull());
        r.setDesktopLauncher(this.getDesktopLauncher().map(NSupportMode::id).orNull());
        r.setMenuLauncher(this.getMenuLauncher().map(NSupportMode::id).orNull());
        r.setUserLauncher(this.getUserLauncher().map(NSupportMode::id).orNull());
        r.setSharedInstance(this.getSharedInstance().orNull());
        r.setPreviewRepo(this.getPreviewRepo().orNull());

        r.setBootRepositories(this.getBootRepositories().orNull());
        r.setRuntimeBootDependencyNode(convertNode(this.getRuntimeBootDependencyNode().orNull()));
        r.setExtensionBootDescriptors(this.getExtensionBootDescriptors().orNull());
        r.setExtensionBootDependencyNodes(convertNodes(this.getExtensionBootDependencyNodes().orNull()));
        r.setBootWorkspaceFactory(this.getBootWorkspaceFactory().orNull());
        r.setClassWorldURLs(this.getClassWorldURLs().orNull());
        r.setClassWorldLoader(this.getClassWorldLoader().orNull());
        r.setUuid(this.getUuid().orNull());
        r.setExtensionsSet(this.getExtensionsSet().orNull());
        r.setRuntimeBootDescriptor(this.getRuntimeBootDescriptor().orNull());

        return r;
    }

    private List<NBootClassLoaderNode> convertNodes(List<NClassLoaderNode> dependencies) {
        return dependencies == null ? null : dependencies.stream().map(this::convertNode).collect(Collectors.toList());
    }

    private NBootClassLoaderNode convertNode(NClassLoaderNode n) {
        if (n == null) {
            return null;
        }
        List<NClassLoaderNode> dependencies = n.getDependencies();
        List<NBootClassLoaderNode> children = convertNodes(dependencies);
        return new NBootClassLoaderNode(
                n.getId() == null ? null : n.getId().toString(),
                n.getURL(),
                n.isEnabled(),
                n.isIncludedInClasspath(),
                children == null ? null : children.toArray(new NBootClassLoaderNode[0])
        );
    }

    @Override
    public NWorkspaceOptions toWorkspaceOptions() {
        NWorkspaceOptionsBuilder b = NWorkspaceOptionsBuilder.of();
        b.setApiVersion(this.getApiVersion().orNull());
        b.setRuntimeId(this.getRuntimeId().orNull());
        b.setJavaCommand(this.getJavaCommand().orNull());
        b.setJavaOptions(this.getJavaOptions().orNull());
        b.setWorkspace(this.getWorkspace().orNull());
        b.setName(this.getName().orNull());
        b.setInstallCompanions(this.getInstallCompanions().orNull());
        b.setSkipWelcome(this.getSkipWelcome().orNull());
        b.setSkipBoot(this.getSkipBoot().orNull());
        b.setSystem(this.getSystem().orNull());
        b.setGui(this.getGui().orNull());
        b.setUserName(this.getUserName().orNull());
        b.setCredentials(this.getCredentials().orNull());
        b.setTerminalMode(this.getTerminalMode().orNull());
        b.setReadOnly(this.getReadOnly().orNull());
        b.setTrace(this.getTrace().orNull());
        b.setProgressOptions(this.getProgressOptions().orNull());
        b.setLogConfig(this.getLogConfig().orNull());
        b.setConfirm(this.getConfirm().orNull());
        b.setConfirm(this.getConfirm().orNull());
        b.setOutputFormat(this.getOutputFormat().orNull());
        b.setOutputFormatOptions(this.getOutputFormatOptions().orNull());
        b.setOpenMode(this.getOpenMode().orNull());
        b.setCreationTime(this.getCreationTime().orNull());
        b.setDry(this.getDry().orNull());
        b.setShowStacktrace(this.getShowStacktrace().orNull());
        b.setClassLoaderSupplier(this.getClassLoaderSupplier().orNull());
        b.setExecutorOptions(this.getExecutorOptions().orNull());
        b.setRecover(this.getRecover().orNull());
        b.setReset(this.getReset().orNull());
        b.setResetHard(this.getResetHard().orNull());
        b.setCommandVersion(this.getCommandVersion().orNull());
        b.setCommandHelp(this.getCommandHelp().orNull());
        b.setDebug(this.getDebug().orNull());
        b.setInherited(this.getInherited().orNull());
        b.setExecutionType(this.getExecutionType().orNull());
        b.setRunAs(this.getRunAs().orNull());
        b.setArchetype(this.getArchetype().orNull());
        b.setStoreStrategy(this.getStoreStrategy().orNull());
        b.setHomeLocations(this.getHomeLocations().orNull());
        b.setStoreLocations(this.getStoreLocations().orNull());
        b.setStoreLayout(this.getStoreLayout().orNull());
        b.setStoreStrategy(this.getStoreStrategy().orNull());
        b.setRepositoryStoreStrategy(this.getRepositoryStoreStrategy().orNull());
        b.setFetchStrategy(this.getFetchStrategy().orNull());
        b.setCached(this.getCached().orNull());
        b.setIndexed(this.getIndexed().orNull());
        b.setTransitive(this.getTransitive().orNull());
        b.setBot(this.getBot().orNull());
        b.setStdin(this.getStdin().orNull());
        b.setStdout(this.getStdout().orNull());
        b.setStderr(this.getStderr().orNull());
        b.setExecutorService(this.getExecutorService().orNull());
        b.setExcludedExtensions(this.getExcludedExtensions().orNull());
        b.setRepositories(this.getRepositories().orNull());
        b.setApplicationArguments(this.getApplicationArguments().orNull());
        b.setCustomOptions(this.getCustomOptions().orNull());
        b.setExpireTime(this.getExpireTime().orNull());
        b.setErrors(this.getErrors().orNull());
        b.setSkipErrors(this.getSkipErrors().orNull());
        b.setSwitchWorkspace(this.getSwitchWorkspace().orNull());
        b.setLocale(this.getLocale().orNull());
        b.setTheme(this.getTheme().orNull());
        b.setDependencySolver(this.getDependencySolver().orNull());
        b.setIsolationLevel(this.getIsolationLevel().orNull());
        b.setInitLaunchers(this.getInitLaunchers().orNull());
        b.setInitJava(this.getInitJava().orNull());
        b.setInitScripts(this.getInitScripts().orNull());
        b.setInitPlatforms(this.getInitPlatforms().orNull());
        b.setDesktopLauncher(this.getDesktopLauncher().orNull());
        b.setMenuLauncher(this.getMenuLauncher().orNull());
        b.setUserLauncher(this.getUserLauncher().orNull());
        b.setSharedInstance(this.getSharedInstance().orNull());
        b.setPreviewRepo(this.getPreviewRepo().orNull());
        return b.build();
    }

}
