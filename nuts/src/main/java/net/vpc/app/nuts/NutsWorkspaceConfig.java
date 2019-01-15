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
import java.util.*;

public final class NutsWorkspaceConfig implements Serializable {

    private static final long serialVersionUID = 1;
    private String workspace = null;
    private String bootApiVersion = null;
    private String bootRuntime = null;
    private String bootRuntimeDependencies = null;
    private String bootRepositories = null;
    private String bootJavaCommand = null;
    private String bootJavaOptions = null;
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    /**
     * valid values are "", "bundle"
     */
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsStoreLocationLayout storeLocationLayout = null;

    private final Map<String, NutsRepositoryLocation> repositories = new LinkedHashMap<>();
    private List<NutsId> extensions = new ArrayList<>();
    private List<NutsWorkspaceCommandFactoryConfig> commandFactories = new ArrayList<>();
    private Properties env = new Properties();
    private Map<String, List<NutsSdkLocation>> sdk = new HashMap<>();
    private String[] imports = new String[0];
    private boolean secure = false;
    private Map<String, NutsUserConfig> security = new HashMap<>();

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
        this.bootJavaCommand = other.getBootJavaCommand();
        this.bootJavaOptions = other.getBootJavaOptions();
        for (NutsRepositoryLocation repository : other.getRepositories()) {
            this.repositories.put(repository.getId(), repository);
        }
        for (NutsUserConfig repository : other.getSecurity()) {
            this.security.put(repository.getUser(), repository);
        }
        for (Map.Entry<String, List<NutsSdkLocation>> e : other.getSdk().entrySet()) {
            List<NutsSdkLocation> value = e.getValue();
            this.sdk.put(e.getKey(), value == null ? new ArrayList<>() : new ArrayList<>(value));
        }
        this.extensions.addAll(Arrays.asList(other.getExtensions()));
        this.env.putAll(other.getEnv());
        this.imports = other.getImports();
    }

    public String getWorkspace() {
        return workspace;
    }


    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }


    public NutsRepositoryLocation[] getRepositories() {
        return repositories.values().toArray(new NutsRepositoryLocation[0]);
    }


    public String[] getImports() {
        return imports;
    }


    public void setImports(String[] imports) {
        this.imports = imports;
    }


    public NutsRepositoryLocation getRepository(String repositoryId) {
        return this.repositories.get(repositoryId);
    }


    public void addRepository(NutsRepositoryLocation repository) {
        this.repositories.put(repository.getId(), repository);
    }


    public void removeRepository(String repositoryId) {
        this.repositories.remove(repositoryId);
    }


    public boolean containsRepository(String repositoryId) {
        return repositories.containsKey(repositoryId);
    }


    public void addExtension(NutsId extensionId) {
        this.extensions.add(extensionId);
    }


    public void removeExtension(NutsId extensionId) {
        extensions.remove(extensionId);
    }

    public void updateExtensionAt(int index, NutsId extensionId) {
        extensions.set(index, extensionId);
    }


    public NutsId[] getExtensions() {
        return extensions.toArray(new NutsId[0]);
    }


    public Properties getEnv() {
        return env;
    }


    public void setEnv(Properties env) {
        this.env = env;
    }


    public void removeSecurity(String securityId) {
        security.remove(securityId);
    }


    public void setSecurity(NutsUserConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }


    public NutsUserConfig getSecurity(String id) {
        return security.get(id);
    }


    public NutsUserConfig[] getSecurity() {
        return security.values().toArray(new NutsUserConfig[0]);
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return sdk == null ? new HashMap<>() : sdk;
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

    public NutsWorkspaceConfig setCommandFactories(List<NutsWorkspaceCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
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
}
