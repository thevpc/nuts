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
package net.vpc.app.nuts.core.impl.def.config;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.NutsStoreLocationStrategy;

import java.io.Serializable;
import java.util.*;

/**
 * @author vpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfigBoot implements Serializable {

    private static final long serialVersionUID = 507;
    /**
     * Api version having created the config
     */
    private String configVersion = null;

    private String uuid = null;
    private boolean global;
    private String name = null;
    private String workspace = null;
    private String bootRepositories = null;

    // folder types and layout types are exploded so that it is easier
    // to extract from json file even though no json library is available
    // via simple regexp
    private Map<String, String> storeLocations = null;
    private Map<String, String> homeLocations = null;

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


    public String getConfigVersion() {
        return configVersion;
    }

    public NutsWorkspaceConfigBoot setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsWorkspaceConfigBoot setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public NutsWorkspaceConfigBoot setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public Map<String, String> getHomeLocations() {
        return homeLocations;
    }

    public NutsWorkspaceConfigBoot setHomeLocations(Map<String, String> homeLocations) {
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

    public static class ExtensionConfig {
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
