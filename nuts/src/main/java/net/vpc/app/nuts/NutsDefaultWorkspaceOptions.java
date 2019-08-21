/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Workspace creation/opening options class.
 *
 * @since 0.5.4
 */
public final class NutsDefaultWorkspaceOptions implements Serializable, Cloneable, NutsWorkspaceOptions {
    private static final long serialVersionUID = 1;

    /**
     * creates a string key combining layout and location.
     * le key has the form of a concatenated layout and location ids separated by ':'
     * where null layout is replaced by 'system' keyword.
     * used in {@link NutsWorkspaceOptions#getHomeLocations()}.
     * @param storeLocationLayout layout
     * @param location location
     * @return combination of layout and location separated by ':'.
     */
    public static String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location) {
        return (storeLocationLayout == null ? "system" : storeLocationLayout.id()) + ":" + (location == null ? "system" : location.id());
    }

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
     * if true consider global/system repository
     *
     * option-type : exported (inherited in child workspaces)
     */
    private boolean global;

    /**
     * if true consider GUI/Swing mode
     *
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
     * parse arguments
     * @param args arguments
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions parse(String[] args) {
        PrivateNutsArgumentsParser.parseNutsArguments(args, this);
        return this;
    }

    public NutsDefaultWorkspaceOptions() {
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public NutsDefaultWorkspaceOptions(String[] args) {
        PrivateNutsArgumentsParser.parseNutsArguments(args, this);
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    /**
     * set workspace
     * @param workspace workspace
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * set workspace name
     * @param workspaceName new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    /**
     * set global
     * @param global new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    @Override
    public boolean isGui() {
        return gui;
    }

    /**
     * set gui
     * @param gui new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setGui(boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public String getArchetype() {
        return archetype;
    }

    /**
     * set archetype
     * @param archetype new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public String[] getExcludedExtensions() {
        return excludedExtensions;
    }

    /**
     * set excludedExtensions
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setExcludedExtensions(String[] excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public String[] getExcludedRepositories() {
        return excludedRepositories;
    }

    /**
     * set excludedRepositories
     * @param excludedRepositories new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setExcludedRepositories(String[] excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * set login
     * @param username new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setUsername(String username) {
        this.userName = username;
        return this;
    }

    @Override
    public char[] getCredentials() {
        return credentials;
    }

    /**
     * set password
     * @param credentials new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    /**
     * set executionType
     * @param executionType new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    /**
     * set inherited
     * @param inherited new value
     * @return {@code this} instance
     */
    NutsDefaultWorkspaceOptions setInherited(boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NutsDefaultWorkspaceOptions copy() {
        try {
            NutsDefaultWorkspaceOptions t = (NutsDefaultWorkspaceOptions) clone();
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
    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }

    /**
     * set terminalMode
     * @param terminalMode new value
     * @return {@code this} instance
     */
    public NutsWorkspaceOptions setTerminalMode(NutsTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public boolean isDry() {
        return dry;
    }

    /**
     * set dry
     * @param dry new value
     * @return {@code this} instance
     */
    public NutsWorkspaceOptions setDry(boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * set creationTime
     * @param creationTime new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * set readOnly
     * @param readOnly new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    /**
     * set trace
     * @param trace new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    /**
     * set runtimeId
     * @param runtimeId new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return classLoaderSupplier;
    }

    /**
     * set provider
     * @param provider new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsFormat format() {
        return new NutsWorkspaceOptionsFormat(this);
    }

    @Override
    public String[] getApplicationArguments() {
        return applicationArguments == null ? new String[0] : Arrays.copyOf(applicationArguments, applicationArguments.length);
    }

    /**
     * set applicationArguments
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsDefaultWorkspaceOptions setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public String getJavaOptions() {
        return javaOptions;
    }

    /**
     * set javaOptions
     * @param javaOptions new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions;
    }

    /**
     * set executorOptions
     * @param executorOptions new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public boolean isRecover() {
        return recover;
    }

    /**
     * set recover
     * @param recover new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setRecover(boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public boolean isReset() {
        return reset;
    }

    /**
     * set reset
     * @param reset new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setReset(boolean reset) {
        this.reset = reset;
        return this;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    /**
     * set debug
     * @param debug new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public String[] getTransientRepositories() {
        return transientRepositories == null ? new String[0] : transientRepositories;
    }

    /**
     * set transientRepositories
     * @param transientRepositories new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setTransientRepositories(String[] transientRepositories) {
        this.transientRepositories = transientRepositories;
        return this;
    }

    @Override
    public NutsLogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * set logConfig
     * @param logConfig new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * set apiVersion
     * @param apiVersion new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    /**
     * set storeLocationLayout
     * @param storeLocationLayout new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    /**
     * set storeLocationStrategy
     * @param storeLocationStrategy new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    /**
     * set repositoryStoreLocationStrategy
     * @param repositoryStoreLocationStrategy new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations.get(folder.id());
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location) {
        String key = createHomeLocationKey(layout, location);
        return homeLocations.get(key);
    }

    /**
     * set store location
     * @param location location
     * @param value new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setStoreLocation(NutsStoreLocation location, String value) {
        if (PrivateNutsUtils.isBlank(value)) {
            storeLocations.remove(location.id());
        } else {
            storeLocations.put(location.id(), value);
        }
        return this;
    }

    /**
     * set home location
     * @param layout layout
     * @param location location
     * @param value new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value) {
        String key = createHomeLocationKey(layout, location);
        if (PrivateNutsUtils.isBlank(value)) {
            homeLocations.remove(key);
        } else {
            homeLocations.put(key, value);
        }
        return this;
    }

    @Override
    public boolean isSkipCompanions() {
        return skipCompanions;
    }

    /**
     * set skipInstallCompanions
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setSkipCompanions(boolean skipInstallCompanions) {
        this.skipCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public boolean isSkipWelcome() {
        return skipWelcome;
    }

    /**
     * set skipWelcome
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setSkipWelcome(boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NutsWorkspaceOpenMode getOpenMode() {
        return openMode;
    }

    /**
     * set openMode
     * @param openMode new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setOpenMode(NutsWorkspaceOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NutsConfirmationMode getConfirm() {
        return confirm;
    }

    /**
     * set confirm
     * @param confirm new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * set outputFormat
     * @param outputFormat new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setOutputFormat(NutsOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    /**
     * add output format options
     * @param options new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions addOutputFormatOptions(String... options) {
        if (options != null) {
            outputFormatOptions.addAll(Arrays.asList(options));
        }
        return this;
    }

    /**
     * set output format options
     * @param options new value
     * @return {@code this} instance
     */
    public NutsDefaultWorkspaceOptions setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public String[] getOutputFormatOptions() {
        return outputFormatOptions.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return format().getBootCommandLine();
    }

}
