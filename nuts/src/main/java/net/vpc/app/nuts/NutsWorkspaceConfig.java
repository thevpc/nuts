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
import java.util.*;

public final class NutsWorkspaceConfig implements Serializable {

    private static final long serialVersionUID = 2;
    private String uuid = null;
    private boolean global;
    private String workspace = null;
    private String bootApiVersion = null;
    private String bootRuntime = null;
    private String bootRuntimeDependencies = null;
    private String bootRepositories = null;
    private String bootJavaCommand = null;
    private String bootJavaOptions = null;
    // folder types and layout types are exploded so thta it is easier 
    // to extract from json file eventhough no json library is available
    // visa simple regexp
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsStoreLocationLayout storeLocationLayout = null;

    private String programsSystemHome = null;
    private String configSystemHome = null;
    private String varSystemHome = null;
    private String libSystemHome = null;
    private String logsSystemHome = null;
    private String tempSystemHome = null;
    private String cacheSystemHome = null;

    private String programsWindowsHome = null;
    private String configWindowsHome = null;
    private String varWindowsHome = null;
    private String libWindowsHome = null;
    private String logsWindowsHome = null;
    private String tempWindowsHome = null;
    private String cacheWindowsHome = null;

    private String programsLinuxHome = null;
    private String configLinuxHome = null;
    private String varLinuxHome = null;
    private String libLinuxHome = null;
    private String logsLinuxHome = null;
    private String tempLinuxHome = null;
    private String cacheLinuxHome = null;

    private List<NutsRepositoryRef> repositories;
    private List<NutsId> extensions;
    private List<NutsWorkspaceCommandFactoryConfig> commandFactories;
    private Properties env = new Properties();
    private List<NutsSdkLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private boolean secure = false;
    private List<NutsUserConfig> users = new ArrayList<>();

    public NutsWorkspaceConfig() {
    }

