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
    private String name = null;
    private String workspace = null;
    private String apiVersion = null;

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
    private String runtimeId = null;

    /**
     * ';' separated list of component Ids in long format (as defined in
     * {@link NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These components should be accessible
     * from {@link NutsWorkspaceConfig#getBootRepositories()}
     *
     * @see NutsId#getLongNameId()
     */
    private String runtimeDependencies = null;
    private String bootRepositories = null;
    private String javaCommand = null;
    private String javaOptions = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file eventhough no json library is available
    // via simple regexp
    private Map<String, String> storeLocations = null;
    private Map<String, String> homeLocations = null;

    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsOsFamily storeLocationLayout = null;

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

    public String getName() {
        return name;
    }

    public NutsWorkspaceConfig setName(String name) {
        this.name = name;
        return this;
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

    public String getApiVersion() {
        return apiVersion;
    }

    public NutsWorkspaceConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getCreateApiVersion() {
        return createApiVersion;
    }

    public NutsWorkspaceConfig setCreateApiVersion(String createApiVersion) {
        this.createApiVersion = createApiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsWorkspaceConfig setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NutsWorkspaceConfig setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsWorkspaceConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsWorkspaceConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsWorkspaceConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public List<NutsCommandAliasFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

    public NutsWorkspaceConfig setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<String, String> getHomeLocations() {
        return homeLocations;
    }

    public NutsWorkspaceConfig setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations = homeLocations;
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

}
