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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfig implements Serializable {

    private static final long serialVersionUID = 2;
    private String uuid = null;
    private boolean global;
    private String workspace = null;
    private String bootApiVersion = null;

    /**
     * Api version having created the config
     */
    private String createApiVersion = null;

    /**
     * boot component Id in long format (as defined in
     * {@link NutsId#getLongName()})
     *
     * @see NutsId#getLongNameId()
     */
    private String bootRuntime = null;

    /**
     * ';' separated list of component Ids in long format (as defined in
     * {@link NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These components should be accessible
     * from {@link NutsWorkspaceConfig#getBootRepositories()}
     *
     * @see NutsId#getLongNameId()
     */
    private String bootRuntimeDependencies = null;
    private String bootRepositories = null;
    private String bootJavaCommand = null;
    private String bootJavaOptions = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file eventhough no json library is available
    // via simple regexp
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    /**
     * @since 0.5.6
     */
    private String runStoreLocation = null;

    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsOsFamily storeLocationLayout = null;

    private String programsSystemHome = null;
    private String configSystemHome = null;
    private String varSystemHome = null;
    private String libSystemHome = null;
    private String logSystemHome = null;
    private String tempSystemHome = null;
    private String cacheSystemHome = null;
    /**
     * @since 0.5.6
     */
    private String runSystemHome = null;

    private String programsWindowsHome = null;
    private String configWindowsHome = null;
    private String varWindowsHome = null;
    private String libWindowsHome = null;
    private String logWindowsHome = null;
    private String tempWindowsHome = null;
    private String cacheWindowsHome = null;
    /**
     * @since 0.5.6
     */
    private String runWindowsHome = null;

    private String programsMacOsHome = null;
    private String configMacOsHome = null;
    private String varMacOsHome = null;
    private String libMacOsHome = null;
    private String logMacOsHome = null;
    private String tempMacOsHome = null;
    private String cacheMacOsHome = null;
    /**
     * @since 0.5.6
     */
    private String runMacOsHome = null;

    private String programsLinuxHome = null;
    private String configLinuxHome = null;
    private String varLinuxHome = null;
    private String libLinuxHome = null;
    private String logLinuxHome = null;
    private String tempLinuxHome = null;
    private String cacheLinuxHome = null;
    /**
     * @since 0.5.6
     */
    private String runLinuxHome = null;

    private List<NutsRepositoryRef> repositories;
    private List<NutsId> extensions;
    private List<NutsCommandAliasFactoryConfig> commandFactories;
    private Properties env = new Properties();
    private List<NutsSdkLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private boolean secure = false;
    private String authenticationAgent;
    private List<NutsUserConfig> users = new ArrayList<>();

    public NutsWorkspaceConfig() {
    }

    public NutsWorkspaceConfig(NutsWorkspaceConfig other) {
        this.secure = other.isSecure();
        this.workspace = other.getWorkspace();
        this.bootApiVersion = other.getBootApiVersion();
        this.createApiVersion = other.getCreateApiVersion();
        this.bootRuntime = other.getBootRuntime();
        this.bootRuntimeDependencies = other.getBootRuntimeDependencies();
        this.bootRepositories = other.getBootRepositories();
        this.storeLocationStrategy = other.getStoreLocationStrategy();
        this.storeLocationLayout = other.getStoreLocationLayout();

        this.programsStoreLocation = other.getProgramsStoreLocation();
        this.configStoreLocation = other.getConfigStoreLocation();
        this.varStoreLocation = other.getVarStoreLocation();
        this.libStoreLocation = other.getLibStoreLocation();
        this.logStoreLocation = other.getLogStoreLocation();
        this.tempStoreLocation = other.getTempStoreLocation();
        this.cacheStoreLocation = other.getCacheStoreLocation();
        this.runStoreLocation = other.getRunStoreLocation();

        this.programsWindowsHome = other.getProgramsWindowsHome();
        this.configWindowsHome = other.getConfigWindowsHome();
        this.varWindowsHome = other.getVarWindowsHome();
        this.libWindowsHome = other.getLibWindowsHome();
        this.logWindowsHome = other.getLogWindowsHome();
        this.tempWindowsHome = other.getTempWindowsHome();
        this.cacheWindowsHome = other.getCacheWindowsHome();
        this.runWindowsHome = other.getRunWindowsHome();

        this.programsMacOsHome = other.getProgramsMacOsHome();
        this.configMacOsHome = other.getConfigMacOsHome();
        this.varMacOsHome = other.getVarMacOsHome();
        this.libMacOsHome = other.getLibMacOsHome();
        this.logMacOsHome = other.getLogMacOsHome();
        this.tempMacOsHome = other.getTempMacOsHome();
        this.cacheMacOsHome = other.getCacheMacOsHome();
        this.runMacOsHome = other.getRunMacOsHome();

        this.programsLinuxHome = other.getProgramsLinuxHome();
        this.configLinuxHome = other.getConfigLinuxHome();
        this.varLinuxHome = other.getVarLinuxHome();
        this.libLinuxHome = other.getLibLinuxHome();
        this.logLinuxHome = other.getLogLinuxHome();
        this.tempLinuxHome = other.getTempLinuxHome();
        this.cacheLinuxHome = other.getCacheLinuxHome();
        this.runLinuxHome = other.getRunLinuxHome();

        this.authenticationAgent = other.getAuthenticationAgent();

        this.bootJavaCommand = other.getBootJavaCommand();
        this.bootJavaOptions = other.getBootJavaOptions();
        this.global = other.isGlobal();
        this.repositories = other.getRepositories() == null ? null : new ArrayList<>(other.getRepositories());
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

    public NutsWorkspaceConfig setCommandFactories(List<NutsCommandAliasFactoryConfig> commandFactories) {
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

    public String getCreateApiVersion() {
        return createApiVersion;
    }

    public NutsWorkspaceConfig setCreateApiVersion(String createApiVersion) {
        this.createApiVersion = createApiVersion;
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

    public List<NutsCommandAliasFactoryConfig> getCommandFactories() {
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

    public String getLogStoreLocation() {
        return logStoreLocation;
    }

    public NutsWorkspaceConfig setLogStoreLocation(String logStoreLocation) {
        this.logStoreLocation = logStoreLocation;
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

    public String getRunStoreLocation() {
        return runStoreLocation;
    }

    public NutsWorkspaceConfig setRunStoreLocation(String runStoreLocation) {
        this.runStoreLocation = runStoreLocation;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsWorkspaceConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
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

    public NutsWorkspaceConfig setProgramsSystemHome(String programsSystemHome) {
        this.programsSystemHome = programsSystemHome;
        return this;
    }

    public String getConfigSystemHome() {
        return configSystemHome;
    }

    public NutsWorkspaceConfig setConfigSystemHome(String configSystemHome) {
        this.configSystemHome = configSystemHome;
        return this;
    }

    public String getVarSystemHome() {
        return varSystemHome;
    }

    public NutsWorkspaceConfig setVarSystemHome(String varSystemHome) {
        this.varSystemHome = varSystemHome;
        return this;
    }

    public String getLibSystemHome() {
        return libSystemHome;
    }

    public NutsWorkspaceConfig setLibSystemHome(String libSystemHome) {
        this.libSystemHome = libSystemHome;
        return this;
    }

    public String getLogSystemHome() {
        return logSystemHome;
    }

    public NutsWorkspaceConfig setLogSystemHome(String logSystemHome) {
        this.logSystemHome = logSystemHome;
        return this;
    }

    public String getTempSystemHome() {
        return tempSystemHome;
    }

    public NutsWorkspaceConfig setTempSystemHome(String tempSystemHome) {
        this.tempSystemHome = tempSystemHome;
        return this;
    }

    public String getCacheSystemHome() {
        return cacheSystemHome;
    }

    public NutsWorkspaceConfig setCacheSystemHome(String cacheSystemHome) {
        this.cacheSystemHome = cacheSystemHome;
        return this;
    }

    public String getRunSystemHome() {
        return runSystemHome;
    }

    public NutsWorkspaceConfig setRunSystemHome(String runSystemHome) {
        this.runSystemHome = runSystemHome;
        return this;
    }

    public String getProgramsWindowsHome() {
        return programsWindowsHome;
    }

    public NutsWorkspaceConfig setProgramsWindowsHome(String programsWindowsHome) {
        this.programsWindowsHome = programsWindowsHome;
        return this;
    }

    public String getConfigWindowsHome() {
        return configWindowsHome;
    }

    public NutsWorkspaceConfig setConfigWindowsHome(String configWindowsHome) {
        this.configWindowsHome = configWindowsHome;
        return this;
    }

    public String getVarWindowsHome() {
        return varWindowsHome;
    }

    public NutsWorkspaceConfig setVarWindowsHome(String varWindowsHome) {
        this.varWindowsHome = varWindowsHome;
        return this;
    }

    public String getLibWindowsHome() {
        return libWindowsHome;
    }

    public NutsWorkspaceConfig setLibWindowsHome(String libWindowsHome) {
        this.libWindowsHome = libWindowsHome;
        return this;
    }

    public String getLogWindowsHome() {
        return logWindowsHome;
    }

    public NutsWorkspaceConfig setLogWindowsHome(String logWindowsHome) {
        this.logWindowsHome = logWindowsHome;
        return this;
    }

    public String getTempWindowsHome() {
        return tempWindowsHome;
    }

    public NutsWorkspaceConfig setTempWindowsHome(String tempWindowsHome) {
        this.tempWindowsHome = tempWindowsHome;
        return this;
    }

    public String getCacheWindowsHome() {
        return cacheWindowsHome;
    }

    public NutsWorkspaceConfig setCacheWindowsHome(String cacheWindowsHome) {
        this.cacheWindowsHome = cacheWindowsHome;
        return this;
    }

    public String getRunWindowsHome() {
        return runWindowsHome;
    }

    public NutsWorkspaceConfig setRunWindowsHome(String runWindowsHome) {
        this.runWindowsHome = runWindowsHome;
        return this;
    }

    public String getProgramsMacOsHome() {
        return programsMacOsHome;
    }

    public NutsWorkspaceConfig setProgramsMacOsHome(String programsMacOsHome) {
        this.programsMacOsHome = programsMacOsHome;
        return this;
    }

    public String getConfigMacOsHome() {
        return configMacOsHome;
    }

    public NutsWorkspaceConfig setConfigMacOsHome(String configMacOsHome) {
        this.configMacOsHome = configMacOsHome;
        return this;
    }

    public String getVarMacOsHome() {
        return varMacOsHome;
    }

    public NutsWorkspaceConfig setVarMacOsHome(String varMacOsHome) {
        this.varMacOsHome = varMacOsHome;
        return this;
    }

    public String getLibMacOsHome() {
        return libMacOsHome;
    }

    public NutsWorkspaceConfig setLibMacOsHome(String libMacOsHome) {
        this.libMacOsHome = libMacOsHome;
        return this;
    }

    public String getLogMacOsHome() {
        return logMacOsHome;
    }

    public NutsWorkspaceConfig setLogMacOsHome(String logMacOsHome) {
        this.logMacOsHome = logMacOsHome;
        return this;
    }

    public String getTempMacOsHome() {
        return tempMacOsHome;
    }

    public NutsWorkspaceConfig setTempMacOsHome(String tempMacOsHome) {
        this.tempMacOsHome = tempMacOsHome;
        return this;
    }

    public String getCacheMacOsHome() {
        return cacheMacOsHome;
    }

    public NutsWorkspaceConfig setCacheMacOsHome(String cacheMacOsHome) {
        this.cacheMacOsHome = cacheMacOsHome;
        return this;
    }

    public String getRunMacOsHome() {
        return runMacOsHome;
    }

    public NutsWorkspaceConfig setRunMacOsHome(String runMacOsHome) {
        this.runMacOsHome = runMacOsHome;
        return this;
    }

    public String getProgramsLinuxHome() {
        return programsLinuxHome;
    }

    public NutsWorkspaceConfig setProgramsLinuxHome(String programsLinuxHome) {
        this.programsLinuxHome = programsLinuxHome;
        return this;
    }

    public String getConfigLinuxHome() {
        return configLinuxHome;
    }

    public NutsWorkspaceConfig setConfigLinuxHome(String configLinuxHome) {
        this.configLinuxHome = configLinuxHome;
        return this;
    }

    public String getVarLinuxHome() {
        return varLinuxHome;
    }

    public NutsWorkspaceConfig setVarLinuxHome(String varLinuxHome) {
        this.varLinuxHome = varLinuxHome;
        return this;
    }

    public String getLibLinuxHome() {
        return libLinuxHome;
    }

    public NutsWorkspaceConfig setLibLinuxHome(String libLinuxHome) {
        this.libLinuxHome = libLinuxHome;
        return this;
    }

    public String getLogLinuxHome() {
        return logLinuxHome;
    }

    public NutsWorkspaceConfig setLogLinuxHome(String logLinuxHome) {
        this.logLinuxHome = logLinuxHome;
        return this;
    }

    public String getTempLinuxHome() {
        return tempLinuxHome;
    }

    public NutsWorkspaceConfig setTempLinuxHome(String tempLinuxHome) {
        this.tempLinuxHome = tempLinuxHome;
        return this;
    }

    public String getCacheLinuxHome() {
        return cacheLinuxHome;
    }

    public NutsWorkspaceConfig setCacheLinuxHome(String cacheLinuxHome) {
        this.cacheLinuxHome = cacheLinuxHome;
        return this;
    }

    public String getRunLinuxHome() {
        return runLinuxHome;
    }

    public NutsWorkspaceConfig setRunLinuxHome(String runLinuxHome) {
        this.runLinuxHome = runLinuxHome;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public NutsWorkspaceConfig setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsWorkspaceConfig setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.uuid);
        hash = 37 * hash + (this.global ? 1 : 0);
        hash = 37 * hash + Objects.hashCode(this.workspace);
        hash = 37 * hash + Objects.hashCode(this.bootApiVersion);
        hash = 37 * hash + Objects.hashCode(this.createApiVersion);
        hash = 37 * hash + Objects.hashCode(this.bootRuntime);
        hash = 37 * hash + Objects.hashCode(this.bootRuntimeDependencies);
        hash = 37 * hash + Objects.hashCode(this.bootRepositories);
        hash = 37 * hash + Objects.hashCode(this.bootJavaCommand);
        hash = 37 * hash + Objects.hashCode(this.bootJavaOptions);
        hash = 37 * hash + Objects.hashCode(this.programsStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.configStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.varStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.libStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.logStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.tempStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.cacheStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.runStoreLocation);
        hash = 37 * hash + Objects.hashCode(this.repositoryStoreLocationStrategy);
        hash = 37 * hash + Objects.hashCode(this.storeLocationStrategy);
        hash = 37 * hash + Objects.hashCode(this.storeLocationLayout);
        hash = 37 * hash + Objects.hashCode(this.programsSystemHome);
        hash = 37 * hash + Objects.hashCode(this.configSystemHome);
        hash = 37 * hash + Objects.hashCode(this.varSystemHome);
        hash = 37 * hash + Objects.hashCode(this.libSystemHome);
        hash = 37 * hash + Objects.hashCode(this.logSystemHome);
        hash = 37 * hash + Objects.hashCode(this.tempSystemHome);
        hash = 37 * hash + Objects.hashCode(this.cacheSystemHome);
        hash = 37 * hash + Objects.hashCode(this.runSystemHome);
        hash = 37 * hash + Objects.hashCode(this.programsWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.configWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.varWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.libWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.logWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.tempWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.cacheWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.runWindowsHome);
        hash = 37 * hash + Objects.hashCode(this.programsMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.configMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.varMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.libMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.logMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.tempMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.cacheMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.runMacOsHome);
        hash = 37 * hash + Objects.hashCode(this.programsLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.configLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.varLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.libLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.logLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.tempLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.cacheLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.runLinuxHome);
        hash = 37 * hash + Objects.hashCode(this.repositories);
        hash = 37 * hash + Objects.hashCode(this.extensions);
        hash = 37 * hash + Objects.hashCode(this.commandFactories);
        hash = 37 * hash + Objects.hashCode(this.env);
        hash = 37 * hash + Objects.hashCode(this.sdk);
        hash = 37 * hash + Objects.hashCode(this.imports);
        hash = 37 * hash + (this.secure ? 1 : 0);
        hash = 37 * hash + Objects.hashCode(this.authenticationAgent);
        hash = 37 * hash + Objects.hashCode(this.users);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsWorkspaceConfig other = (NutsWorkspaceConfig) obj;
        if (this.global != other.global) {
            return false;
        }
        if (this.secure != other.secure) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.workspace, other.workspace)) {
            return false;
        }
        if (!Objects.equals(this.bootApiVersion, other.bootApiVersion)) {
            return false;
        }
        if (!Objects.equals(this.createApiVersion, other.createApiVersion)) {
            return false;
        }
        if (!Objects.equals(this.bootRuntime, other.bootRuntime)) {
            return false;
        }
        if (!Objects.equals(this.bootRuntimeDependencies, other.bootRuntimeDependencies)) {
            return false;
        }
        if (!Objects.equals(this.bootRepositories, other.bootRepositories)) {
            return false;
        }
        if (!Objects.equals(this.bootJavaCommand, other.bootJavaCommand)) {
            return false;
        }
        if (!Objects.equals(this.bootJavaOptions, other.bootJavaOptions)) {
            return false;
        }
        if (!Objects.equals(this.programsStoreLocation, other.programsStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.configStoreLocation, other.configStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.varStoreLocation, other.varStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.libStoreLocation, other.libStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.logStoreLocation, other.logStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.tempStoreLocation, other.tempStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.cacheStoreLocation, other.cacheStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.runStoreLocation, other.runStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.programsSystemHome, other.programsSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.configSystemHome, other.configSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.varSystemHome, other.varSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.libSystemHome, other.libSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.logSystemHome, other.logSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.tempSystemHome, other.tempSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.cacheSystemHome, other.cacheSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.runSystemHome, other.runSystemHome)) {
            return false;
        }
        if (!Objects.equals(this.programsWindowsHome, other.programsWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.configWindowsHome, other.configWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.varWindowsHome, other.varWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.libWindowsHome, other.libWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.logWindowsHome, other.logWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.tempWindowsHome, other.tempWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.cacheWindowsHome, other.cacheWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.runWindowsHome, other.runWindowsHome)) {
            return false;
        }
        if (!Objects.equals(this.programsMacOsHome, other.programsMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.configMacOsHome, other.configMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.varMacOsHome, other.varMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.libMacOsHome, other.libMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.logMacOsHome, other.logMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.tempMacOsHome, other.tempMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.cacheMacOsHome, other.cacheMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.runMacOsHome, other.runMacOsHome)) {
            return false;
        }
        if (!Objects.equals(this.programsLinuxHome, other.programsLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.configLinuxHome, other.configLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.varLinuxHome, other.varLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.libLinuxHome, other.libLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.logLinuxHome, other.logLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.tempLinuxHome, other.tempLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.cacheLinuxHome, other.cacheLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.runLinuxHome, other.runLinuxHome)) {
            return false;
        }
        if (!Objects.equals(this.authenticationAgent, other.authenticationAgent)) {
            return false;
        }
        if (this.repositoryStoreLocationStrategy != other.repositoryStoreLocationStrategy) {
            return false;
        }
        if (this.storeLocationStrategy != other.storeLocationStrategy) {
            return false;
        }
        if (this.storeLocationLayout != other.storeLocationLayout) {
            return false;
        }
        if (!Objects.equals(this.repositories, other.repositories)) {
            return false;
        }
        if (!Objects.equals(this.extensions, other.extensions)) {
            return false;
        }
        if (!Objects.equals(this.commandFactories, other.commandFactories)) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.sdk, other.sdk)) {
            return false;
        }
        if (!Objects.equals(this.imports, other.imports)) {
            return false;
        }
        if (!Objects.equals(this.users, other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceConfig{" + "uuid=" + uuid + ", global=" + global + ", workspace=" + workspace + ", bootApiVersion=" + bootApiVersion + ", createApiVersion=" + createApiVersion + ", bootRuntime=" + bootRuntime + ", bootRuntimeDependencies=" + bootRuntimeDependencies + ", bootRepositories=" + bootRepositories + ", bootJavaCommand=" + bootJavaCommand + ", bootJavaOptions=" + bootJavaOptions + ", programsStoreLocation=" + programsStoreLocation + ", configStoreLocation=" + configStoreLocation + ", varStoreLocation=" + varStoreLocation + ", libStoreLocation=" + libStoreLocation + ", logStoreLocation=" + logStoreLocation + ", tempStoreLocation=" + tempStoreLocation + ", cacheStoreLocation=" + cacheStoreLocation + ", runStoreLocation=" + runStoreLocation + ", repositoryStoreLocationStrategy=" + repositoryStoreLocationStrategy + ", storeLocationStrategy=" + storeLocationStrategy + ", storeLocationLayout=" + storeLocationLayout + ", programsSystemHome=" + programsSystemHome + ", configSystemHome=" + configSystemHome + ", varSystemHome=" + varSystemHome + ", libSystemHome=" + libSystemHome + ", logSystemHome=" + logSystemHome + ", tempSystemHome=" + tempSystemHome + ", cacheSystemHome=" + cacheSystemHome + ", runSystemHome=" + runSystemHome + ", programsWindowsHome=" + programsWindowsHome + ", configWindowsHome=" + configWindowsHome + ", varWindowsHome=" + varWindowsHome + ", libWindowsHome=" + libWindowsHome + ", logWindowsHome=" + logWindowsHome + ", tempWindowsHome=" + tempWindowsHome + ", cacheWindowsHome=" + cacheWindowsHome + ", runWindowsHome=" + runWindowsHome + ", programsMacOsHome=" + programsMacOsHome + ", configMacOsHome=" + configMacOsHome + ", varMacOsHome=" + varMacOsHome + ", libMacOsHome=" + libMacOsHome + ", logMacOsHome=" + logMacOsHome + ", tempMacOsHome=" + tempMacOsHome + ", cacheMacOsHome=" + cacheMacOsHome + ", runMacOsHome=" + runMacOsHome + ", programsLinuxHome=" + programsLinuxHome + ", configLinuxHome=" + configLinuxHome + ", varLinuxHome=" + varLinuxHome + ", libLinuxHome=" + libLinuxHome + ", logLinuxHome=" + logLinuxHome + ", tempLinuxHome=" + tempLinuxHome + ", cacheLinuxHome=" + cacheLinuxHome + ", runLinuxHome=" + runLinuxHome + ", repositories=" + repositories + ", extensions=" + extensions + ", commandFactories=" + commandFactories + ", env=" + env + ", sdk=" + sdk + ", imports=" + imports + ", secure=" + secure + ", authenticationAgent=" + authenticationAgent + ", users=" + users + '}';
    }

}
