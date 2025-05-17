/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigBoot;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * @author thevpc
 * @since 0.5.4
 */
public final class NWorkspaceConfigBoot506 extends NConfigItem {

    private static final long serialVersionUID = 3;
    private String uuid = null;
    private boolean global;
    private String name = null;
    private String workspace = null;
    private NVersion apiVersion = null;

    /**
     * boot package Id in long format (as defined in
     * {@link NId#getLongName()})
     *
     * @see NId#getLongId()
     */
    private NId runtimeId = null;

    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These packages should be accessible
     * from {@link NWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NId#getLongId()
     */
    private String runtimeDependencies = null;
    private String bootRepositories = null;
    private String javaCommand = null;
    private String javaOptions = null;

    // folder types and layout types are exploded so that It's easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private Map<NStoreType, String> storeLocations = null;
    private Map<NHomeLocation, String> homeLocations = null;

    private NStoreStrategy repositoryStoreLocationStrategy = null;
    private NStoreStrategy storeLocationStrategy = null;
    private NOsFamily storeLocationLayout = null;

    private List<NRepositoryRef> repositories;
    private List<NId> extensions;
    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid extensions. These packages should be either cached in boot cache
     * folder or accessible from
     * {@link NWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NId#getLongId()
     */
    private String extensionDependencies = null;
    private List<NCommandFactoryConfig> commandFactories;
    private Properties env = new Properties();
    private List<NPlatformLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private boolean secure = false;
    private String authenticationAgent;
    private List<NUserConfig> users = new ArrayList<>();

    public NWorkspaceConfigBoot506() {
    }

    public String getName() {
        return name;
    }

    public NWorkspaceConfigBoot506 setName(String name) {
        this.name = name;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NWorkspaceConfigBoot506 setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public List<NRepositoryRef> getRepositories() {
        return repositories;
    }

    public NWorkspaceConfigBoot506 setRepositories(List<NRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NWorkspaceConfigBoot506 setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public List<NId> getExtensions() {
        return extensions;
    }

    public NWorkspaceConfigBoot506 setExtensions(List<NId> extensions) {
        this.extensions = extensions;
        return this;
    }

    public NWorkspaceConfigBoot506 setCommandFactories(List<NCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NWorkspaceConfigBoot506 setSdk(List<NPlatformLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Properties getEnv() {
        return env;
    }

    public NWorkspaceConfigBoot506 setEnv(Properties env) {
        this.env = env;
        return this;
    }

    public NWorkspaceConfigBoot506 setUsers(List<NUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NUserConfig> getUsers() {
        return users;
    }

    public List<NPlatformLocation> getSdk() {
        return sdk;
    }

    public boolean isSecure() {
        return secure;
    }

    public NWorkspaceConfigBoot506 setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public NWorkspaceConfigBoot506 setApiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public NId getRuntimeId() {
        return runtimeId;
    }

    public NWorkspaceConfigBoot506 setRuntimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NWorkspaceConfigBoot506 setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getExtensionDependencies() {
        return extensionDependencies;
    }

    public NWorkspaceConfigBoot506 setExtensionDependencies(String runtimeDependencies) {
        this.extensionDependencies = runtimeDependencies;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NWorkspaceConfigBoot506 setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NWorkspaceConfigBoot506 setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NWorkspaceConfigBoot506 setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public List<NCommandFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

    public NWorkspaceConfigBoot506 setStoreLocations(Map<NStoreType, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<NStoreType, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<NHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }

    public NWorkspaceConfigBoot506 setHomeLocations(Map<NHomeLocation, String> homeLocations) {
        this.homeLocations = homeLocations;
        return this;
    }

    public NStoreStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NWorkspaceConfigBoot506 setStoreLocationStrategy(NStoreStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NWorkspaceConfigBoot506 setStoreLocationLayout(NOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NStoreStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NWorkspaceConfigBoot506 setRepositoryStoreLocationStrategy(NStoreStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NWorkspaceConfigBoot506 setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public NWorkspaceConfigBoot506 setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NWorkspaceConfigBoot506 setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public NWorkspaceConfigBoot toWorkspaceConfig() {
        NWorkspaceConfigBoot c = new NWorkspaceConfigBoot();
        c.setUuid(this.getUuid());
        c.setSystem(this.isGlobal());
        c.setName(this.getName());
        c.setWorkspace(this.getWorkspace());
        c.setConfigVersion(this.getConfigVersion());
        c.setBootRepositories(NStringUtils.split(getBootRepositories(),";,\n"));
        c.setStoreLocations(this.getStoreLocations() == null ? null : new LinkedHashMap<>(this.getStoreLocations()));
        c.setHomeLocations(this.getHomeLocations() == null ? null : new LinkedHashMap<>(this.getHomeLocations()));
        c.setRepositoryStoreStrategy(this.getRepositoryStoreLocationStrategy());
        c.setStoreStrategy(this.getStoreLocationStrategy());
        c.setStoreLayout(this.getStoreLocationLayout());
        //there is no extensions in 0.5.6
        return c;
    }


}
