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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.reserved.NReservedWorkspaceCmdLineParser;
import net.thevpc.nuts.reserved.NReservedCollectionUtils;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.NLogConfig;
import net.thevpc.nuts.util.NStringUtils;

import java.io.InputStream;
import java.io.PrintStream;
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
     * if true no real execution, wil dry exec option-type : runtime (available
     * only for the current workspace instance)
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
    public NOptional<NSupportMode> getDesktopLauncher() {
        return NOptional.ofNamed(desktopLauncher,"desktopLauncher");
    }

    @Override
    public NOptional<NSupportMode> getMenuLauncher() {
        return NOptional.ofNamed(menuLauncher,"menuLauncher");
    }

    @Override
    public NOptional<NSupportMode> getUserLauncher() {
        return NOptional.ofNamed(userLauncher,"userLauncher");
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
        return new DefaultNWorkspaceOptionsBuilder().setAll(this);
    }


    @Override
    public NOptional<NVersion> getApiVersion() {
        return NOptional.ofNamed(apiVersion,"apiVersion");
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
        return NOptional.ofNamed(applicationArguments,"applicationArguments");
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
        return NOptional.ofNamed(archetype,"archetype");
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
        return NOptional.ofNamed(classLoaderSupplier,"classLoaderSupplier");
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
        return NOptional.ofNamed(confirm,"confirm");
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
        return NOptional.ofNamed(dry,"dry");
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

    @Override
    public NOptional<Instant> getCreationTime() {
        return NOptional.ofNamed(creationTime,"creationTime");
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
        return NOptional.ofNamed(excludedExtensions,"excludedExtensions");
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
        return NOptional.ofNamed(executionType,"executionType");
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
        return NOptional.ofNamed(runAs,"runAs");
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
        return NOptional.ofNamed(executorOptions,"executorOptions");
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
        return NOptional.ofNamed(homeLocations==null?null:homeLocations.get(location),"homeLocations["+location+"]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> getHomeLocations() {
        return NOptional.ofNamed(homeLocations,"homeLocations");
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
        return NOptional.ofNamed(javaCommand,"javaCommand");
    }

    @Override
    public NWorkspaceOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NOptional<String> getJavaOptions() {
        return NOptional.ofNamed(javaOptions,"javaOptions");
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
        return NOptional.ofNamed(logConfig,"logConfig");
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
        return NOptional.ofNamed(name,"name");
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
        return NOptional.ofNamed(openMode,"openMode");
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
        return NOptional.ofNamed(outputFormat,"outputFormat");
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
        return NOptional.ofNamed(outputFormatOptions,"outputFormatOptions");
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
            return addOutputFormatOptions(NReservedCollectionUtils.nonNullList(options).toArray(new String[0]));
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
        return NOptional.ofNamed(credentials,"credentials");
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
        return NOptional.ofNamed(repositoryStoreStrategy,"repositoryStoreStrategy");
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
        return NOptional.ofNamed(runtimeId,"runtimeId");
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
        return NOptional.ofNamed(storeLocations==null?null:storeLocations.get(folder),"storeLocations["+folder+"]");
    }

    @Override
    public NOptional<NOsFamily> getStoreLayout() {
        return NOptional.ofNamed(storeLayout,"storeLayout");
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
        return NOptional.ofNamed(storeStrategy,"storeStrategy");
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
        return NOptional.ofNamed(storeLocations,"storeLocations");
    }

    @Override
    public NWorkspaceOptionsBuilder setStoreLocations(Map<NStoreType, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(NReservedCollectionUtils.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return this;
    }

    @Override
    public NOptional<NTerminalMode> getTerminalMode() {
        return NOptional.ofNamed(terminalMode,"terminalMode");
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
        return NOptional.ofNamed(repositories,"repositories");
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

    @Override
    public NOptional<String> getUserName() {
        return NOptional.ofNamed(userName,"userName");
    }

    @Override
    public NOptional<String> getWorkspace() {
        return NOptional.ofNamed(workspace,"workspace");
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
        return NOptional.ofNamed(debug,"debug");
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
        return NOptional.ofNamed(system,"system");
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
        return NOptional.ofNamed(gui,"gui");
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
        return NOptional.ofNamed(inherited,"inherited");
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
        return NOptional.ofNamed(readOnly,"readOnly");
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
        return NOptional.ofNamed(recover,"recover");
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
        return NOptional.ofNamed(reset,"reset");
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
    public NOptional<Boolean> getCommandVersion() {
        return NOptional.ofNamed(commandVersion,"commandVersion");
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NOptional<Boolean> getCommandHelp() {
        return NOptional.ofNamed(commandHelp,"commandHelp");
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NOptional<Boolean> getInstallCompanions() {
        return NOptional.ofNamed(installCompanions,"installCompanions");
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
        return NOptional.ofNamed(skipWelcome,"skipWelcome");
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
        return NOptional.ofNamed(outLinePrefix,"outLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<String> getErrLinePrefix() {
        return NOptional.ofNamed(errLinePrefix,"errLinePrefix");
    }

    @Override
    public NWorkspaceOptionsBuilder setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipBoot() {
        return NOptional.ofNamed(skipBoot,"skipBoot");
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
        return NOptional.ofNamed(trace,"trace");
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
        return NOptional.ofNamed(progressOptions,"progressOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> getCached() {
        return NOptional.ofNamed(cached,"cached");
    }

    @Override
    public NWorkspaceOptionsBuilder setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NOptional<Boolean> getIndexed() {
        return NOptional.ofNamed(indexed,"indexed");
    }

    @Override
    public NWorkspaceOptionsBuilder setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NOptional<Boolean> getTransitive() {
        return NOptional.ofNamed(transitive,"transitive");
    }

    @Override
    public NWorkspaceOptionsBuilder setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NOptional<Boolean> getBot() {
        return NOptional.ofNamed(bot,"bot");
    }

    @Override
    public NWorkspaceOptionsBuilder setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NOptional<NFetchStrategy> getFetchStrategy() {
        return NOptional.ofNamed(fetchStrategy,"fetchStrategy");
    }

    @Override
    public NWorkspaceOptionsBuilder setFetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NOptional<InputStream> getStdin() {
        return NOptional.ofNamed(stdin,"stdin");
    }

    @Override
    public NWorkspaceOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStdout() {
        return NOptional.ofNamed(stdout,"stdout");
    }

    @Override
    public NWorkspaceOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStderr() {
        return NOptional.ofNamed(stderr,"stderr");
    }

    @Override
    public NWorkspaceOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NOptional<ExecutorService> getExecutorService() {
        return NOptional.ofNamed(executorService,"executorService");
    }

    @Override
    public NWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NOptional<Instant> getExpireTime() {
        return NOptional.ofNamed(expireTime,"expireTime");
    }

    @Override
    public NWorkspaceOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipErrors() {
        return NOptional.ofNamed(skipErrors,"skipErrors");
    }

    @Override
    public NWorkspaceOptionsBuilder setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NOptional<Boolean> getSwitchWorkspace() {
        return NOptional.ofNamed(switchWorkspace,"switchWorkspace");
    }

    public NWorkspaceOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NOptional<List<NMsg>> getErrors() {
        return NOptional.ofNamed(errors,"errors");
    }

    @Override
    public NWorkspaceOptionsBuilder setErrors(List<NMsg> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NOptional<List<String>> getCustomOptions() {
        return NOptional.ofNamed(customOptions,"customOptions");
    }

    @Override
    public NWorkspaceOptionsBuilder setCustomOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NOptional<String> getLocale() {
        return NOptional.ofNamed(locale,"locale");
    }

    @Override
    public NWorkspaceOptionsBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NOptional<String> getTheme() {
        return NOptional.ofNamed(theme,"theme");
    }

    @Override
    public NWorkspaceOptionsBuilder setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setAll(NWorkspaceOptions other) {
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
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
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
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setAllPresent(NWorkspaceOptions other) {
        if (other != null) {
            if (other.getApiVersion().isPresent()) {
                this.setApiVersion(other.getApiVersion().orNull());
            }
            if (other.getRuntimeId().isPresent()) {
                this.setRuntimeId(other.getRuntimeId().orNull());
            }
            if (other.getJavaCommand().isPresent()) {
                this.setJavaCommand(other.getJavaCommand().orNull());
            }
            if (other.getJavaOptions().isPresent()) {
                this.setJavaOptions(other.getJavaOptions().orNull());
            }
            if (other.getWorkspace().isPresent()) {
                this.setWorkspace(other.getWorkspace().orNull());
            }
            if (other.getName().isPresent()) {
                this.setName(other.getName().orNull());
            }
            if (other.getInstallCompanions().isPresent()) {
                this.setInstallCompanions(other.getInstallCompanions().orNull());
            }
            if (other.getSkipWelcome().isPresent()) {
                this.setSkipWelcome(other.getSkipWelcome().orNull());
            }
            if (other.getSkipBoot().isPresent()) {
                this.setSkipBoot(other.getSkipBoot().orNull());
            }
            if (other.getSystem().isPresent()) {
                this.setSystem(other.getSystem().orNull());
            }
            if (other.getGui().isPresent()) {
                this.setGui(other.getGui().orNull());
            }
            if (other.getUserName().isPresent()) {
                this.setUserName(other.getUserName().orNull());
            }
            if (other.getCredentials().isPresent()) {
                this.setCredentials(other.getCredentials().orNull());
            }
            if (other.getTerminalMode().isPresent()) {
                this.setTerminalMode(other.getTerminalMode().orNull());
            }
            if (other.getReadOnly().isPresent()) {
                this.setReadOnly(other.getReadOnly().orNull());
            }
            if (other.getTrace().isPresent()) {
                this.setTrace(other.getTrace().orNull());
            }
            if (other.getProgressOptions().isPresent()) {
                this.setProgressOptions(other.getProgressOptions().orNull());
            }
            if (other.getLogConfig().isPresent()) {
                this.setLogConfig(other.getLogConfig().orNull());
            }
            if (other.getConfirm().isPresent()) {
                this.setConfirm(other.getConfirm().orNull());
            }
            if (other.getConfirm().isPresent()) {
                this.setConfirm(other.getConfirm().orNull());
            }
            if (other.getOutputFormat().isPresent()) {
                this.setOutputFormat(other.getOutputFormat().orNull());
            }
            if (other.getOutputFormatOptions().isPresent()) {
                this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
            }
            if (other.getOpenMode().isPresent()) {
                this.setOpenMode(other.getOpenMode().orNull());
            }
            if (other.getCreationTime().isPresent()) {
                this.setCreationTime(other.getCreationTime().orNull());
            }
            if (other.getDry().isPresent()) {
                this.setDry(other.getDry().orNull());
            }
            if (other.getClassLoaderSupplier().isPresent()) {
                this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
            }
            if (other.getExecutorOptions().isPresent()) {
                this.setExecutorOptions(other.getExecutorOptions().orNull());
            }
            if (other.getRecover().isPresent()) {
                this.setRecover(other.getRecover().orNull());
            }
            if (other.getReset().isPresent()) {
                this.setReset(other.getReset().orNull());
            }
            if (other.getCommandVersion().isPresent()) {
                this.setCommandVersion(other.getCommandVersion().orNull());
            }
            if (other.getCommandHelp().isPresent()) {
                this.setCommandHelp(other.getCommandHelp().orNull());
            }
            if (other.getDebug().isPresent()) {
                this.setDebug(other.getDebug().orNull());
            }
            if (other.getInherited().isPresent()) {
                this.setInherited(other.getInherited().orNull());
            }
            if (other.getExecutionType().isPresent()) {
                this.setExecutionType(other.getExecutionType().orNull());
            }
            if (other.getRunAs().isPresent()) {
                this.setRunAs(other.getRunAs().orNull());
            }
            if (other.getArchetype().isPresent()) {
                this.setArchetype(other.getArchetype().orNull());
            }
            if (other.getStoreStrategy().isPresent()) {
                this.setStoreStrategy(other.getStoreStrategy().orNull());
            }
            if (other.getHomeLocations().isPresent()) {
                this.setHomeLocations(other.getHomeLocations().orNull());
            }

            if (other.getStoreLocations().isPresent()) {
                this.setStoreLocations(other.getStoreLocations().orNull());
            }
            if (other.getStoreLayout().isPresent()) {
                this.setStoreLayout(other.getStoreLayout().orNull());
            }
            if (other.getStoreStrategy().isPresent()) {
                this.setStoreStrategy(other.getStoreStrategy().orNull());
            }
            if (other.getRepositoryStoreStrategy().isPresent()) {
                this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
            }
            if (other.getFetchStrategy().isPresent()) {
                this.setFetchStrategy(other.getFetchStrategy().orNull());
            }
            if (other.getCached().isPresent()) {
                this.setCached(other.getCached().orNull());
            }
            if (other.getIndexed().isPresent()) {
                this.setIndexed(other.getIndexed().orNull());
            }
            if (other.getTransitive().isPresent()) {
                this.setTransitive(other.getTransitive().orNull());
            }
            if (other.getBot().isPresent()) {
                this.setBot(other.getBot().orNull());
            }
            if (other.getStdin().isPresent()) {
                this.setStdin(other.getStdin().orNull());
            }
            if (other.getStdout().isPresent()) {
                this.setStdout(other.getStdout().orNull());
            }
            if (other.getStderr().isPresent()) {
                this.setStderr(other.getStderr().orNull());
            }
            if (other.getExecutorService().isPresent()) {
                this.setExecutorService(other.getExecutorService().orNull());
            }
            if (other.getExcludedExtensions().isPresent()) {
                this.setExcludedExtensions(other.getExcludedExtensions().orNull());
            }
            if (other.getRepositories().isPresent()) {
                this.setRepositories(other.getRepositories().orNull());
            }
            if (other.getApplicationArguments().isPresent()) {
                this.setApplicationArguments(other.getApplicationArguments().orNull());
            }
            if (other.getCustomOptions().isPresent()) {
                this.setCustomOptions(other.getCustomOptions().orNull());
            }
            if (other.getExpireTime().isPresent()) {
                this.setExpireTime(other.getExpireTime().orNull());
            }
            if (other.getErrors().isPresent()) {
                this.setErrors(other.getErrors().orNull());
            }
            if (other.getSkipErrors().isPresent()) {
                this.setSkipErrors(other.getSkipErrors().orNull());
            }
            if (other.getSwitchWorkspace().isPresent()) {
                this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
            }
            if (other.getLocale().isPresent()) {
                this.setLocale(other.getLocale().orNull());
            }
            if (other.getTheme().isPresent()) {
                this.setTheme(other.getTheme().orNull());
            }
            if (other.getDependencySolver().isPresent()) {
                this.setDependencySolver(other.getDependencySolver().orNull());
            }
            if (other.getIsolationLevel().isPresent()) {
                this.setIsolationLevel(other.getIsolationLevel().orNull());
            }
            if (other.getInitLaunchers().isPresent()) {
                this.setInitLaunchers(other.getInitLaunchers().orNull());
            }
            if (other.getInitJava().isPresent()) {
                this.setInitJava(other.getInitJava().orNull());
            }
            if (other.getInitScripts().isPresent()) {
                this.setInitScripts(other.getInitScripts().orNull());
            }
            if (other.getInitLaunchers().isPresent()) {
                this.setInitLaunchers(other.getInitLaunchers().orNull());
            }
            if (other.getDesktopLauncher().isPresent()) {
                this.setDesktopLauncher(other.getDesktopLauncher().orNull());
            }
            if (other.getMenuLauncher().isPresent()) {
                this.setMenuLauncher(other.getMenuLauncher().orNull());
            }
            if (other.getUserLauncher().isPresent()) {
                this.setUserLauncher(other.getUserLauncher().orNull());
            }
        }
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandLine(String cmdLine, NSession session) {
        setCommandLine(NCmdLine.parseDefault(cmdLine).get(session).toStringArray(), session);
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setCommandLine(String[] args, NSession session) {
        NReservedWorkspaceCmdLineParser.parseNutsArguments(args, this, session);
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
        return NOptional.ofNamed(dependencySolver,"dependencySolver");
    }

    @Override
    public NWorkspaceOptionsBuilder setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    @Override
    public String toString() {
        return toCommandLine().toString();
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
                getDry().orNull(), getRecover().orNull(), getReset().orNull(), getCommandVersion().orNull(),
                getCommandHelp().orNull(), getCommandHelp().orNull(), getSwitchWorkspace().orNull(), getCached().orNull(),
                getIndexed().orNull(), getTransitive().orNull(), getBot().orNull(), getSkipErrors().orNull(),
                getIsolationLevel().orNull(), getInitLaunchers().orNull(), getInitScripts().orNull(), getInitPlatforms().orNull(),
                getInitJava().orNull(), getStdin().orNull(), getStdout().orNull(), getStdout().orNull(), getExecutorService().orNull(),
                getClassLoaderSupplier().orNull(), getApplicationArguments().orNull(), getOutputFormatOptions().orNull(),
                getCustomOptions().orNull(), getExcludedExtensions().orNull(), getRepositories().orNull(),
                getExecutorOptions().orNull(), getErrors().orNull(), getStoreLocations().orNull(), getHomeLocations().orNull(),
                getDesktopLauncher().orNull(), getMenuLauncher().orNull(), getUserLauncher().orNull());
    }

    @Override
    public NWorkspaceOptionsBuilder builder() {
        return new DefaultNWorkspaceOptionsBuilder().setAll(this);
    }

    @Override
    public NWorkspaceOptions readOnly() {
        return build();
    }

    @Override
    public NCmdLine toCommandLine() {
        return build().toCommandLine();
    }

    @Override
    public NCmdLine toCommandLine(NWorkspaceOptionsConfig config) {
        return build().toCommandLine(config);
    }

    @Override
    public NOptional<NIsolationLevel> getIsolationLevel() {
        return NOptional.ofNamed(isolationLevel,"isolationLevel");
    }

    @Override
    public NOptional<Boolean> getInitLaunchers() {
        return NOptional.ofNamed(initLaunchers,"initLaunchers");
    }

    @Override
    public NOptional<Boolean> getInitScripts() {
        return NOptional.ofNamed(initScripts,"initScripts");
    }

    @Override
    public NOptional<Boolean> getInitPlatforms() {
        return NOptional.ofNamed(initPlatforms,"initPlatforms");
    }

    @Override
    public NOptional<Boolean> getInitJava() {
        return NOptional.ofNamed(initJava,"initJava");
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
}
