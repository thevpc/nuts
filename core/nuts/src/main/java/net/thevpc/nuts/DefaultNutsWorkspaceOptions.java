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

import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.boot.PrivateNutsWorkspaceOptionsArgumentsBuilder;

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
public class DefaultNutsWorkspaceOptions implements Serializable, NutsWorkspaceOptions {
    public static NutsWorkspaceOptions BLANK = new DefaultNutsWorkspaceOptions(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
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
    private final NutsVersion apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * child workspaces)
     */
    private final NutsId runtimeId;

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
    private final Boolean skipCompanions;

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
     * if true consider global/system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean global;

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
    private final NutsTerminalMode terminalMode;

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
    private final NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final NutsOpenMode openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Instant creationTime;

    /**
     * if true no real execution, wil dry exec option-type : runtime (available
     * only for the current workspace instance)
     */
    private final Boolean dry;

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
    private final NutsExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private final NutsRunAs runAs;

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
    private final Map<NutsStoreLocation, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final Map<NutsHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsOsFamily storeLocationLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsStoreLocationStrategy storeLocationStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsStoreLocationStrategy repositoryStoreLocationStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsFetchStrategy fetchStrategy;

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
    private final List<NutsMessage> errors;
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
    private final NutsWorkspaceIsolation isolation;
    private final NutsSupportMode desktopLauncher;
    private final NutsSupportMode menuLauncher;
    private final NutsSupportMode userLauncher;

    public DefaultNutsWorkspaceOptions(NutsVersion apiVersion, NutsId runtimeId, String workspace, String name, String javaCommand,
                                       String javaOptions, String outLinePrefix, String errLinePrefix, String userName,
                                       char[] credentials, String progressOptions, String dependencySolver,
                                       String debug, String archetype, String locale, String theme, NutsLogConfig logConfig,
                                       NutsConfirmationMode confirm, NutsContentType outputFormat, NutsOpenMode openMode,
                                       NutsExecutionType executionType, NutsStoreLocationStrategy storeLocationStrategy,
                                       NutsStoreLocationStrategy repositoryStoreLocationStrategy, NutsOsFamily storeLocationLayout,
                                       NutsTerminalMode terminalMode, NutsFetchStrategy fetchStrategy, NutsRunAs runAs,
                                       Instant creationTime, Instant expireTime, Boolean skipCompanions, Boolean skipWelcome,
                                       Boolean skipBoot, Boolean global, Boolean gui, Boolean readOnly,
                                       Boolean trace, Boolean dry, Boolean recover, Boolean reset, Boolean commandVersion,
                                       Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached,
                                       Boolean indexed, Boolean transitive, Boolean bot, Boolean skipErrors,
                                       NutsWorkspaceIsolation isolation, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, InputStream stdin, PrintStream stdout, PrintStream stderr,
                                       ExecutorService executorService, Supplier<ClassLoader> classLoaderSupplier,
                                       List<String> applicationArguments, List<String> outputFormatOptions,
                                       List<String> customOptions, List<String> excludedExtensions, List<String> repositories,
                                       List<String> executorOptions, List<NutsMessage> errors, Map<NutsStoreLocation, String> storeLocations,
                                       Map<NutsHomeLocation, String> homeLocations, NutsSupportMode desktopLauncher, NutsSupportMode menuLauncher, NutsSupportMode userLauncher) {
        this.outputFormatOptions = PrivateNutsUtilCollections.unmodifiableOrNullList(outputFormatOptions);
        this.customOptions = PrivateNutsUtilCollections.unmodifiableOrNullList(customOptions);
        this.excludedExtensions = PrivateNutsUtilCollections.unmodifiableOrNullList(excludedExtensions);
        this.repositories = PrivateNutsUtilCollections.unmodifiableOrNullList(repositories);
        this.applicationArguments = PrivateNutsUtilCollections.unmodifiableOrNullList(applicationArguments);
        this.errors = PrivateNutsUtilCollections.unmodifiableOrNullList(errors);
        this.executorOptions = PrivateNutsUtilCollections.unmodifiableOrNullList(executorOptions);

        this.storeLocations = PrivateNutsUtilCollections.unmodifiableOrNullMap(storeLocations);
        this.homeLocations = PrivateNutsUtilCollections.unmodifiableOrNullMap(homeLocations);

        this.apiVersion = apiVersion;
        this.runtimeId = runtimeId;
        this.javaCommand = javaCommand;
        this.javaOptions = javaOptions;
        this.workspace = workspace;
        this.outLinePrefix = outLinePrefix;
        this.errLinePrefix = errLinePrefix;
        this.name = name;
        this.skipCompanions = skipCompanions;
        this.skipWelcome = skipWelcome;
        this.skipBoot = skipBoot;
        this.global = global;
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
        this.storeLocationLayout = storeLocationLayout;
        this.storeLocationStrategy = storeLocationStrategy;
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
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
        this.isolation = isolation;
        this.initLaunchers=initLaunchers;
        this.initScripts=initScripts;
        this.initPlatforms=initPlatforms;
        this.initJava=initJava;
        this.desktopLauncher=desktopLauncher;
        this.menuLauncher=menuLauncher;
        this.userLauncher=userLauncher;
    }

