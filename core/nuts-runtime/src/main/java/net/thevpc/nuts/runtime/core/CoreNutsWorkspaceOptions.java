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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace creation/opening options class.
 *
 * @category Config
 * @since 0.5.4
 */
public final class CoreNutsWorkspaceOptions implements Serializable, Cloneable, NutsWorkspaceOptionsBuilder {
    private static final long serialVersionUID = 1;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private String apiVersion = null;

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
    private String workspace = null;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private String name = null;

    /**
     * if true, do not install nuts companion tools upon workspace creation
     * option-type : exported (inherited in child workspaces)
     */
    private boolean skipCompanions;

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * defaults to false option-type : exported (inherited in child workspaces)
     *
     * @since 0.5.5
     */
    private boolean skipWelcome;

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     *
     * @since 0.6.0
     */
    private boolean skipBoot;

    /**
     * if true consider global/system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private boolean global;

    /**
     * if true consider GUI/Swing mode
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private boolean gui;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String[] excludedExtensions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String[] excludedRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String[] transientRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String userName = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private char[] credentials = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsTerminalMode terminalMode = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean readOnly = false;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean trace = true;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String progressOptions = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsConfirmationMode confirm = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsOutputFormat outputFormat = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> outputFormatOptions = new ArrayList<>();

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String[] applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsWorkspaceOpenMode openMode = NutsWorkspaceOpenMode.OPEN_OR_CREATE;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private long creationTime;

    /**
     * if true no real execution, wil dry exec
     * option-type : runtime (available only for the current workspace instance)
     */
    private boolean dry = false;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Supplier<ClassLoader> classLoaderSupplier;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String[] executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private boolean recover = false;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private boolean reset = false;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private boolean debug = false;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private boolean inherited = false;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsExecutionType executionType;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String archetype;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     * @since 0.8.0
     */
    private Boolean switchWorkspace;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<String, String> storeLocations = new HashMap<>();

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<String, String> homeLocations = new HashMap<>();

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsOsFamily storeLocationLayout = null;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy storeLocationStrategy = null;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NutsFetchStrategy fetchStrategy = NutsFetchStrategy.ONLINE;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean cached = true;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean indexed = true;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean transitive = true;

    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private InputStream stdin = null;

    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private PrintStream stdout = null;

    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private PrintStream stderr = null;

    /**
     * not parsed
     * option-type : runtime (available only for the current workspace instance)
     */
    private ExecutorService executorService = null;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String bootRepositories = null;
    private Instant expireTime = null;
    private String[] errors = new String[0];
    private boolean skipErrors = false;
    private NutsWorkspace ws = null;

    public CoreNutsWorkspaceOptions(NutsWorkspace ws) {
        this.ws = ws;
    }

