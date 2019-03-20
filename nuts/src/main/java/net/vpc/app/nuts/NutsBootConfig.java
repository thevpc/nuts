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

public final class NutsBootConfig implements Cloneable {
    private String apiVersion = null;
    private String runtimeId = null;
    private String runtimeDependencies = null;
    private String repositories = null;
    private String javaCommand = null;
    private String javaOptions = null;
    private String workspace = null;
    private String programsStoreLocation = null;
    private String libStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy = null;
    private NutsStoreLocationLayout storeLocationLayout = null;

    public NutsBootConfig() {
    }

    public NutsBootConfig(NutsWorkspaceOptions options) {
        if (options != null) {
            this.setWorkspace(options.getWorkspace());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.setProgramsStoreLocation(options.getProgramsStoreLocation());
            this.setLibStoreLocation(options.getLibStoreLocation());
            this.setLogsStoreLocation(options.getLogsStoreLocation());
            this.setVarStoreLocation(options.getVarStoreLocation());
            this.setCacheStoreLocation(options.getCacheStoreLocation());
            this.setTempStoreLocation(options.getTempStoreLocation());
            this.setConfigStoreLocation(options.getConfigStoreLocation());
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
            this.cacheStoreLocation = context.getStoreLocation(NutsStoreFolder.CACHE);
            this.programsStoreLocation = context.getStoreLocation(NutsStoreFolder.PROGRAMS);
            this.libStoreLocation = context.getStoreLocation(NutsStoreFolder.LIB);
            this.configStoreLocation = context.getStoreLocation(NutsStoreFolder.CONFIG);
            this.varStoreLocation = context.getStoreLocation(NutsStoreFolder.VAR);
            this.logsStoreLocation = context.getStoreLocation(NutsStoreFolder.LOGS);
            this.tempStoreLocation = context.getStoreLocation(NutsStoreFolder.TEMP);
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
            this.cacheStoreLocation = other.getCacheStoreLocation();
            this.programsStoreLocation = other.getProgramsStoreLocation();
            this.libStoreLocation = other.getLibStoreLocation();
            this.configStoreLocation = other.getConfigStoreLocation();
            this.varStoreLocation = other.getVarStoreLocation();
            this.logsStoreLocation = other.getLogsStoreLocation();
            this.tempStoreLocation = other.getTempStoreLocation();
            this.storeLocationStrategy = other.getStoreLocationStrategy();
            this.storeLocationLayout = other.getStoreLocationLayout();
        }
    }

    public String getApiVersion() {
        return apiVersion;
    }
    
    public String getApiId() {
        return NutsConstants.NUTS_ID_BOOT_API+"#"+apiVersion;
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
            throw new IllegalArgumentException("Unexpected Behaviour");
        }
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsBootConfig setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsBootConfig setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsBootConfig setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public NutsBootConfig setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsBootConfig setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsBootConfig setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
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

    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsBootConfig setStoreLocationLayout(NutsStoreLocationLayout storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsBootConfig setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public NutsBootConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("NutsBootConfig{");
        if (!NutsUtils.isEmpty(apiVersion)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("apiVersion='").append(apiVersion).append('\'');
        }
        if (!NutsUtils.isEmpty(runtimeId)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("runtimeId='").append(runtimeId).append('\'');
        }
        if (!NutsUtils.isEmpty(runtimeDependencies)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("runtimeDependencies='").append(runtimeDependencies).append('\'');
        }
        if (!NutsUtils.isEmpty(repositories)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("repositories='").append(repositories).append('\'');
        }
        if (!NutsUtils.isEmpty(javaCommand)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("javaCommand='").append(javaCommand).append('\'');
        }
        if (!NutsUtils.isEmpty(javaOptions)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("javaOptions='").append(javaOptions).append('\'');
        }
        if (!NutsUtils.isEmpty(workspace)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("workspace='").append(workspace).append('\'');
        }
        if (!NutsUtils.isEmpty(programsStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("programsStoreLocation='").append(programsStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(libStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("libStoreLocation='").append(libStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(configStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("configStoreLocation='").append(configStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(varStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("varStoreLocation='").append(varStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(logsStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("logsStoreLocation='").append(logsStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(tempStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("tempStoreLocation='").append(tempStoreLocation).append('\'');
        }
        if (!NutsUtils.isEmpty(cacheStoreLocation)) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("cacheStoreLocation='").append(cacheStoreLocation).append('\'');
        }
        if (storeLocationStrategy != null) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("storeLocationStrategy=").append(storeLocationStrategy);
        }
        if (repositoryStoreLocationStrategy != null) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("repositoryStoreLocationStrategy=").append(repositoryStoreLocationStrategy);
        }
        if (storeLocationLayout != null) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append("storeLocationLayout=").append(storeLocationLayout);
        }
        sb.append('}');
        return sb.toString();
    }
}
