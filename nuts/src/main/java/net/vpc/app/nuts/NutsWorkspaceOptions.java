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
import java.util.logging.Level;

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
    private String bootRuntimeSourceURL;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean perf = false;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String workspace = null;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private boolean skipInstallCompanions;

    /**
     * if true consider global/system repository
     *
     * option-type : exported (inherited in child workspaces)
     */
    private boolean global;

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
    private String login = null;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String password = null;
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
    private NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean defaultResponse = null;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private String[] applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsWorkspaceOpenMode openMode = NutsWorkspaceOpenMode.OPEN_OR_CREATE;
    /**
     * if true, all means are deployed to recover from corrupted workspace. This
     * flag may alter current used version of nuts to update to latest.
     *
     * option-type : runtime (available only for the current workspace instance)
     */
    private NutsBootInitMode initMode;

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
    private NutsExecutionType executionType = NutsExecutionType.SPAWN;

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
    private String[] homeLocations = new String[NutsStoreLocationLayout.values().length * NutsStoreLocation.values().length];

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NutsStoreLocationLayout storeLocationLayout = null;

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

    public boolean isGlobal() {
        return global;
    }

    public NutsWorkspaceOptions setGlobal(boolean global) {
        this.global = global;
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

    public String getLogin() {
        return login;
    }

    public NutsWorkspaceOptions setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NutsWorkspaceOptions setPassword(String password) {
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
            t.setExcludedExtensions(t.getExcludedExtensions() == null ? null : Arrays.copyOf(t.getExcludedExtensions(), t.getExcludedExtensions().length));
            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            t.setTransientRepositories(t.getTransientRepositories() == null ? null : Arrays.copyOf(t.getTransientRepositories(), t.getTransientRepositories().length));
            t.setApplicationArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen", e);
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

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsWorkspaceOptions setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public String getBootRuntimeSourceURL() {
        return bootRuntimeSourceURL;
    }

    public NutsWorkspaceOptions setBootRuntimeSourceURL(String bootRuntimeSourceURL) {
        this.bootRuntimeSourceURL = bootRuntimeSourceURL;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsWorkspaceOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    public String getExportedBootArgumentsString() {
        return getBootArgumentsString(true, false, false);
    }

    public String[] getExportedBootArguments() {
        return getBootArguments(true, false, false);
    }

    public String getBootArgumentsString(boolean exportedOptions, boolean runtimeOptions, boolean createOptions) {
        return NutsMinimalCommandLine.escapeArguments(getBootArguments(exportedOptions, runtimeOptions, createOptions));
    }

    public String[] getBootArguments(boolean exportedOptions, boolean runtimeOptions, boolean createOptions) {
        List<String> all = new ArrayList<>();
        if (exportedOptions) {
            for (int i = 0; i < NutsStoreLocationLayout.values().length; i++) {
                for (int j = 0; j < NutsStoreLocation.values().length; j++) {
                    String s = homeLocations[i * NutsStoreLocation.values().length + j];
                    if (!NutsUtils.isBlank(s)) {
                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.values()[i];
                        NutsStoreLocation folder = NutsStoreLocation.values()[j];
                        //config is exported!
                        if ((folder == NutsStoreLocation.CONFIG)) {
                            all.add("--" + layout.name().toLowerCase() + "-" + folder.name().toLowerCase() + "-home");
                            all.add(s);
                        }
                    }
                }
            }
            if (!NutsUtils.isBlank(bootRuntime)) {
                all.add("--boot-runtime");
                all.add(bootRuntime);
            }
            if (!NutsUtils.isBlank(bootJavaCommand)) {
                all.add("--java");
                all.add(bootJavaCommand);
            }
            if (!NutsUtils.isBlank(bootJavaOptions)) {
                all.add("--java-options");
                all.add(bootJavaOptions);
            }
            if (!NutsUtils.isBlank(bootJavaOptions)) {
                all.add("--java-options");
                all.add(bootJavaOptions);
            }
            if (!NutsUtils.isBlank(workspace)) {
                all.add("--workspace");
                all.add(NutsUtils.getAbsolutePath(workspace));
            }
            if (!NutsUtils.isBlank(login)) {
                all.add("--login");
                all.add(login);
            }
            if (!NutsUtils.isBlank(password)) {
                all.add("--password");
                all.add(password);
            }
            if (!NutsUtils.isBlank(requiredBootVersion)) {
                all.add("--boot-version");
                all.add(requiredBootVersion);
            }
            if (terminalMode != null) {
                all.add("--term");
                all.add(terminalMode.toString());
            }
            if (logConfig != null) {
                if (logConfig.getLogLevel() != null) {
                    if (logConfig.getLogLevel() == Level.FINEST) {
                        if (logConfig.isDebug()) {
                            all.add("--debug");
                        } else {
                            all.add("--verbose");
                        }
                    } else {
                        all.add("--log-" + logConfig.getLogLevel().toString().toLowerCase());
                    }
                }
                if (logConfig.getLogCount() > 0) {
                    all.add("--log-count");
                    all.add(String.valueOf(logConfig.getLogCount()));
                }
                if (logConfig.getLogSize() > 0) {
                    all.add("--log-size");
                    all.add(String.valueOf(logConfig.getLogSize()));
                }
                if (!NutsUtils.isBlank(logConfig.getLogFolder())) {
                    all.add("--log-folder");
                    all.add(NutsUtils.getAbsolutePath(logConfig.getLogFolder()));
                }
                if (!NutsUtils.isBlank(logConfig.getLogName())) {
                    all.add("--log-name");
                    all.add(logConfig.getLogName());
                }
                if (logConfig.isLogInherited()) {
                    all.add("--log-inherited");
                }
            }
            if (excludedExtensions != null && excludedExtensions.length > 0) {
                all.add("--exclude-extension");
                all.add(NutsUtils.join(";", excludedExtensions));
            }
            if (excludedRepositories != null && excludedRepositories.length > 0) {
                all.add("--exclude-repository");
                all.add(NutsUtils.join(";", excludedRepositories));
            }
            if (transientRepositories != null && transientRepositories.length > 0) {
                all.add("--repository");
                all.add(NutsUtils.join(";", transientRepositories));
            }

            if (global) {
                all.add("--global");
            }
            if (readOnly) {
                all.add("--read-only");
            }
            if (skipInstallCompanions) {
                all.add("--skip-install-companions");
            }
            if (perf) {
                all.add("--perf");
            }
            if (defaultResponse != null) {
                all.add((defaultResponse ? "--yes" : "--no"));
            }
        }
        if (createOptions) {
            if (!NutsUtils.isBlank(archetype)) {
                all.add("--archetype");
                all.add(archetype);
            }
            if (storeLocationLayout != null) {
                all.add("--store-layout");
                all.add(storeLocationLayout.toString().toLowerCase());
            }
            if (storeLocationStrategy != null) {
                all.add("--store-strategy");
                all.add(storeLocationStrategy.toString().toLowerCase());
            }
            if (repositoryStoreLocationStrategy != null) {
                all.add("--repo-store-strategy");
                all.add(repositoryStoreLocationStrategy.toString().toLowerCase());
            }
            for (int i = 0; i < storeLocations.length; i++) {
                if (!NutsUtils.isBlank(storeLocations[i])) {
                    all.add("--" + NutsStoreLocation.values()[i].name().toLowerCase() + "-location");
                    all.add(storeLocations[i]);
                }
            }
            for (int i = 0; i < NutsStoreLocationLayout.values().length; i++) {
                for (int j = 0; j < NutsStoreLocation.values().length; j++) {
                    String s = homeLocations[i * NutsStoreLocation.values().length + j];
                    if (!NutsUtils.isBlank(s)) {
                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.values()[i];
                        NutsStoreLocation folder = NutsStoreLocation.values()[j];
                        //config is exported!
                        if (!(folder == NutsStoreLocation.CONFIG)) {
                            all.add("--" + layout.name().toLowerCase() + "-" + folder.name().toLowerCase() + "-home");
                            all.add(s);
                        }
                    }
                }
            }
        }
        if (runtimeOptions) {

            if (initMode != null) {
                switch (initMode) {
                    case RECOVER: {
                        all.add("--recover");
                        break;
                    }
                    case CLEANUP: {
                        all.add("--cleanup");
                        break;
                    }
                    case RESET: {
                        all.add("--reset");
                        break;
                    }
                }
            }
            if (executionType != null) {
                switch (executionType) {
                    case EMBEDDED: {
                        all.add("--embedded");
                        break;
                    }
                    case SYSCALL: {
                        all.add("--native");
                        break;
                    }
                }
            }
            if (bootCommand != null) {
                switch (bootCommand) {
                    case EXEC: {
                        all.add("--exec");
                        break;
                    }
                    case HELP: {
                        all.add("--help");
                        break;
                    }
                    case RESET: {
                        all.add("--reset");
                        break;
                    }
                    case VERSION: {
                        all.add("--version");
                        break;
                    }
                }
            }
            all.addAll(Arrays.asList(getApplicationArguments()));
        }
        return all.toArray(new String[0]);
    }

    public String[] getApplicationArguments() {
        return applicationArguments == null ? new String[0] : Arrays.copyOf(applicationArguments, applicationArguments.length);
    }

    public NutsWorkspaceOptions setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    public boolean isPerf() {
        return perf;
    }

    public NutsWorkspaceOptions setPerf(boolean perf) {
        this.perf = perf;
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

    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceOptions setStoreLocationLayout(NutsStoreLocationLayout storeLocationLayout) {
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

    public String getHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folder) {
        return homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()];
    }

    public NutsWorkspaceOptions setStoreLocation(NutsStoreLocation folder, String value) {
        storeLocations[folder.ordinal()] = value;
        return this;
    }

    public NutsWorkspaceOptions setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folder, String value) {
        homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()] = value;
        return this;
    }

    public NutsBootInitMode getInitMode() {
        return initMode;
    }

    public NutsWorkspaceOptions setInitMode(NutsBootInitMode initMode) {
        this.initMode = initMode;
        return this;
    }

    public boolean isSkipInstallCompanions() {
        return skipInstallCompanions;
    }

    public NutsWorkspaceOptions setSkipInstallCompanions(boolean skipInstallCompanions) {
        this.skipInstallCompanions = skipInstallCompanions;
        return this;
    }

    public NutsWorkspaceOpenMode getOpenMode() {
        return openMode;
    }

    public NutsWorkspaceOptions setOpenMode(NutsWorkspaceOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsBootOptions(");
        boolean empty = true;
        if (bootRuntime != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("bootRuntime=").append(bootRuntime);
            empty = false;
        }
        if (bootRuntimeSourceURL != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("bootRuntimeSourceURL=").append(bootRuntimeSourceURL);
            empty = false;
        }
        if (classLoaderProvider != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("classLoaderProvider=").append(classLoaderProvider);
            empty = false;
        }
        if (!empty) {
            sb.append(", ");
        }
        sb.append(", archetype=").append(archetype);
        sb.append(", excludedExtensions=").append(Arrays.toString(excludedExtensions == null ? new String[0] : excludedExtensions));
        sb.append(", excludedRepositories=").append(Arrays.toString(excludedRepositories == null ? new String[0] : excludedRepositories));
        sb.append(")");
        return sb.toString();
    }

    public boolean isYes() {
        return getDefaultResponse() != null && getDefaultResponse();
    }

    public boolean isNo() {
        return getDefaultResponse() != null && !getDefaultResponse();
    }

    public String[] getStoreLocations() {
        return Arrays.copyOf(storeLocations, storeLocations.length);
    }

    public String[] getHomeLocations() {
        return Arrays.copyOf(homeLocations, homeLocations.length);
    }

}
