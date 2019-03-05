/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/23/17.
 */
public final class NutsWorkspaceOptions implements Serializable, Cloneable {

    private String bootRuntime;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private String bootRuntimeSourceURL;
//    private String[] bootArguments;
    private String[] applicationArguments;
    private boolean perf = false;
    private String workspace = null;
    private boolean skipPostCreateInstallCompanionTools;
    private NutsWorkspaceOpenMode openMode=NutsWorkspaceOpenMode.DEFAULT;
    /**
     * if true, all means are deployed to recover from corrupted workspace.
     * This flag may alter current used version of nuts to update to latest.
     */
    private boolean recover;
    /**
     * if true consider global/system repository
     */
    private boolean global;
    private String archetype;
    private String[] excludedExtensions;
    private String[] excludedRepositories;
    private String[] transientRepositories;
    private String login = null;
    private String password = null;
    private String autoConfig = null;
    private NutsTerminalMode terminalMode = null;
    private boolean readOnly = false;
    private long creationTime;
    private NutsClassLoaderProvider classLoaderProvider;
    private String[] executorOptions;
    private NutsBootCommand bootCommand = NutsBootCommand.EXEC;
    private NutsLogConfig logConfig;
    private NutsExecutionType executionType = NutsExecutionType.EXTERNAL;
    private String requiredBootVersion = null;
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private String libStoreLocation = null;
    private NutsStoreLocationLayout storeLocationLayout = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;

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

    public String getBootArgumentsString() {
        return NutsMinimalCommandLine.escapeArguments(getBootArguments());
    }

    public String[] getBootArguments() {
        List<String> all=new ArrayList<>();
        if(!NutsUtils.isEmpty(bootRuntime)){
            all.add("--boot-runtime");
            all.add(bootRuntime);
        }
        if(!NutsUtils.isEmpty(bootJavaCommand)){
            all.add("--java");
            all.add(bootJavaCommand);
        }
        if(!NutsUtils.isEmpty(bootJavaOptions)){
            all.add("--java-options");
            all.add(bootJavaOptions);
        }
        if(!NutsUtils.isEmpty(bootJavaOptions)){
            all.add("--java-options");
            all.add(bootJavaOptions);
        }
        if(!NutsUtils.isEmpty(workspace)){
            all.add("--workspace");
            all.add(NutsUtils.getAbsolutePath(workspace));
        }
        if(!NutsUtils.isEmpty(archetype)){
            all.add("--archetype");
            all.add(archetype);
        }
        if(!NutsUtils.isEmpty(login)){
            all.add("--login");
            all.add(login);
        }
        if(!NutsUtils.isEmpty(password)){
            all.add("--password");
            all.add(password);
        }
        if(!NutsUtils.isEmpty(requiredBootVersion)){
            all.add("--boot-version");
            all.add(requiredBootVersion);
        }
        if(!NutsUtils.isEmpty(autoConfig)){
            all.add("--auto-config");
            all.add(autoConfig);
        }
        if(storeLocationLayout!=null){
            all.add("--store-layout");
            all.add(storeLocationLayout.toString().toLowerCase());
        }
        if(storeLocationStrategy!=null){
            all.add("--store-strategy");
            all.add(storeLocationStrategy.toString().toLowerCase());
        }
        if(repositoryStoreLocationStrategy!=null){
            all.add("--repo-store-strategy");
            all.add(repositoryStoreLocationStrategy.toString().toLowerCase());
        }
        if(!NutsUtils.isEmpty(programsStoreLocation)){
            all.add("--programs-location");
            all.add(programsStoreLocation);
        }
        if(!NutsUtils.isEmpty(configStoreLocation)){
            all.add("--config-location");
            all.add(configStoreLocation);
        }
        if(!NutsUtils.isEmpty(varStoreLocation)){
            all.add("--var-location");
            all.add(varStoreLocation);
        }
        if(!NutsUtils.isEmpty(logsStoreLocation)){
            all.add("--logs-location");
            all.add(logsStoreLocation);
        }
        if(!NutsUtils.isEmpty(tempStoreLocation)){
            all.add("--temp-location");
            all.add(tempStoreLocation);
        }
        if(!NutsUtils.isEmpty(cacheStoreLocation)){
            all.add("--cache-location");
            all.add(cacheStoreLocation);
        }
        if(!NutsUtils.isEmpty(libStoreLocation)){
            all.add("--cache-location");
            all.add(libStoreLocation);
        }

        if(terminalMode!=null){
            all.add("--term");
            all.add(terminalMode.toString());
        }
        if(logConfig!=null){
            if(logConfig.getLogLevel()!=null){
                all.add("--log-"+logConfig.getLogLevel().toString().toLowerCase());
            }
            if(logConfig.getLogCount()>0){
                all.add("--log-count");
                all.add(String.valueOf(logConfig.getLogCount()));
            }
            if(logConfig.getLogSize()>0){
                all.add("--log-size");
                all.add(String.valueOf(logConfig.getLogSize()));
            }
            if(!NutsUtils.isEmpty(logConfig.getLogFolder())){
                all.add("--log-folder");
                all.add(NutsUtils.getAbsolutePath(logConfig.getLogFolder()));
            }
            if(!NutsUtils.isEmpty(logConfig.getLogName())){
                all.add("--log-name");
                all.add(logConfig.getLogName());
            }
            if(logConfig.isLogInherited()){
                all.add("--log-inherited");
            }
        }
        if(excludedExtensions!=null && excludedExtensions.length>0){
            all.add("--exclude-extension");
            all.add(NutsUtils.join(";",excludedExtensions));
        }
        if(excludedRepositories!=null && excludedRepositories.length>0){
            all.add("--exclude-repository");
            all.add(NutsUtils.join(";",excludedRepositories));
        }
        if(transientRepositories!=null && transientRepositories.length>0){
            all.add("--repository");
            all.add(NutsUtils.join(";",transientRepositories));
        }

        if(global){
            all.add("--global");
        }
        if(readOnly){
            all.add("--read-only");
        }
        if(skipPostCreateInstallCompanionTools){
            all.add("--skip-install-companions");
        }
        if(recover){
            all.add("--recover");
        }
        if(perf){
            all.add("--perf");
        }
        return all.toArray(new String[0]);
    }

    public String[] getApplicationArguments() {
        return applicationArguments;
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

    public String getAutoConfig() {
        return autoConfig;
    }

    public NutsWorkspaceOptions setAutoConfig(String autoConfig) {
        this.autoConfig = autoConfig;
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

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsWorkspaceOptions setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsWorkspaceOptions setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsWorkspaceOptions setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public NutsWorkspaceOptions setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsWorkspaceOptions setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsWorkspaceOptions setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsWorkspaceOptions setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public boolean isRecover() {
        return recover;
    }

    public NutsWorkspaceOptions setRecover(boolean recover) {
        this.recover = recover;
        return this;
    }

    public boolean isSkipPostCreateInstallCompanionTools() {
        return skipPostCreateInstallCompanionTools;
    }

    public NutsWorkspaceOptions setSkipPostCreateInstallCompanionTools(boolean skipPostCreateInstallCompanionTools) {
        this.skipPostCreateInstallCompanionTools = skipPostCreateInstallCompanionTools;
        return this;
    }

    public NutsWorkspaceOpenMode getOpenMode() {
        return openMode;
    }

    public NutsWorkspaceOptions setOpenMode(NutsWorkspaceOpenMode openMode) {
        this.openMode = openMode;
        return this;
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


}
