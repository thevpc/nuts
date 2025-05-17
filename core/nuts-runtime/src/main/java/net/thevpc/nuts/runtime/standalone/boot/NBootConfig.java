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
 * <p>
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
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NHomeLocation;
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceCurrentConfig;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NOsFamily;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nuts Boot editable configuration object
 *
 * @author thevpc
 * @since 0.5.4
 */
public final class NBootConfig implements Cloneable, Serializable {

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
    private NVersion apiVersion;

    /**
     * workspace runtime id (group, name with version)
     */
    private NId runtimeId;

    /**
     * runtime package dependencies id list (; separated)
     */
    private NDescriptor runtimeBootDescriptor;

    /**
     *
     */
    private List<NDescriptor> extensionBootDescriptors;

    /**
     * bootRepositories list (; separated) where to look for runtime dependencies
     */
    private List<String> bootRepositories;

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
    private NStoreStrategy storeStrategy;

    /**
     * workspace bootRepositories store location strategy
     */
    private NStoreStrategy repositoryStoreStrategy;

    /**
     * workspace store location layout
     */
    private NOsFamily storeLayout;

    /**
     * when global is true consider system wide folders (user independent but
     * needs greater privileges)
     */
    private boolean system;

    /**
     * workspace store locations
     */
    private Map<NStoreType, String> storeLocations;
    /**
     * workspace expected locations for all layout. Relevant when moving the
     * workspace cross operating systems
     */
    private Map<NHomeLocation, String> homeLocations;

    public NBootConfig() {
    }

    public NBootConfig(NWorkspaceOptions options) {
        if (options != null) {
            this.setWorkspace(options.getWorkspace().orNull());
            this.setName(options.getName().orNull());
            this.setStoreStrategy(options.getStoreStrategy().orNull());
            this.setRepositoryStoreStrategy(options.getRepositoryStoreStrategy().orNull());
            this.setStoreLayout(options.getStoreLayout().orNull());
            this.storeLocations = NCoreCollectionUtils.nonNullMap(options.getStoreLocations().orNull());
            this.homeLocations = NCoreCollectionUtils.nonNullMap(options.getHomeLocations().orNull());
            this.setRuntimeId(options.getRuntimeId().orNull());
            this.system = options.getSystem().orElse(false);
            this.runtimeId = options.getRuntimeId().orNull();
        }
    }

    public NBootConfig(DefaultNWorkspaceCurrentConfig context) {
        if (context != null) {
            this.name = context.getName();
            this.apiVersion = context.getApiVersion();
            this.runtimeId = context.getRuntimeId();
            this.runtimeBootDescriptor = context.getRuntimeBootDescriptor();
            this.extensionBootDescriptors = context.getExtensionBootDescriptors();
            this.bootRepositories = context.getBootRepositories();
            this.javaCommand = context.getJavaCommand();
            this.javaOptions = context.getJavaOptions();
            this.storeLocations = new LinkedHashMap<>(context.getStoreLocations());
            this.homeLocations = new LinkedHashMap<>(context.getHomeLocations());
            this.storeStrategy = context.getStoreStrategy();
            this.repositoryStoreStrategy = context.getRepositoryStoreStrategy();
            this.storeLayout = context.getStoreLayout();
            this.system = context.getSystem();
        }
    }

    public NBootConfig(NBootConfig other) {
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
            this.storeStrategy = other.getStoreStrategy();
            this.storeLayout = other.getStoreLayout();
            this.system = other.isSystem();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public String getApiId() {
        return NConstants.Ids.NUTS_API + "#" + apiVersion;
    }

    public NBootConfig setApiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public NId getRuntimeId() {
        return runtimeId;
    }

    public NBootConfig setRuntimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public NDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public NBootConfig setRuntimeBootDescriptor(NDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public List<NDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public NBootConfig setExtensionBootDescriptors(List<NDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NBootConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NBootConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public List<String> getBootRepositories() {
        return bootRepositories;
    }

    public NBootConfig setBootRepositories(List<String> repositories) {
        this.bootRepositories = repositories;
        return this;
    }

    public NBootConfig copy() {
        try {
            NBootConfig p = (NBootConfig) clone();
            p.storeLocations = p.storeLocations == null ? null : new LinkedHashMap<>(p.storeLocations);
            p.homeLocations = p.homeLocations == null ? null : new LinkedHashMap<>(p.homeLocations);

            return p;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public NStoreStrategy getStoreStrategy() {
        return storeStrategy;
    }

    public NBootConfig setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NBootConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public NOsFamily getStoreLayout() {
        return storeLayout;
    }

    //    public NutsBootConfig setStoreLocation(NutsStoreLocation folder, String value) {
//        this.storeLocations[folder.ordinal()] = value;
//        return this;
//    }
//
//    public String getStoreLocation(NutsStoreLocation folder) {
//        return this.storeLocations[folder.ordinal()];
//    }
    public void setStoreLocations(Map<NStoreType, String> storeLocations) {
        this.storeLocations = storeLocations;
    }

    public void setHomeLocations(Map<NHomeLocation, String> homeLocations) {
        this.homeLocations = homeLocations;
    }

    public NBootConfig setStoreLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    public NStoreStrategy getRepositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    public NBootConfig setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    public Map<NStoreType, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<NHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!NBlankable.isBlank(apiVersion)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (!NBlankable.isBlank(runtimeId)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId).append('\'');
        }
        if (runtimeBootDescriptor != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeDependencies=").append(runtimeBootDescriptor);
        }
        if (!NBlankable.isBlank(bootRepositories)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("bootRepositories='").append(bootRepositories).append('\'');
        }
        if (!NBlankable.isBlank(javaCommand)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!NBlankable.isBlank(javaOptions)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!NBlankable.isBlank(workspace)) {
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
        if (storeStrategy != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("storeStrategy=").append(storeStrategy);
        }
        if (repositoryStoreStrategy != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("repositoryStoreStrategy=").append(repositoryStoreStrategy);
        }
        if (storeLayout != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("storeLayout=").append(storeLayout);
        }
        if (system) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("global");
        }
        sb.append('}');
        return sb.toString();
    }
}
