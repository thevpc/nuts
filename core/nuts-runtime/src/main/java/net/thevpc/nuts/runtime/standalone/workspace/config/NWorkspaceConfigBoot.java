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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NHomeLocation;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.NStoreType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 * @since 0.5.4
 */
public final class NWorkspaceConfigBoot extends NConfigItem implements Cloneable {

    private static final long serialVersionUID = 830;
    private String uuid = null;
    private boolean system;
    private String name = null;
    private String workspace = null;
    private List<String> bootRepositories = null;

    // folder types and layout types are exploded so that It's easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private Map<NStoreType, String> storeLocations = null;
    private Map<NHomeLocation, String> homeLocations = null;

    private NStoreStrategy repositoryStoreStrategy = null;
    private NStoreStrategy storeStrategy = null;
    private NOsFamily storeLayout = null;

    private List<ExtensionConfig> extensions;

    public NWorkspaceConfigBoot() {
    }

    public String getName() {
        return name;
    }

    public NWorkspaceConfigBoot setName(String name) {
        this.name = name;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NWorkspaceConfigBoot setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }


    public List<ExtensionConfig> getExtensions() {
        return extensions;
    }

    public NWorkspaceConfigBoot setExtensions(List<ExtensionConfig> extensions) {
        this.extensions = extensions;
        return this;
    }


    public List<String> getBootRepositories() {
        return bootRepositories;
    }

    public NWorkspaceConfigBoot setBootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public Map<NStoreType, String> getStoreLocations() {
        return storeLocations;
    }

    public NWorkspaceConfigBoot setStoreLocations(Map<NStoreType, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<NHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }

    public NWorkspaceConfigBoot setHomeLocations(Map<NHomeLocation, String> homeLocations) {
        this.homeLocations = homeLocations;
        return this;
    }

    public NStoreStrategy getStoreStrategy() {
        return storeStrategy;
    }

    public NWorkspaceConfigBoot setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public NOsFamily getStoreLayout() {
        return storeLayout;
    }

    public NWorkspaceConfigBoot setStoreLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    public NStoreStrategy getRepositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    public NWorkspaceConfigBoot setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NWorkspaceConfigBoot setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isSystem() {
        return system;
    }

    public NWorkspaceConfigBoot setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public NWorkspaceConfigBoot copy() {
        try {
            NWorkspaceConfigBoot cloned = (NWorkspaceConfigBoot) clone();
            if (cloned.storeLocations != null) {
                cloned.storeLocations = new LinkedHashMap<>(cloned.storeLocations);
            }
            if (cloned.homeLocations != null) {
                cloned.homeLocations = new LinkedHashMap<>(cloned.homeLocations);
            }
            if (cloned.extensions != null) {
                cloned.extensions = extensions.stream().map(x -> x.copy()).collect(Collectors.toList());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ExtensionConfig extends NConfigItem implements Cloneable {
        private NId id;
        private boolean enabled;
        private String dependencies;

        public ExtensionConfig() {
        }

        public ExtensionConfig(NId id, String dependencies, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
            this.dependencies = dependencies;
        }

        public String getDependencies() {
            return dependencies;
        }

        public void setDependencies(String dependencies) {
            this.dependencies = dependencies;
        }

        public NId getId() {
            return id;
        }

        public void setId(NId id) {
            this.id = id;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, dependencies, enabled);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExtensionConfig that = (ExtensionConfig) o;
            return enabled == that.enabled
                    && Objects.equals(id, that.id)
                    && Objects.equals(dependencies, that.dependencies)
                    ;
        }

        public ExtensionConfig copy() {
            try {
                return (ExtensionConfig) clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
