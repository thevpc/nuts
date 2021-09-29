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

import net.thevpc.nuts.*;

import java.util.*;

/**
 * @author thevpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfigBoot extends NutsConfigItem {

    private static final long serialVersionUID = 600;
    private String uuid = null;
    private boolean global;
    private String name = null;
    private String workspace = null;
    private String bootRepositories = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private Map<NutsStoreLocation, String> storeLocations = null;
    private Map<NutsHomeLocation, String> homeLocations = null;

    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsOsFamily storeLocationLayout = null;

    private List<ExtensionConfig> extensions;

    public NutsWorkspaceConfigBoot() {
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceConfigBoot setName(String name) {
        this.name = name;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceConfigBoot setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }


    public List<ExtensionConfig> getExtensions() {
        return extensions;
    }

    public NutsWorkspaceConfigBoot setExtensions(List<ExtensionConfig> extensions) {
        this.extensions = extensions;
        return this;
    }


    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsWorkspaceConfigBoot setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public NutsWorkspaceConfigBoot setStoreLocations(Map<NutsStoreLocation, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<NutsStoreLocation, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<NutsHomeLocation, String> getHomeLocations() {
        return homeLocations;
    }

    public NutsWorkspaceConfigBoot setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        this.homeLocations = homeLocations;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsWorkspaceConfigBoot setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsWorkspaceConfigBoot setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsWorkspaceConfigBoot setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceConfigBoot setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public NutsWorkspaceConfigBoot setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public static class ExtensionConfig extends NutsConfigItem{
        private NutsId id;
        private boolean enabled;

        public ExtensionConfig() {
        }

        public ExtensionConfig(NutsId id, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
        }

        public NutsId getId() {
            return id;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExtensionConfig that = (ExtensionConfig) o;
            return enabled == that.enabled &&
                    Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, enabled);
        }
    }
}