    public NutsWorkspaceConfig(NutsWorkspaceConfig other) {
        this.secure = other.isSecure();
        this.workspace = other.getWorkspace();
        this.bootApiVersion = other.getBootApiVersion();
        this.bootRuntime = other.getBootRuntime();
        this.bootRuntimeDependencies = other.getBootRuntimeDependencies();
        this.bootRepositories = other.getBootRepositories();
        this.storeLocationStrategy = other.getStoreLocationStrategy();
        this.storeLocationLayout = other.getStoreLocationLayout();

        this.programsStoreLocation = other.getProgramsStoreLocation();
        this.configStoreLocation = other.getConfigStoreLocation();
        this.varStoreLocation = other.getVarStoreLocation();
        this.libStoreLocation = other.getLibStoreLocation();
        this.logsStoreLocation = other.getLogsStoreLocation();
        this.tempStoreLocation = other.getTempStoreLocation();
        this.cacheStoreLocation = other.getCacheStoreLocation();

        this.programsWindowsHome = other.getProgramsWindowsHome();
        this.configWindowsHome = other.getConfigWindowsHome();
        this.varWindowsHome = other.getVarWindowsHome();
        this.libWindowsHome = other.getLibWindowsHome();
        this.logsWindowsHome = other.getLogsWindowsHome();
        this.tempWindowsHome = other.getTempWindowsHome();
        this.cacheWindowsHome = other.getCacheWindowsHome();

        this.programsLinuxHome = other.getProgramsLinuxHome();
        this.configLinuxHome = other.getConfigLinuxHome();
        this.varLinuxHome = other.getVarLinuxHome();
        this.libLinuxHome = other.getLibLinuxHome();
        this.logsLinuxHome = other.getLogsLinuxHome();
        this.tempLinuxHome = other.getTempLinuxHome();
        this.cacheLinuxHome = other.getCacheLinuxHome();

        this.bootJavaCommand = other.getBootJavaCommand();
        this.bootJavaOptions = other.getBootJavaOptions();
        this.global = other.isGlobal();
        this.repositories = other.getRepositories() == null ? null : new ArrayList<>(other.getRepositories());;
        this.users = other.getUsers() == null ? null : new ArrayList<>(other.getUsers());
        this.sdk = other.getSdk() == null ? null : new ArrayList<>(other.getSdk());
        this.extensions = other.getExtensions() == null ? null : new ArrayList<>(other.getExtensions());
        this.imports = other.getImports() == null ? null : new ArrayList<>(other.getImports());
        if (other.getEnv() == null) {
            this.env = null;
        } else {
            this.env = new Properties();
            this.env.putAll(other.getEnv());
        }
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public List<NutsRepositoryRef> getRepositories() {
        return repositories;
    }

    public NutsWorkspaceConfig setRepositories(List<NutsRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NutsWorkspaceConfig setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public List<NutsId> getExtensions() {
        return extensions;
    }

    public NutsWorkspaceConfig setExtensions(List<NutsId> extensions) {
        this.extensions = extensions;
        return this;
    }

    public NutsWorkspaceConfig setCommandFactories(List<NutsWorkspaceCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NutsWorkspaceConfig setSdk(List<NutsSdkLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Properties getEnv() {
        return env;
    }

    public NutsWorkspaceConfig setEnv(Properties env) {
        this.env = env;
        return this;
    }

    public NutsWorkspaceConfig setUsers(List<NutsUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NutsUserConfig> getUsers() {
        return users;
    }

    public List<NutsSdkLocation> getSdk() {
        return sdk;
    }

    public boolean isSecure() {
        return secure;
    }

    public NutsWorkspaceConfig setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public String getBootApiVersion() {
        return bootApiVersion;
    }

    public NutsWorkspaceConfig setBootApiVersion(String bootApiVersion) {
        this.bootApiVersion = bootApiVersion;
        return this;
    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsWorkspaceConfig setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public String getBootRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    public NutsWorkspaceConfig setBootRuntimeDependencies(String bootRuntimeDependencies) {
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsWorkspaceConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public NutsWorkspaceConfig setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public NutsWorkspaceConfig setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public List<NutsWorkspaceCommandFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsWorkspaceConfig setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsWorkspaceConfig setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsWorkspaceConfig setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public NutsWorkspaceConfig setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsWorkspaceConfig setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsWorkspaceConfig setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsWorkspaceConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceConfig setStoreLocationLayout(NutsStoreLocationLayout storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsWorkspaceConfig setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsWorkspaceConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getProgramsSystemHome() {
        return programsSystemHome;
    }

    public void setProgramsSystemHome(String programsSystemHome) {
        this.programsSystemHome = programsSystemHome;
    }

    public String getConfigSystemHome() {
        return configSystemHome;
    }

    public void setConfigSystemHome(String configSystemHome) {
        this.configSystemHome = configSystemHome;
    }

    public String getVarSystemHome() {
        return varSystemHome;
    }

    public void setVarSystemHome(String varSystemHome) {
        this.varSystemHome = varSystemHome;
    }

    public String getLibSystemHome() {
        return libSystemHome;
    }

    public void setLibSystemHome(String libSystemHome) {
        this.libSystemHome = libSystemHome;
    }

    public String getLogsSystemHome() {
        return logsSystemHome;
    }

    public void setLogsSystemHome(String logsSystemHome) {
        this.logsSystemHome = logsSystemHome;
    }

    public String getTempSystemHome() {
        return tempSystemHome;
    }

    public void setTempSystemHome(String tempSystemHome) {
        this.tempSystemHome = tempSystemHome;
    }

    public String getCacheSystemHome() {
        return cacheSystemHome;
    }

    public void setCacheSystemHome(String cacheSystemHome) {
        this.cacheSystemHome = cacheSystemHome;
    }

    public String getProgramsWindowsHome() {
        return programsWindowsHome;
    }

    public void setProgramsWindowsHome(String programsWindowsHome) {
        this.programsWindowsHome = programsWindowsHome;
    }

    public String getConfigWindowsHome() {
        return configWindowsHome;
    }

    public void setConfigWindowsHome(String configWindowsHome) {
        this.configWindowsHome = configWindowsHome;
    }

    public String getVarWindowsHome() {
        return varWindowsHome;
    }

    public void setVarWindowsHome(String varWindowsHome) {
        this.varWindowsHome = varWindowsHome;
    }

    public String getLibWindowsHome() {
        return libWindowsHome;
    }

    public void setLibWindowsHome(String libWindowsHome) {
        this.libWindowsHome = libWindowsHome;
    }

    public String getLogsWindowsHome() {
        return logsWindowsHome;
    }

    public void setLogsWindowsHome(String logsWindowsHome) {
        this.logsWindowsHome = logsWindowsHome;
    }

    public String getTempWindowsHome() {
        return tempWindowsHome;
    }

    public void setTempWindowsHome(String tempWindowsHome) {
        this.tempWindowsHome = tempWindowsHome;
    }

    public String getCacheWindowsHome() {
        return cacheWindowsHome;
    }

    public void setCacheWindowsHome(String cacheWindowsHome) {
        this.cacheWindowsHome = cacheWindowsHome;
    }

    public String getProgramsLinuxHome() {
        return programsLinuxHome;
    }

    public void setProgramsLinuxHome(String programsLinuxHome) {
        this.programsLinuxHome = programsLinuxHome;
    }

    public String getConfigLinuxHome() {
        return configLinuxHome;
    }

    public void setConfigLinuxHome(String configLinuxHome) {
        this.configLinuxHome = configLinuxHome;
    }

    public String getVarLinuxHome() {
        return varLinuxHome;
    }

    public void setVarLinuxHome(String varLinuxHome) {
        this.varLinuxHome = varLinuxHome;
    }

    public String getLibLinuxHome() {
        return libLinuxHome;
    }

    public void setLibLinuxHome(String libLinuxHome) {
        this.libLinuxHome = libLinuxHome;
    }

    public String getLogsLinuxHome() {
        return logsLinuxHome;
    }

    public void setLogsLinuxHome(String logsLinuxHome) {
        this.logsLinuxHome = logsLinuxHome;
    }

    public String getTempLinuxHome() {
        return tempLinuxHome;
    }

    public void setTempLinuxHome(String tempLinuxHome) {
        this.tempLinuxHome = tempLinuxHome;
    }

    public String getCacheLinuxHome() {
        return cacheLinuxHome;
    }

    public void setCacheLinuxHome(String cacheLinuxHome) {
        this.cacheLinuxHome = cacheLinuxHome;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

}
