/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class holds information gathered by nuts Boot and passed to Runtime on BootTime
 * @author thevpc
 * @app.category Internal
 */
final class PrivateNutsWorkspaceInitInformation implements NutsWorkspaceInitInformation {

    private NutsWorkspaceOptions options;
    /**
     * workspace api version
     */
    private String apiVersion;
    /**
     * workspace runtime id (group, name with version)
     */
    private NutsBootId runtimeId;
    private NutsBootDescriptor runtimeBootDescriptor;
    private NutsClassLoaderNode runtimeBootDependencyNode;
    private NutsBootDescriptor[] extensionBootDescriptors;
    private NutsClassLoaderNode[] extensionBootDependencyNodes;

    private NutsBootWorkspaceFactory bootWorkspaceFactory;

    private URL[] bootClassWorldURLs;

    private ClassLoader workspaceClassLoader;

    /**
     * workspace uuid
     */
    private String uuid;

    /**
     * workspace name
     */
    private String name;

    /**
     * workspace path
     */
    private String workspace;
//
//    /**
//     * runtime artifact dependencies id list (; separated)
//     */
//    private LinkedHashSet<String> runtimeDependenciesSet;
//
//    /**
//     *
//     */
//    private LinkedHashSet<String> extensionDependenciesSet;
//
    /**
     *
     */
    private LinkedHashSet<String> extensionsSet;

    /**
     * bootRepositories list (; separated) where to look for runtime
     * dependencies
     */
    private String bootRepositories;

    /**
     * java executable command to run nuts binaries
     */
    private String javaCommand;

    /**
     * java executable command options to run nuts binaries
     */
    private String javaOptions;

    /**
     * workspace store location strategy
     */
    private NutsStoreLocationStrategy storeLocationStrategy;

    /**
     * workspace bootRepositories store location strategy
     */
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;

    /**
     * workspace store location layout
     */
    private NutsOsFamily storeLocationLayout;

    /**
     * when global is true consider system wide folders (user independent but
     * needs greater privileges)
     */
    private boolean global;

    /**
     * workspace store locations
     */
    private Map<String, String> storeLocations;
    /**
     * workspace expected locations for all layout. Relevant when moving the
     * workspace cross operating systems
     */
    private Map<String, String> homeLocations;

    @Override
    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    public PrivateNutsWorkspaceInitInformation setOptions(NutsWorkspaceOptions options) {
        this.options = options;
        if (options != null) {
            this.setWorkspaceLocation(options.getWorkspace());
            this.setName(options.getName());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.storeLocations = new LinkedHashMap<>(options.getStoreLocations());
            this.homeLocations = new LinkedHashMap<>(options.getHomeLocations());
            this.setRuntimeId(options.getRuntimeId()==null?null:NutsBootId.parse(options.getRuntimeId()));
            this.global = options.isGlobal();
            this.javaCommand = options.getJavaCommand();
            this.javaOptions = options.getJavaOptions();
            this.apiVersion = NutsUtilStrings.trimToNull(options.getApiVersion());
        }
        return this;
    }

