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

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedWorkspaceOptionsToCmdLineBuilder;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.util.NSupportMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
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
public class DefaultNWorkspaceOptions implements Serializable, NWorkspaceOptions {

    public static NWorkspaceOptions BLANK = new DefaultNWorkspaceOptions(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null,
            null,
            null, null, null,
            null, null, null);

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
//    private String bootRepositories = null;
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
                                    Boolean trace, Boolean dry, Boolean showStacktrace, Boolean recover, Boolean reset, Boolean commandVersion,
                                    Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached,
                                    Boolean indexed, Boolean transitive, Boolean bot, Boolean skipErrors,
                                    NIsolationLevel isolationLevel, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, InputStream stdin, PrintStream stdout, PrintStream stderr,
                                    ExecutorService executorService, Supplier<ClassLoader> classLoaderSupplier,
                                    List<String> applicationArguments, List<String> outputFormatOptions,
                                    List<String> customOptions, List<String> excludedExtensions, List<String> repositories,
                                    List<String> executorOptions, List<NMsg> errors, Map<NStoreType, String> storeLocations,
                                    Map<NHomeLocation, String> homeLocations, NSupportMode desktopLauncher, NSupportMode menuLauncher, NSupportMode userLauncher) {
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
    public NWorkspaceOptions readOnly() {
        return this;
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
        return new DefaultNWorkspaceOptionsBuilder().setAll(this);
    }

}
