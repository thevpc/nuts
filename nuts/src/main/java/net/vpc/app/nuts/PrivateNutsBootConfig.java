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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nuts Boot editable configuration object
 *
 * @author vpc
 * @since 0.5.4
 */
final class PrivateNutsBootConfig implements Cloneable, Serializable {

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

    /**
     * workspace api version
     */
    private String apiVersion;

    /**
     * workspace runtime id (group, name avec version)
     */
    private String runtimeId;

    /**
     * runtime component dependencies id list (; separated)
     */
    private String runtimeDependencies;

    /**
     *
     */
    private String extensionDependencies;

    /**
     * repositories list (; separated) where to look for runtime dependencies
     */
    private String repositories;

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
     * workspace repositories store location strategy
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
     * when true enable Desktop GUI components if available
     */
    private boolean gui;

    /**
     * workspace store locations
     */
    private Map<String,String> storeLocations;
    /**
     * workspace expected locations for all layout. Relevant when moving the
     * workspace cross operating systems
     */
    private Map<String,String> homeLocations;

    public PrivateNutsBootConfig() {
    }

    public PrivateNutsBootConfig(NutsWorkspaceOptions options) {
        if (options != null) {
            this.setWorkspace(options.getWorkspace());
            this.setName(options.getName());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.storeLocations = new LinkedHashMap<>(options.getStoreLocations());
            this.homeLocations = new LinkedHashMap<>(options.getHomeLocations());
            this.setRuntimeId(options.getBootRuntime());
//            this.setRuntimeDependencies(options.getBootRuntimeDependencies());
//            this.setRepositories(options.getRepositories());
            this.global = options.isGlobal();
            this.gui = options.isGui();
            this.runtimeId = options.getBootRuntime();
        }
    }

    public PrivateNutsBootConfig(PrivateNutsBootConfig other) {
        if (other != null) {
            this.uuid = other.getUuid();
            this.name = other.getName();
            this.apiVersion = other.getApiVersion();
            this.runtimeId = other.getRuntimeId();
            this.runtimeDependencies = other.getRuntimeDependencies();
            this.extensionDependencies = other.getExtensionDependencies();
            this.repositories = other.getRepositories();
            this.javaCommand = other.getJavaCommand();
            this.javaOptions = other.getJavaOptions();
            this.storeLocations = other.storeLocations==null?null:new LinkedHashMap<>(other.storeLocations);
            this.homeLocations = other.homeLocations==null?null:new LinkedHashMap<>(other.homeLocations);
            this.storeLocationStrategy = other.getStoreLocationStrategy();
            this.storeLocationLayout = other.getStoreLocationLayout();
            this.global = other.isGlobal();
            this.gui = other.isGui();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiId() {
        return NutsConstants.Ids.NUTS_API + "#" + apiVersion;
    }

    public PrivateNutsBootConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public PrivateNutsBootConfig setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public PrivateNutsBootConfig setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getExtensionDependencies() {
        return extensionDependencies;
    }

    public PrivateNutsBootConfig setExtensionDependencies(String extensionDependencies) {
        this.extensionDependencies = extensionDependencies;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public PrivateNutsBootConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public PrivateNutsBootConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getRepositories() {
        return repositories;
    }

    public PrivateNutsBootConfig setRepositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public PrivateNutsBootConfig copy() {
        try {
            PrivateNutsBootConfig p = (PrivateNutsBootConfig) clone();
            p.storeLocations = p.storeLocations == null ? null : new LinkedHashMap<>(p.storeLocations);
            p.homeLocations = p.homeLocations == null ? null : new LinkedHashMap<>(p.homeLocations);

            return p;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnexpectedException(null);
        }
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public PrivateNutsBootConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public PrivateNutsBootConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

//    public PrivateNutsBootConfig setStoreLocation(NutsStoreLocation folder, String value) {
//        this.storeLocations[folder.ordinal()] = value;
//        return this;
//    }
//
//    public String getStoreLocation(NutsStoreLocation folder) {
//        return this.storeLocations[folder.ordinal()];
//    }

    public void setStoreLocations(Map<String,String> storeLocations) {
        this.storeLocations = storeLocations;
    }

    public void setHomeLocations(Map<String,String> homeLocations) {
        this.homeLocations = homeLocations;
    }


//    public PrivateNutsBootConfig setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String value) {
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

    public PrivateNutsBootConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public PrivateNutsBootConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public Map<String,String> getStoreLocations() {
        return storeLocations;
    }

    public Map<String,String> getHomeLocations() {
        return homeLocations;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean isGui() {
        return gui;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!PrivateNutsUtils.isBlank(apiVersion)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(runtimeId)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(runtimeDependencies)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeDependencies='").append(runtimeDependencies).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(repositories)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("repositories='").append(repositories).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(javaCommand)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(javaOptions)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!PrivateNutsUtils.isBlank(workspace)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("workspace='").append(workspace).append('\'');
        }
        if(storeLocations!=null && !storeLocations.isEmpty()){
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("storeLocations=").append(storeLocations);
            
        }
        if(homeLocations!=null && !homeLocations.isEmpty()){
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