    public NutsBootDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public PrivateNutsWorkspaceInitInformation setRuntimeBootDescriptor(NutsBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public NutsBootDescriptor[] getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public PrivateNutsWorkspaceInitInformation setExtensionBootDescriptors(NutsBootDescriptor[] extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    public PrivateNutsWorkspaceInitInformation setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NutsBootId getRuntimeId() {
        return runtimeId;
    }

    public PrivateNutsWorkspaceInitInformation setRuntimeId(NutsBootId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

//    @Override
//    public String getRuntimeDependencies() {
//        return String.join(";", getRuntimeDependenciesSet());
//    }
//
//    @Override
//    public String getExtensionDependencies() {
//        return String.join(";", getExtensionDependenciesSet());
//    }
    @Override
    public String getBootRepositories() {
        return bootRepositories;
    }

    @Override
    public NutsBootWorkspaceFactory getBootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }

    public PrivateNutsWorkspaceInitInformation setBootWorkspaceFactory(NutsBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    @Override
    public URL[] getClassWorldURLs() {
        return bootClassWorldURLs;
    }

    public PrivateNutsWorkspaceInitInformation setBootClassWorldURLs(URL[] bootClassWorldURLs) {
        this.bootClassWorldURLs = bootClassWorldURLs;
        return this;
    }

    @Override
    public ClassLoader getClassWorldLoader() {
        return workspaceClassLoader;
    }

    public PrivateNutsWorkspaceInitInformation setWorkspaceClassLoader(ClassLoader workspaceClassLoader) {
        this.workspaceClassLoader = workspaceClassLoader;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getApiId() {
        return NutsConstants.Ids.NUTS_API + "#" + apiVersion;
    }

//    @Override
//    public Set<String> getRuntimeDependenciesSet() {
//        return runtimeDependenciesSet;
//    }
//
//    public PrivateNutsWorkspaceInitInformation setRuntimeDependenciesSet(Set<String> runtimeDependenciesSet) {
//        this.runtimeDependenciesSet = runtimeDependenciesSet == null ? null : new LinkedHashSet<>(runtimeDependenciesSet);
//        return this;
//    }
//    @Override
//    public Set<String> getExtensionDependenciesSet() {
//        return extensionDependenciesSet;
//    }
//
//    public PrivateNutsWorkspaceInitInformation setExtensionDependenciesSet(Set<String> extensionDependenciesSet) {
//        this.extensionDependenciesSet = extensionDependenciesSet == null ? null : new LinkedHashSet<>(extensionDependenciesSet);
//        return this;
//    }
    @Override
    public String getJavaCommand() {
        return javaCommand;
    }

    public PrivateNutsWorkspaceInitInformation setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public String getJavaOptions() {
        return javaOptions;
    }

    public PrivateNutsWorkspaceInitInformation setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public PrivateNutsWorkspaceInitInformation setBootRepositories(String repositories) {
        this.bootRepositories = repositories;
        return this;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public PrivateNutsWorkspaceInitInformation setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public PrivateNutsWorkspaceInitInformation setWorkspaceLocation(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public void setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
    }

    public void setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations = homeLocations;
    }

    public PrivateNutsWorkspaceInitInformation setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public PrivateNutsWorkspaceInitInformation setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getCacheBoot() {
        return getStoreLocation(NutsStoreLocation.CACHE) + File.separator + NutsConstants.Folders.ID;
    }

    public String getLib() {
        return getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation location) {
        Map<String, String> s = storeLocations;
        if (s != null) {
            return s.get(location.id());
        }
        return null;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<String, String> getHomeLocations() {
        return homeLocations;
    }

    public boolean isGlobal() {
        return global;
    }

    public Set<String> getExtensionsSet() {
        return extensionsSet;
    }

    public void setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = PrivateNutsUtils.copy(extensionsSet);
    }

    @Override
    public NutsClassLoaderNode getRuntimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }

    public PrivateNutsWorkspaceInitInformation setRuntimeBootDependencyNode(NutsClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    @Override
    public NutsClassLoaderNode[] getExtensionBootDependencyNodes() {
        return extensionBootDependencyNodes;
    }

    public PrivateNutsWorkspaceInitInformation setExtensionBootDependencyNodes(NutsClassLoaderNode[] extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = extensionBootDependencyNodes;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!NutsUtilStrings.isBlank(apiVersion)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (runtimeId!=null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId.toString()).append('\'');
        }
//        if (!runtimeDependenciesSet.isEmpty()) {
//            if (sb.length() > 0) {
//                sb.append(", ");
//            }
//            sb.append("runtimeDependencies='").append(runtimeDependenciesSet).append('\'');
//        }
        if (!NutsUtilStrings.isBlank(bootRepositories)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("bootRepositories='").append(bootRepositories).append('\'');
        }
        if (!NutsUtilStrings.isBlank(javaCommand)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!NutsUtilStrings.isBlank(javaOptions)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!NutsUtilStrings.isBlank(workspace)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("workspace='").append(workspace).append('\'');
        }
        if (storeLocations != null && !storeLocations.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("storeLocations=").append(storeLocations);

        }
        if (homeLocations != null && !homeLocations.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("homeLocations=").append(homeLocations);

        }
        if (storeLocationStrategy != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("storeLocationStrategy=").append(storeLocationStrategy);
        }
        if (repositoryStoreLocationStrategy != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("repositoryStoreLocationStrategy=").append(repositoryStoreLocationStrategy);
        }
        if (storeLocationLayout != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("storeLocationLayout=").append(storeLocationLayout);
        }
        if (global) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("global");
        }
        sb.append('}');
        return sb.toString();
    }

}
