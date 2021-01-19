/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.config.compat.v502;

import java.io.Serializable;
import java.util.*;
import net.thevpc.nuts.NutsCommandAliasFactoryConfig;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsRepositoryRef;
import net.thevpc.nuts.NutsSdkLocation;
import net.thevpc.nuts.NutsStoreLocationStrategy;
import net.thevpc.nuts.NutsUserConfig;
import net.thevpc.nuts.runtime.standalone.config.NutsWorkspaceConfigBoot;

/**
 *
 * @author thevpc
 * @since 0.5.6
 */
public final class NutsWorkspaceConfigBoot502 implements Serializable {

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
     * from {@link NutsWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NutsId#getLongNameId()
     */
    private String bootRuntimeDependencies = null;
    private String bootRepositories = null;
    private String bootJavaCommand = null;
    private String bootJavaOptions = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private String storeLocationLayout = null;

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

    private String programsMacOsHome = null;
    private String configMacOsHome = null;
    private String varMacOsHome = null;
    private String libMacOsHome = null;
    private String logsMacOsHome = null;
    private String tempMacOsHome = null;
    private String cacheMacOsHome = null;

    private String programsLinuxHome = null;
    private String configLinuxHome = null;
    private String varLinuxHome = null;
    private String libLinuxHome = null;
    private String logsLinuxHome = null;
    private String tempLinuxHome = null;
    private String cacheLinuxHome = null;

    private List<NutsRepositoryRef> repositories;
    private List<NutsId> extensions;
    private List<NutsCommandAliasFactoryConfig> commandFactories;
    private Properties env = new Properties();
    private List<NutsSdkLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private boolean secure = false;
    private String authenticationAgent;
    private List<NutsUserConfig> users = new ArrayList<>();

    public NutsWorkspaceConfigBoot502() {
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public List<NutsRepositoryRef> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<NutsRepositoryRef> repositories) {
        this.repositories = repositories;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;

    }

    public List<NutsId> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<NutsId> extensions) {
        this.extensions = extensions;

    }

    public void setCommandFactories(List<NutsCommandAliasFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;

    }

    public void setSdk(List<NutsSdkLocation> sdk) {
        this.sdk = sdk;

    }

    public Properties getEnv() {
        return env;
    }

    public void setEnv(Properties env) {
        this.env = env;

    }

    public void setUsers(List<NutsUserConfig> users) {
        this.users = users;

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

    public void setSecure(boolean secure) {
        this.secure = secure;

    }

    public String getBootApiVersion() {
        return bootApiVersion;
    }

    public void setBootApiVersion(String bootApiVersion) {
        this.bootApiVersion = bootApiVersion;

    }

    public String getCreateApiVersion() {
        return createApiVersion;
    }

    public void setCreateApiVersion(String createApiVersion) {
        this.createApiVersion = createApiVersion;

    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public void setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;

    }

    public String getBootRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    public void setBootRuntimeDependencies(String bootRuntimeDependencies) {
        this.bootRuntimeDependencies = bootRuntimeDependencies;

    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public void setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;

    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public void setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;

    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public void setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;

    }

    public List<NutsCommandAliasFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public void setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;

    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public void setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;

    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public void setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;

    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public void setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;

    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public void setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;

    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public void setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;

    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public void setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;

    }

    public String getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public void setStoreLocationLayout(String storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;

    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public void setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;

    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public void setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;

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

    public String getProgramsMacOsHome() {
        return programsMacOsHome;
    }

    public void setProgramsMacOsHome(String programsMacOsHome) {
        this.programsMacOsHome = programsMacOsHome;

    }

    public String getConfigMacOsHome() {
        return configMacOsHome;
    }

    public void setConfigMacOsHome(String configMacOsHome) {
        this.configMacOsHome = configMacOsHome;

    }

    public String getVarMacOsHome() {
        return varMacOsHome;
    }

    public void setVarMacOsHome(String varMacOsHome) {
        this.varMacOsHome = varMacOsHome;

    }

    public String getLibMacOsHome() {
        return libMacOsHome;
    }

    public void setLibMacOsHome(String libMacOsHome) {
        this.libMacOsHome = libMacOsHome;

    }

    public String getLogsMacOsHome() {
        return logsMacOsHome;
    }

    public void setLogsMacOsHome(String logsMacOsHome) {
        this.logsMacOsHome = logsMacOsHome;

    }

    public String getTempMacOsHome() {
        return tempMacOsHome;
    }

    public void setTempMacOsHome(String tempMacOsHome) {
        this.tempMacOsHome = tempMacOsHome;

    }

    public String getCacheMacOsHome() {
        return cacheMacOsHome;
    }

    public void setCacheMacOsHome(String cacheMacOsHome) {
        this.cacheMacOsHome = cacheMacOsHome;

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

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public void setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
    }

    public NutsWorkspaceConfigBoot toWorkspaceConfig() {
        NutsWorkspaceConfigBoot c = new NutsWorkspaceConfigBoot();
        c.setConfigVersion(createApiVersion);
        c.setBootRepositories(bootRepositories);
//         c.setConfigVersion(createApiVersion);
        Map<String, String> storeLocations = new LinkedHashMap<>();
        storeLocations.put("apps", programsStoreLocation);
        storeLocations.put("config", configStoreLocation);
        storeLocations.put("var", varStoreLocation);
        storeLocations.put("lib", libStoreLocation);
        storeLocations.put("log", logsStoreLocation);
        storeLocations.put("temp", tempStoreLocation);
        storeLocations.put("cache", cacheStoreLocation);

        Map<String, String> homeLocations = new LinkedHashMap<>();
        homeLocations.put("system:apps", programsSystemHome);
        homeLocations.put("system:config", configSystemHome);
        homeLocations.put("system:var", varSystemHome);
        homeLocations.put("system:lib", libSystemHome);
        homeLocations.put("system:log", logsSystemHome);
        homeLocations.put("system:temp", tempSystemHome);
        homeLocations.put("system:cache", cacheSystemHome);

        homeLocations.put("windows:apps", programsWindowsHome);
        homeLocations.put("windows:config", configWindowsHome);
        homeLocations.put("windows:var", varWindowsHome);
        homeLocations.put("windows:lib", libWindowsHome);
        homeLocations.put("windows:log", logsWindowsHome);
        homeLocations.put("windows:temp", tempWindowsHome);
        homeLocations.put("windows:cache", cacheWindowsHome);

        homeLocations.put("linux:apps", programsWindowsHome);
        homeLocations.put("linux:config", configWindowsHome);
        homeLocations.put("linux:var", varWindowsHome);
        homeLocations.put("linux:lib", libWindowsHome);
        homeLocations.put("linux:log", logsWindowsHome);
        homeLocations.put("linux:temp", tempWindowsHome);
        homeLocations.put("linux:cache", cacheWindowsHome);

        c.setGlobal(global);
        //there is no extensions in 0.5.2
//        c.setExtensions(extensions);
        c.setStoreLocationLayout("windows".equalsIgnoreCase(storeLocationLayout) ? NutsOsFamily.WINDOWS
                : "linux".equals(storeLocationLayout) ? NutsOsFamily.LINUX : null);
        c.setStoreLocationStrategy(storeLocationStrategy);
        c.setUuid(uuid);
        c.setWorkspace(workspace);
        return c;
    }
}