    @Override
    public NutsOptional<NutsSupportMode> getDesktopLauncher() {
        return NutsOptional.of(desktopLauncher);
    }

    @Override
    public NutsOptional<NutsSupportMode> getMenuLauncher() {
        return NutsOptional.of(menuLauncher);
    }

    @Override
    public NutsOptional<NutsSupportMode> getUserLauncher() {
        return NutsOptional.of(userLauncher);
    }

    @Override
    public NutsOptional<NutsWorkspaceIsolation> getIsolation() {
        return NutsOptional.of(isolation);
    }

    @Override
    public NutsOptional<Boolean> getInitLaunchers() {
        return NutsOptional.of(initLaunchers);
    }

    @Override
    public NutsOptional<Boolean> getInitScripts() {
        return NutsOptional.of(initScripts);
    }

    @Override
    public NutsOptional<Boolean> getInitPlatforms() {
        return NutsOptional.of(initPlatforms);
    }

    @Override
    public NutsOptional<Boolean> getInitJava() {
        return NutsOptional.of(initJava);
    }

    @Override
    public NutsOptional<NutsVersion> getApiVersion() {
        return NutsOptional.of(apiVersion,s->NutsMessage.cstyle("apiVersion is null"));
    }

    @Override
    public NutsOptional<List<String>> getApplicationArguments() {
        return NutsOptional.of(applicationArguments);
    }


    @Override
    public NutsOptional<String> getArchetype() {
        return NutsOptional.of(archetype);
    }


    @Override
    public NutsOptional<Supplier<ClassLoader>> getClassLoaderSupplier() {
        return NutsOptional.of(classLoaderSupplier);
    }


    @Override
    public NutsOptional<NutsConfirmationMode> getConfirm() {
        return NutsOptional.of(confirm);
    }


    @Override
    public NutsOptional<Boolean> getDry() {
        return NutsOptional.of(dry);
    }


    @Override
    public NutsOptional<Instant> getCreationTime() {
        return NutsOptional.of(creationTime);
    }


    @Override
    public NutsOptional<List<String>> getExcludedExtensions() {
        return NutsOptional.of(excludedExtensions);
    }


    @Override
    public NutsOptional<NutsExecutionType> getExecutionType() {
        return NutsOptional.of(executionType);
    }


    @Override
    public NutsOptional<NutsRunAs> getRunAs() {
        return NutsOptional.of(runAs);
    }


    @Override
    public NutsOptional<List<String>> getExecutorOptions() {
        return NutsOptional.of(executorOptions);
    }


    @Override
    public NutsOptional<String> getHomeLocation(NutsHomeLocation location) {
        return NutsOptional.of(homeLocations.get(location));
    }

    @Override
    public NutsOptional<Map<NutsHomeLocation, String>> getHomeLocations() {
        return NutsOptional.of(homeLocations);
    }


    @Override
    public NutsOptional<String> getJavaCommand() {
        return NutsOptional.of(javaCommand);
    }


    @Override
    public NutsOptional<String> getJavaOptions() {
        return NutsOptional.of(javaOptions);
    }


    @Override
    public NutsOptional<NutsLogConfig> getLogConfig() {
        return NutsOptional.of(logConfig);
    }


    @Override
    public NutsOptional<String> getName() {
        return NutsOptional.of(name);
    }


    @Override
    public NutsOptional<NutsOpenMode> getOpenMode() {
        return NutsOptional.of(openMode);
    }


    @Override
    public NutsOptional<NutsContentType> getOutputFormat() {
        return NutsOptional.of(outputFormat);
    }


    @Override
    public NutsOptional<List<String>> getOutputFormatOptions() {
        return NutsOptional.of(outputFormatOptions);
    }


    @Override
    public NutsOptional<char[]> getCredentials() {
        return NutsOptional.of(credentials == null ? null : Arrays.copyOf(credentials, credentials.length));
    }


    @Override
    public NutsOptional<NutsStoreLocationStrategy> getRepositoryStoreLocationStrategy() {
        return NutsOptional.of(repositoryStoreLocationStrategy);
    }

    @Override
    public NutsOptional<NutsId> getRuntimeId() {
        return NutsOptional.of(runtimeId);
    }


    @Override
    public NutsOptional<String> getStoreLocation(NutsStoreLocation folder) {
        return NutsOptional.of(storeLocations.get(folder));
    }

    @Override
    public NutsOptional<NutsOsFamily> getStoreLocationLayout() {
        return NutsOptional.of(storeLocationLayout);
    }


    @Override
    public NutsOptional<NutsStoreLocationStrategy> getStoreLocationStrategy() {
        return NutsOptional.of(storeLocationStrategy);
    }


    @Override
    public NutsOptional<Map<NutsStoreLocation, String>> getStoreLocations() {
        return NutsOptional.of(storeLocations);
    }


