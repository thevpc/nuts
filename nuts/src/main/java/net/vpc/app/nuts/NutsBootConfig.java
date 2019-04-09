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

import java.io.File;
import java.util.Arrays;

public final class NutsBootConfig implements Cloneable {

    private String apiVersion = null;
    private String runtimeId = null;
    private String runtimeDependencies = null;
    private String repositories = null;
    private String javaCommand = null;
    private String javaOptions = null;
    private String workspace = null;
    private String[] storeLocations = new String[NutsStoreLocation.values().length];
    private String[] homeLocations = new String[NutsStoreLocation.values().length * NutsStoreLocationLayout.values().length];
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationLayout storeLocationLayout = null;
    private boolean global = false;

    public NutsBootConfig() {
    }

    public NutsBootConfig(NutsWorkspaceOptions options) {
        if (options != null) {
            this.setWorkspace(options.getWorkspace());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.storeLocations = options.getStoreLocations();
            this.homeLocations = options.getHomeLocations();
            this.global = options.isGlobal();
        }
    }

    public NutsBootConfig(NutsBootContext context) {
        if (context != null) {
            this.apiVersion = context.getApiId().getVersion().getValue();
            this.runtimeId = context.getRuntimeId().toString();
            this.runtimeDependencies = context.getRuntimeDependencies();
            this.repositories = context.getRepositories();
            this.javaCommand = context.getJavaCommand();
            this.javaOptions = context.getJavaOptions();
            this.storeLocations = context.getStoreLocations();
            this.homeLocations = context.getHomeLocations();
            this.storeLocationStrategy = context.getStoreLocationStrategy();
            this.repositoryStoreLocationStrategy = context.getRepositoryStoreLocationStrategy();
            this.storeLocationLayout = context.getStoreLocationLayout();
        }
    }

    public NutsBootConfig(NutsBootConfig other) {
        if (other != null) {
            this.apiVersion = other.getApiVersion();
            this.runtimeId = other.getRuntimeId();
            this.runtimeDependencies = other.getRuntimeDependencies();
            this.repositories = other.getRepositories();
            this.javaCommand = other.getJavaCommand();
            this.javaOptions = other.getJavaOptions();
            this.storeLocations = Arrays.copyOf(other.storeLocations, other.storeLocations.length);
            this.homeLocations = Arrays.copyOf(other.homeLocations, other.homeLocations.length);
            this.storeLocationStrategy = other.getStoreLocationStrategy();
            this.storeLocationLayout = other.getStoreLocationLayout();
            this.global = other.isGlobal();
        }
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

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NutsBootConfig setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
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

    public String getRepositories() {
        return repositories;
    }

    public NutsBootConfig setRepositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public NutsBootConfig copy() {
        try {
            return (NutsBootConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new NutsUnexpectedException();
        }
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsBootConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getBootsrap() {
        return getWorkspace()+File.separator+NutsConstants.Folders.BOOT;
    }
    
    public String getWorkspace() {
        return workspace;
    }

    public NutsBootConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsBootConfig setStoreLocation(NutsStoreLocation folder, String value) {
        this.storeLocations[folder.ordinal()] = value;
        return this;
    }

    public String getStoreLocation(NutsStoreLocation folder) {
        return this.storeLocations[folder.ordinal()];
    }

    public NutsBootConfig setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folder, String value) {
        this.homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()] = value;
        return this;
    }

    public String getHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folder) {
        return this.homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folder.ordinal()];
    }

    public NutsBootConfig setStoreLocationLayout(NutsStoreLocationLayout storeLocationLayout) {
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

    public String[] getStoreLocations() {
        return Arrays.copyOf(storeLocations, storeLocations.length);
    }

    public String[] getHomeLocations() {
        return Arrays.copyOf(homeLocations, homeLocations.length);
    }

    public boolean isGlobal() {
        return global;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!NutsUtils.isBlank(apiVersion)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (!NutsUtils.isBlank(runtimeId)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId).append('\'');
        }
        if (!NutsUtils.isBlank(runtimeDependencies)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("runtimeDependencies='").append(runtimeDependencies).append('\'');
        }
        if (!NutsUtils.isBlank(repositories)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("repositories='").append(repositories).append('\'');
        }
        if (!NutsUtils.isBlank(javaCommand)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!NutsUtils.isBlank(javaOptions)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!NutsUtils.isBlank(workspace)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("workspace='").append(workspace).append('\'');
        }
        for (int i = 0; i < NutsStoreLocation.values().length; i++) {
            String s = getStoreLocation(NutsStoreLocation.values()[i]);
            if (!NutsUtils.isBlank(s)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(NutsStoreLocation.values()[i].name().toLowerCase()).append("StoreLocation='").append(s).append('\'');
            }
        }
        for (int i = 0; i < NutsStoreLocationLayout.values().length; i++) {
            for (int j = 0; j < NutsStoreLocation.values().length; j++) {
                String s = getHomeLocation(NutsStoreLocationLayout.values()[i], NutsStoreLocation.values()[j]);
                if (!NutsUtils.isBlank(s)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(NutsStoreLocation.values()[j].name().toLowerCase())
                            .append(NutsUtils.capiltalize(NutsStoreLocationLayout.values()[i].name().toLowerCase()))
                            .append("Home='").append(s).append('\'');
                }
            }
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
