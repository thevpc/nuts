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
import java.util.List;

/**
 * Created by vpc on 1/23/17.
 *
 * @since 0.5.4
 */
public final class NutsWorkspaceOptions implements Serializable, Cloneable {

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String requiredBootVersion = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String bootRuntime;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String bootJavaCommand;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String bootJavaOptions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String workspace = null;
    
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String name = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean skipInstallCompanions;

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
    private char[] password = null;
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
    private boolean trace = false;

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
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsClassLoaderProvider classLoaderProvider;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String[] executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsBootCommand bootCommand = NutsBootCommand.EXEC;

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
    private String[] storeLocations = new String[NutsStoreLocation.values().length];

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String[] homeLocations = new String[NutsOsFamily.values().length * NutsStoreLocation.values().length];

    private String[] defaultHomeLocations = new String[NutsStoreLocation.values().length];

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

    public void parse(String[] args) {
        NutsArgumentsParser.parseNutsArguments(args, this);
    }

    public NutsWorkspaceOptions() {
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public NutsWorkspaceOptions(String[] args) {
        NutsArgumentsParser.parseNutsArguments(args, this);
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceOptions setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceOptions setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public NutsWorkspaceOptions setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public boolean isGui() {
        return gui;
    }

    public NutsWorkspaceOptions setGui(boolean gui) {
        this.gui = gui;
        return this;
    }

    public String getArchetype() {
        return archetype;
    }

    public NutsWorkspaceOptions setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    public String[] getExcludedExtensions() {
        return excludedExtensions;
    }

    public NutsWorkspaceOptions setExcludedExtensions(String[] excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    public String[] getExcludedRepositories() {
        return excludedRepositories;
    }

    public NutsWorkspaceOptions setExcludedRepositories(String[] excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public NutsWorkspaceOptions setLogin(String login) {
        this.userName = login;
        return this;
    }

    public char[] getPassword() {
        return password;
    }

    public NutsWorkspaceOptions setPassword(char[] password) {
        this.password = password;
        return this;
    }

    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    public NutsWorkspaceOptions setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    public NutsWorkspaceOptions copy() {
        try {
            NutsWorkspaceOptions t = (NutsWorkspaceOptions) clone();
            t.storeLocations = Arrays.copyOf(storeLocations, storeLocations.length);
            t.homeLocations = Arrays.copyOf(homeLocations, homeLocations.length);
            t.defaultHomeLocations = Arrays.copyOf(defaultHomeLocations, defaultHomeLocations.length);
            t.setExcludedExtensions(t.getExcludedExtensions() == null ? null : Arrays.copyOf(t.getExcludedExtensions(), t.getExcludedExtensions().length));
            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            t.setTransientRepositories(t.getTransientRepositories() == null ? null : Arrays.copyOf(t.getTransientRepositories(), t.getTransientRepositories().length));
            t.setApplicationArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(null, "Should never Happen", e);
        }
    }

    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }

    public void setTerminalMode(NutsTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public NutsWorkspaceOptions setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public NutsWorkspaceOptions setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsWorkspaceOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsWorkspaceOptions setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsWorkspaceOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    public NutsWorkspaceOptionsFormat format() {
        return new NutsWorkspaceOptionsFormat(this);
    }

    public String[] getApplicationArguments() {
        return applicationArguments == null ? new String[0] : Arrays.copyOf(applicationArguments, applicationArguments.length);
    }

    public NutsWorkspaceOptions setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public NutsWorkspaceOptions setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public NutsWorkspaceOptions setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions;
    }

    public NutsWorkspaceOptions setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    public NutsBootCommand getBootCommand() {
        return bootCommand;
    }

    public NutsWorkspaceOptions setBootCommand(NutsBootCommand bootCommand) {
        this.bootCommand = bootCommand;
        return this;
    }

    public String[] getTransientRepositories() {
        return transientRepositories == null ? new String[0] : transientRepositories;
    }

    public NutsWorkspaceOptions setTransientRepositories(String[] transientRepositories) {
        this.transientRepositories = transientRepositories;
        return this;
    }

    public NutsLogConfig getLogConfig() {
        return logConfig;
    }

    public NutsWorkspaceOptions setLogConfig(NutsLogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    public String getRequiredBootVersion() {
        return requiredBootVersion;
    }

    public NutsWorkspaceOptions setRequiredBootVersion(String requiredBootVersion) {
        this.requiredBootVersion = requiredBootVersion;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceOptions setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsWorkspaceOptions setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsWorkspaceOptions setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations[folder.ordinal()];
    }

    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folder) {
        if (layout == null) {
            return defaultHomeLocations[folder.ordinal()];
        }
        return homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()];
    }

    public NutsWorkspaceOptions setStoreLocation(NutsStoreLocation folder, String value) {
        storeLocations[folder.ordinal()] = value;
        return this;
    }

    public NutsWorkspaceOptions setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String value) {
        if (layout == null) {
            defaultHomeLocations[folder.ordinal()] = value;
        } else {
            homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()] = value;
        }
        return this;
    }

    public boolean isSkipInstallCompanions() {
        return skipInstallCompanions;
    }

    public NutsWorkspaceOptions setSkipInstallCompanions(boolean skipInstallCompanions) {
        this.skipInstallCompanions = skipInstallCompanions;
        return this;
    }

    public boolean isSkipWelcome() {
        return skipWelcome;
    }

    public NutsWorkspaceOptions setSkipWelcome(boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    public NutsWorkspaceOpenMode getOpenMode() {
        return openMode;
    }

    public NutsWorkspaceOptions setOpenMode(NutsWorkspaceOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    public NutsConfirmationMode getConfirm() {
        return confirm;
    }

    public NutsWorkspaceOptions setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    public NutsOutputFormat getOutputFormat() {
        return outputFormat;
    }

    public NutsWorkspaceOptions setOutputFormat(NutsOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public String[] getStoreLocations() {
        return Arrays.copyOf(storeLocations, storeLocations.length);
    }

    public String[] getHomeLocations() {
        return Arrays.copyOf(homeLocations, homeLocations.length);
    }

    public String[] getDefaultHomeLocations() {
        return Arrays.copyOf(defaultHomeLocations, defaultHomeLocations.length);
    }

    public NutsWorkspaceOptions addOutputFormatOptions(String... options) {
        if (options != null) {
            outputFormatOptions.addAll(Arrays.asList(options));
        }
        return this;
    }

    public NutsWorkspaceOptions setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    public String[] getOutputFormatOptions() {
        return outputFormatOptions.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return format().getBootCommandLine();
    }

}