    /**
     * creates a string key combining layout and location.
     * le key has the form of a concatenated layout and location ids separated by ':'
     * where null layout is replaced by 'system' keyword.
     * used in {@link NutsWorkspaceOptions#getHomeLocations()}.
     *
     * @param storeLocationLayout layout
     * @param location            location
     * @return combination of layout and location separated by ':'.
     */
    public static String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location) {
        return (storeLocationLayout == null ? "system" : storeLocationLayout.id()) + ":" + (location == null ? "system" : location.id());
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other) {
        this.setApiVersion(other.getApiVersion());
        this.setRuntimeId(other.getRuntimeId());
        this.setJavaCommand(other.getJavaCommand());
        this.setJavaOptions(other.getJavaOptions());
        this.setWorkspace(other.getWorkspace());
        this.setName(other.getName());
        this.setSkipCompanions(other.isSkipCompanions());
        this.setSkipWelcome(other.isSkipWelcome());
        this.setSkipBoot(other.isSkipBoot());
        this.setGlobal(other.isGlobal());
        this.setGui(other.isGui());
        this.setUsername(other.getUserName());
        this.setCredentials(other.getCredentials());
        this.setTerminalMode(other.getTerminalMode());
        this.setReadOnly(other.isReadOnly());
        this.setTrace(other.isTrace());
        this.setProgressOptions(other.getProgressOptions());
        this.setLogConfig(other.getLogConfig() == null ? null : new NutsLogConfig(other.getLogConfig())); //TODO
        this.setConfirm(other.getConfirm());
        this.setConfirm(other.getConfirm());
        this.setOutputFormat(other.getOutputFormat());
        this.setOutputFormatOptions(other.getOutputFormatOptions());
        this.setOpenMode(other.getOpenMode());
        this.setCreationTime(other.getCreationTime());
        this.setDry(other.isDry());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier());
        this.setExecutorOptions(other.getExecutorOptions());
        this.setRecover(other.isRecover());
        this.setReset(other.isReset());
        this.setDebug(other.isDebug());
        this.setInherited(other.isInherited());
        this.setExecutionType(other.getExecutionType());
        this.setArchetype(other.getArchetype());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy());
        this.setHomeLocations(other.getHomeLocations());
        this.setStoreLocations(other.getStoreLocations());
        this.setStoreLocationLayout(other.getStoreLocationLayout());
        this.setStoreLocationStrategy(other.getStoreLocationStrategy());
        this.setRepositoryStoreLocationStrategy(other.getRepositoryStoreLocationStrategy());
        this.setFetchStrategy(other.getFetchStrategy());
        this.setCached(other.isCached());
        this.setIndexed(other.isIndexed());
        this.setTransitive(other.isTransitive());
        this.setStdin(other.getStdin());
        this.setStdout(other.getStdout());
        this.setStderr(other.getStderr());
        this.setExecutorService(other.getExecutorService());
        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(other.getExcludedExtensions() == null ? null : Arrays.copyOf(other.getExcludedExtensions(), other.getExcludedExtensions().length));
        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setTransientRepositories(other.getTransientRepositories() == null ? null : Arrays.copyOf(other.getTransientRepositories(), other.getTransientRepositories().length));
        this.setApplicationArguments(other.getApplicationArguments() == null ? null : Arrays.copyOf(other.getApplicationArguments(), other.getApplicationArguments().length));
        this.setExpireTime(other.getExpireTime());
        this.setErrors(other.getErrors());
        this.setSkipErrors(other.isSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder parseCommandLine(String commandLine) {
        return parseArguments(ws.commandLine().parse(commandLine).toStringArray());
    }

    @Override
    public NutsWorkspaceOptionsBuilder parseArguments(String[] args) {
        CoreNutsArgumentsParser.parseNutsArguments(ws, args, this);
        return this;
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    /**
     * set global
     *
     * @param global new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setGui(boolean gui) {
        this.gui = gui;
        return this;
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExcludedExtensions(String[] excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    /**
     * set excludedRepositories
     *
     * @param excludedRepositories new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExcludedRepositories(String[] excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setUsername(String username) {
        this.userName = username;
        return this;
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setInherited(boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setSkipErrors(boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setErrors(String[] errors) {
        this.errors = errors == null ? new String[0] : Arrays.copyOf(errors, errors.length);
        return this;
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setDry(boolean dry) {
        this.dry = dry;
        return this;
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRecover(boolean recover) {
        this.recover = recover;
        return this;
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setReset(boolean reset) {
        this.reset = reset;
        return this;
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * set transientRepositories
     *
     * @param transientRepositories new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setTransientRepositories(String[] transientRepositories) {
        this.transientRepositories = transientRepositories;
        return this;
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * set storeLocationLayout
     *
     * @param storeLocationLayout new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    /**
     * set storeLocationStrategy
     *
     * @param storeLocationStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    /**
     * set repositoryStoreLocationStrategy
     *
     * @param repositoryStoreLocationStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
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
    public NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value) {
        if (CoreStringUtils.isBlank(value)) {
            storeLocations.remove(location.id());
        } else {
            storeLocations.put(location.id(), value);
        }
        return this;
    }

    /**
     * set home location
     *
     * @param layout   layout
     * @param location location
     * @param value    new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value) {
        String key = createHomeLocationKey(layout, location);
        if (CoreStringUtils.isBlank(value)) {
            homeLocations.remove(key);
        } else {
            homeLocations.put(key, value);
        }
        return this;
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipCompanions(boolean skipInstallCompanions) {
        this.skipCompanions = skipInstallCompanions;
        return this;
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipBoot(boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setSkipWelcome(boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOpenMode(NutsWorkspaceOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOutputFormat(NutsOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    /**
     * add output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = CoreStringUtils.trim(option);
                    if (!option.isEmpty()) {
                        outputFormatOptions.add(option);
                    }
                }
            }
        }
        return this;
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NutsWorkspaceOptionsBuilder setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy == null ? NutsFetchStrategy.ONLINE : fetchStrategy;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setCached(boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setIndexed(boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations.clear();
        if (storeLocations != null) {
            this.storeLocations.putAll(storeLocations);
        }
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder copy() {
        try {
            CoreNutsWorkspaceOptions t = (CoreNutsWorkspaceOptions) clone();
            t.logConfig = logConfig == null ? null : t.logConfig;
            t.storeLocations = new LinkedHashMap<>(storeLocations);
            t.homeLocations = new LinkedHashMap<>(homeLocations);
            t.setExcludedExtensions(t.getExcludedExtensions() == null ? null : Arrays.copyOf(t.getExcludedExtensions(), t.getExcludedExtensions().length));
            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            t.setTransientRepositories(t.getTransientRepositories() == null ? null : Arrays.copyOf(t.getTransientRepositories(), t.getTransientRepositories().length));
            t.setApplicationArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(null, "Should never Happen", e);
        }
    }

    @Override
    public NutsWorkspaceOptionsFormat format() {
        return new CoreNutsWorkspaceOptionsFormat(ws, this);
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public String[] getApplicationArguments() {
        return applicationArguments == null ? new String[0] : Arrays.copyOf(applicationArguments, applicationArguments.length);
    }

    @Override
    public String getArchetype() {
        return archetype;
    }

    @Override
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return classLoaderSupplier;
    }

    @Override
    public NutsConfirmationMode getConfirm() {
        return confirm;
    }

    @Override
    public boolean isDry() {
        return dry;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String[] getExcludedExtensions() {
        return excludedExtensions;
    }

    @Override
    public String[] getExcludedRepositories() {
        return excludedRepositories;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions;
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location) {
        String key = createHomeLocationKey(layout, location);
        return homeLocations.get(key);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    @Override
    public String getJavaCommand() {
        return javaCommand;
    }

    @Override
    public String getJavaOptions() {
        return javaOptions;
    }

    @Override
    public NutsLogConfig getLogConfig() {
        return logConfig;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsWorkspaceOpenMode getOpenMode() {
        return openMode;
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public String[] getOutputFormatOptions() {
        return outputFormatOptions.toArray(new String[0]);
    }

    @Override
    public char[] getCredentials() {
        return credentials;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations.get(folder.id());
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }

    @Override
    public String[] getTransientRepositories() {
        return transientRepositories == null ? new String[0] : transientRepositories;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public boolean isGui() {
        return gui;
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isRecover() {
        return recover;
    }

    @Override
    public boolean isReset() {
        return reset;
    }

    @Override
    public boolean isSkipCompanions() {
        return skipCompanions;
    }

    @Override
    public boolean isSkipWelcome() {
        return skipWelcome;
    }

    @Override
    public boolean isSkipBoot() {
        return skipBoot;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    public String getProgressOptions() {
        return progressOptions;
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public NutsFetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public InputStream getStdin() {
        return stdin;
    }

    public PrintStream getStdout() {
        return stdout;
    }

    public PrintStream getStderr() {
        return stderr;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public String getBootRepositories() {
        return bootRepositories;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public boolean isSkipErrors() {
        return skipErrors;
    }

    @Override
    public String[] getErrors() {
        return Arrays.copyOf(errors, errors.length);
    }

    @Override
    public String toString() {
        return format().getBootCommandLine();
    }


    @Override
    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    public NutsWorkspaceOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }
}
