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
package net.thevpc.nuts.runtime.standalone.config;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsBootDescriptor;

/**
 * Nuts Boot editable configuration object
 *
 * @author thevpc
 * @since 0.5.4
 */
public final class NutsBootConfig implements Cloneable, Serializable {

    /**
     * workspace name
     */
    private String name;

    /**
     * workspace path
     */
    private String workspace;

    /**
     * workspace api version
     */
    private String apiVersion;

    /**
     * workspace runtime id (group, name with version)
     */
    private String runtimeId;

    /**
     * runtime package dependencies id list (; separated)
     */
    private NutsBootDescriptor runtimeBootDescriptor;

    /**
     *
     */
    private NutsBootDescriptor[] extensionBootDescriptors;

    /**
     * bootRepositories list (; separated) where to look for runtime dependencies
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
    private Map<NutsStoreLocation, String> storeLocations;
    /**
     * workspace expected locations for all layout. Relevant when moving the
     * workspace cross operating systems
     */
    private Map<NutsHomeLocation, String> homeLocations;

    public NutsBootConfig() {
    }

    public NutsBootConfig(NutsWorkspaceOptions options) {
        if (options != null) {
            this.setWorkspace(options.getWorkspace());
            this.setName(options.getName());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.storeLocations = new LinkedHashMap<>(options.getStoreLocations());
            this.homeLocations = new LinkedHashMap<>(options.getHomeLocations());
            this.setRuntimeId(options.getRuntimeId());
//            this.setRuntimeDependencies(options.getBootRuntimeDependencies());
//            this.setRepositories(options.getBootRepositories());
            this.global = options.isGlobal();
            this.runtimeId = options.getRuntimeId();
        }
    }

    public NutsBootConfig(DefaultNutsWorkspaceCurrentConfig context) {
        if (context != null) {
            this.name = context.getName();
            this.apiVersion = context.getApiVersion();
            this.runtimeId = context.getRuntimeId().getLongName();
            this.runtimeBootDescriptor = context.getRuntimeBootDescriptor();
            this.extensionBootDescriptors = context.getExtensionBootDescriptors();
            this.bootRepositories = context.getBootRepositories();
            this.javaCommand = context.getJavaCommand();
            this.javaOptions = context.getJavaOptions();
            this.storeLocations = new LinkedHashMap<>(context.getStoreLocations());
            this.homeLocations = new LinkedHashMap<>(context.getHomeLocations());
            this.storeLocationStrategy = context.getStoreLocationStrategy();
            this.repositoryStoreLocationStrategy = context.getRepositoryStoreLocationStrategy();
            this.storeLocationLayout = context.getStoreLocationLayout();
            this.global = context.isGlobal();
        }
    }

    public NutsBootConfig(NutsBootConfig other) {
        if (other != null) {
            this.name = other.getName();
            this.apiVersion = other.getApiVersion();
            this.runtimeId = other.getRuntimeId();
            this.runtimeBootDescriptor = other.getRuntimeBootDescriptor();
            this.extensionBootDescriptors = other.getExtensionBootDescriptors();
            this.bootRepositories = other.getBootRepositories();
            this.javaCommand = other.getJavaCommand();
            this.javaOptions = other.getJavaOptions();
            this.storeLocations = other.storeLocations == null ? null : new LinkedHashMap<>(other.storeLocations);
            this.homeLocations = other.homeLocations == null ? null : new LinkedHashMap<>(other.homeLocations);
            this.storeLocationStrategy = other.getStoreLocationStrategy();
            this.storeLocationLayout = other.getStoreLocationLayout();
            this.global = other.isGlobal();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiId() {
        return NutsConstants.Ids.NUTS_API + "#" + apiVersion;
    }

    public NutsBootConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootConfig setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public NutsBootDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public NutsBootConfig setRuntimeBootDescriptor(NutsBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public NutsBootDescriptor[] getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public NutsBootConfig setExtensionBootDescriptors(NutsBootDescriptor[] extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsBootConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsBootConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsBootConfig setRepositories(String repositories) {
        this.bootRepositories = repositories;
        return this;
    }

    public NutsBootConfig copy() {
        try {
            NutsBootConfig p = (NutsBootConfig) clone();
            p.storeLocations = p.storeLocations == null ? null : new LinkedHashMap<>(p.storeLocations);
            p.homeLocations = p.homeLocations == null ? null : new LinkedHashMap<>(p.homeLocations);

            return p;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsBootConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsBootConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

//    public NutsBootConfig setStoreLocation(NutsStoreLocation folder, String value) {
//        this.storeLocations[folder.ordinal()] = value;
//        return this;
//    }
//
//    public String getStoreLocation(NutsStoreLocation folder) {
//        return this.storeLocations[folder.ordinal()];
//    }
    public void setStoreLocations(Map<NutsStoreLocation, String> storeLocations) {
        this.storeLocations = storeLocations;
    }

    public void setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        this.homeLocations = homeLocations;
    }

//    public NutsBootConfig setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String value) {
//        if (layout == null) {
//            this.defaultHomeLocations[folder.ordinal()] = value;
//        } else {
//            this.homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()] = value;
//        }
//        return this;
//    }
//
//    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folder) {
//        if (layout == null) {
//            return this.defaultHomeLocations[folder.ordinal()];
//        } else {
//            return this.homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()];
//        }
//    }
    public NutsBootConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsBootConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public Map<NutsStoreLocation, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<NutsHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }

    public boolean isGlobal() {
        return global;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!NutsBlankable.isBlank(apiVersion)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (!NutsBlankable.isBlank(runtimeId)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId).append('\'');
        }
        if (runtimeBootDescriptor !=null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeDependencies=").append(runtimeBootDescriptor);
        }
        if (!NutsBlankable.isBlank(bootRepositories)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("repositories='").append(bootRepositories).append('\'');
        }
        if (!NutsBlankable.isBlank(javaCommand)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!NutsBlankable.isBlank(javaOptions)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!NutsBlankable.isBlank(workspace)) {
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