    @Override
    public NutsOptional<NutsTerminalMode> getTerminalMode() {
        return NutsOptional.of(terminalMode);
    }


    @Override
    public NutsOptional<List<String>> getRepositories() {
        return NutsOptional.of(repositories);
    }


    @Override
    public NutsOptional<String> getUserName() {
        return NutsOptional.of(userName);
    }

    @Override
    public NutsOptional<String> getWorkspace() {
        return NutsOptional.of(workspace);
    }

    @Override
    public NutsOptional<String> getDebug() {
        return NutsOptional.of(debug);
    }


    @Override
    public NutsOptional<Boolean> getGlobal() {
        return NutsOptional.of(global);
    }


    @Override
    public NutsOptional<Boolean> getGui() {
        return NutsOptional.of(gui);
    }


    @Override
    public NutsOptional<Boolean> getInherited() {
        return NutsOptional.of(inherited);
    }



    @Override
    public NutsOptional<Boolean> getReadOnly() {
        return NutsOptional.of(readOnly);
    }


    @Override
    public NutsOptional<Boolean> getRecover() {
        return NutsOptional.of(recover);
    }


    @Override
    public NutsOptional<Boolean> getReset() {
        return NutsOptional.of(reset);
    }


    @Override
    public NutsOptional<Boolean> getCommandVersion() {
        return NutsOptional.of(commandVersion);
    }

    @Override
    public NutsOptional<Boolean> getCommandHelp() {
        return NutsOptional.of(commandHelp);
    }

    @Override
    public NutsOptional<Boolean> getSkipCompanions() {
        return NutsOptional.of(skipCompanions);
    }


    @Override
    public NutsOptional<Boolean> getSkipWelcome() {
        return NutsOptional.of(skipWelcome);
    }

    @Override
    public NutsOptional<String> getOutLinePrefix() {
        return NutsOptional.of(outLinePrefix);
    }


    @Override
    public NutsOptional<String> getErrLinePrefix() {
        return NutsOptional.of(errLinePrefix);
    }

    @Override
    public NutsOptional<Boolean> getSkipBoot() {
        return NutsOptional.of(skipBoot);
    }


    @Override
    public NutsOptional<Boolean> getTrace() {
        return NutsOptional.of(trace);
    }

    public NutsOptional<String> getProgressOptions() {
        return NutsOptional.of(progressOptions);
    }

    @Override
    public NutsOptional<Boolean> getCached() {
        return NutsOptional.of(cached);
    }

    @Override
    public NutsOptional<Boolean> getIndexed() {
        return NutsOptional.of(indexed);
    }

    @Override
    public NutsOptional<Boolean> getTransitive() {
        return NutsOptional.of(transitive);
    }

    @Override
    public NutsOptional<Boolean> getBot() {
        return NutsOptional.of(bot);
    }

    @Override
    public NutsOptional<NutsFetchStrategy> getFetchStrategy() {
        return NutsOptional.of(fetchStrategy);
    }

    @Override
    public NutsOptional<InputStream> getStdin() {
        return NutsOptional.of(stdin);
    }

    @Override
    public NutsOptional<PrintStream> getStdout() {
        return NutsOptional.of(stdout);
    }

    @Override
    public NutsOptional<PrintStream> getStderr() {
        return NutsOptional.of(stderr);
    }

    @Override
    public NutsOptional<ExecutorService> getExecutorService() {
        return NutsOptional.of(executorService);
    }

    @Override
    public NutsOptional<Instant> getExpireTime() {
        return NutsOptional.of(expireTime);
    }

    @Override
    public NutsOptional<Boolean> getSkipErrors() {
        return NutsOptional.of(skipErrors);
    }

    @Override
    public NutsOptional<Boolean> getSwitchWorkspace() {
        return NutsOptional.of(switchWorkspace);
    }

    @Override
    public NutsOptional<List<NutsMessage>> getErrors() {
        return NutsOptional.of(errors);
    }

    @Override
    public NutsOptional<List<String>> getCustomOptions() {
        return NutsOptional.of(customOptions);
    }

    @Override
    public NutsOptional<String> getLocale() {
        return NutsOptional.of(locale);
    }

    @Override
    public NutsOptional<String> getTheme() {
        return NutsOptional.of(theme);
    }


    @Override
    public NutsOptional<String> getDependencySolver() {
        return NutsOptional.of(dependencySolver);
    }

    @Override
    public String toString() {
        return toCommandLine().toString();
    }

    @Override
    public NutsWorkspaceOptions readOnly() {
        return this;
    }

    @Override
    public NutsCommandLine toCommandLine() {
        return toCommandLine(new NutsWorkspaceOptionsConfig());
    }

    @Override
    public NutsCommandLine toCommandLine(NutsWorkspaceOptionsConfig config) {
        return new PrivateNutsWorkspaceOptionsArgumentsBuilder(config, this).toCommandLine();
    }

    @Override
    public NutsWorkspaceOptionsBuilder builder() {
        return new DefaultNutsWorkspaceOptionsBuilder().setAll(this);
    }

}
