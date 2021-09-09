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
package net.thevpc.nuts.runtime.standalone.config.compat.v506;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.NutsWorkspaceConfigBoot;

import java.util.*;

/**
 * @author thevpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfigBoot506 extends NutsConfigItem {

    private static final long serialVersionUID = 3;
    private String uuid = null;
    private boolean global;
    private String name = null;
    private String workspace = null;
    private String apiVersion = null;

    /**
     * boot package Id in long format (as defined in
     * {@link NutsId#getLongName()})
     *
     * @see NutsId#getLongNameId()
     */
    private String runtimeId = null;

    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These packages should be accessible
     * from {@link NutsWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NutsId#getLongNameId()
     */
    private String runtimeDependencies = null;
    private String bootRepositories = null;
    private String javaCommand = null;
    private String javaOptions = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private Map<String, String> storeLocations = null;
    private Map<String, String> homeLocations = null;

    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsOsFamily storeLocationLayout = null;

    private List<NutsRepositoryRef> repositories;
    private List<NutsId> extensions;
    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid extensions. These packages should be either cached in boot cache
     * folder or accessible from
     * {@link NutsWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NutsId#getLongNameId()
     */
    private String extensionDependencies = null;
    private List<NutsCommandFactoryConfig> commandFactories;
    private Properties env = new Properties();
    private List<NutsPlatformLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private boolean secure = false;
    private String authenticationAgent;
    private List<NutsUserConfig> users = new ArrayList<>();

    public NutsWorkspaceConfigBoot506() {
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceConfigBoot506 setName(String name) {
        this.name = name;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceConfigBoot506 setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public List<NutsRepositoryRef> getRepositories() {
        return repositories;
    }

    public NutsWorkspaceConfigBoot506 setRepositories(List<NutsRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NutsWorkspaceConfigBoot506 setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public List<NutsId> getExtensions() {
        return extensions;
    }

    public NutsWorkspaceConfigBoot506 setExtensions(List<NutsId> extensions) {
        this.extensions = extensions;
        return this;
    }

    public NutsWorkspaceConfigBoot506 setCommandFactories(List<NutsCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NutsWorkspaceConfigBoot506 setSdk(List<NutsPlatformLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Properties getEnv() {
        return env;
    }

    public NutsWorkspaceConfigBoot506 setEnv(Properties env) {
        this.env = env;
        return this;
    }

    public NutsWorkspaceConfigBoot506 setUsers(List<NutsUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NutsUserConfig> getUsers() {
        return users;
    }

    public List<NutsPlatformLocation> getSdk() {
        return sdk;
    }

    public boolean isSecure() {
        return secure;
    }

    public NutsWorkspaceConfigBoot506 setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public NutsWorkspaceConfigBoot506 setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsWorkspaceConfigBoot506 setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NutsWorkspaceConfigBoot506 setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getExtensionDependencies() {
        return extensionDependencies;
    }

    public NutsWorkspaceConfigBoot506 setExtensionDependencies(String runtimeDependencies) {
        this.extensionDependencies = runtimeDependencies;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsWorkspaceConfigBoot506 setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsWorkspaceConfigBoot506 setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsWorkspaceConfigBoot506 setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public List<NutsCommandFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

    public NutsWorkspaceConfigBoot506 setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<String, String> getHomeLocations() {
        return homeLocations;
    }

    public NutsWorkspaceConfigBoot506 setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations = homeLocations;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsWorkspaceConfigBoot506 setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceConfigBoot506 setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsWorkspaceConfigBoot506 setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceConfigBoot506 setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public NutsWorkspaceConfigBoot506 setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsWorkspaceConfigBoot506 setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public NutsWorkspaceConfigBoot toWorkspaceConfig() {
        NutsWorkspaceConfigBoot c = new NutsWorkspaceConfigBoot();
        c.setUuid(this.getUuid());
        c.setGlobal(this.isGlobal());
        c.setName(this.getName());
        c.setWorkspace(this.getWorkspace());
        c.setConfigVersion(this.getConfigVersion());
        c.setBootRepositories(this.getBootRepositories());
        c.setStoreLocations(this.getStoreLocations() == null ? null : new LinkedHashMap<>(this.getStoreLocations()));
        c.setHomeLocations(this.getHomeLocations() == null ? null : new LinkedHashMap<>(this.getHomeLocations()));
        c.setRepositoryStoreLocationStrategy(this.getRepositoryStoreLocationStrategy());
        c.setStoreLocationStrategy(this.getStoreLocationStrategy());
        c.setStoreLocationLayout(this.getStoreLocationLayout());
        //there is no extensions in 0.5.6
        return c;
    }


}
