package net.vpc.app.nuts.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public final class DefaultNutsBootContext implements NutsBootContext {

    private String name;
    private NutsId bootAPI;
    private NutsId bootRuntime;
    private String bootRuntimeDependencies;
    private String bootExtensionDependencies;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NutsStoreLocationStrategy storeLocationStrategy;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;
    private NutsOsFamily storeLocationLayout;
    private final Map<String, String> storeLocations = new HashMap<>();
    private final Map<String, String> homeLocations = new HashMap<>();
    private boolean global;
    private final NutsWorkspace ws;

    public DefaultNutsBootContext(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsBootContext merge(NutsWorkspaceOptions c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
//        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        if (c.getBootRuntime() != null) {
            this.bootRuntime = c.getBootRuntime().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getBootRuntime())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getBootRuntime());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getRepositories();
        if (c.getBootJavaCommand() != null) {
            this.bootJavaCommand = c.getBootJavaCommand();
        }
        if (c.getBootJavaOptions() != null) {
            this.bootJavaOptions = c.getBootJavaOptions();
        }
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.storeLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
        return this;
    }

    public DefaultNutsBootContext mergeRuntime(NutsWorkspaceOptions c) {
        if (c.getBootRuntime() != null) {
            this.bootRuntime = c.getBootRuntime().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getBootRuntime())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getBootRuntime());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getRepositories();
        if (c.getBootJavaCommand() != null) {
            this.bootJavaCommand = c.getBootJavaCommand();
        }
        if (c.getBootJavaOptions() != null) {
            this.bootJavaOptions = c.getBootJavaOptions();
        }
        return this;
    }

    public DefaultNutsBootContext build(Path workspaceLocation) {
        if(storeLocationStrategy==null){
            storeLocationStrategy=NutsStoreLocationStrategy.EXPLODED;
        }
        if(repositoryStoreLocationStrategy==null){
            repositoryStoreLocationStrategy=NutsStoreLocationStrategy.EXPLODED;
        }
        Path[] homes = new Path[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            String ss=NutsPlatformUtils.resolveHomeFolder(getStoreLocationLayout(), type, homeLocations,isGlobal(), getName());
            if (CoreStringUtils.isBlank(ss)) {
                throw new NutsIllegalArgumentException(null, "Missing Home for " + type.id());
            }
            homes[type.ordinal()] = Paths.get(ss);
        }
        Map<String, String> storeLocations = getStoreLocations() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(getStoreLocations());
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            switch (location) {
                default: {
                String typeLocation = storeLocations.get(typeId);
                    if (CoreStringUtils.isBlank(typeLocation)) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, workspaceLocation.resolve(location.id()).toString());
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()].toString());
                                break;
                            }
                        }
                    } else if (!CoreIOUtils.isAbsolutePath(typeLocation)) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, workspaceLocation.resolve(location.id()).toString());
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()].resolve(typeLocation).toString());
                                break;
                            }
                        }
                    }

                }
            }
        }
        this.storeLocations.clear();
        this.storeLocations.putAll(storeLocations);
        return this;
    }
    
    public DefaultNutsBootContext merge(NutsWorkspaceConfig c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
//        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        if (c.getBootRuntime() != null) {
            this.bootRuntime = c.getBootRuntime().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getBootRuntime())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getBootRuntime());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getRepositories();
        if (c.getBootJavaCommand() != null) {
            this.bootJavaCommand = c.getBootJavaCommand();
        }
        this.bootJavaOptions = c.getBootJavaOptions();
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.storeLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
//        this.gui |= c.isGui();
        return this;
    }

    public DefaultNutsBootContext merge(NutsBootConfig c) {
        this.name = c.getName();
        if (c.getApiVersion() != null) {
            this.bootAPI = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
        if (c.getRuntimeDependencies() != null) {
            this.bootRuntimeDependencies = c.getRuntimeDependencies();
        }
        if (c.getExtensionDependencies() != null) {
            this.bootExtensionDependencies = c.getExtensionDependencies();
        }
        if (c.getRepositories() != null) {
            this.bootRepositories = c.getRepositories();
        }
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.storeLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
        return this;
    }

    @Override
    public String getExtensionDependencies() {
        return bootExtensionDependencies;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isGlobal() {
        return this.global;
    }

    @Override
    public NutsId getApiId() {
        return bootAPI;
    }

    @Override
    public NutsId getRuntimeId() {
        return bootRuntime;
    }

    @Override
    public String getRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    @Override
    public String getRepositories() {
        return bootRepositories;
    }

    @Override
    public String getJavaCommand() {
        return bootJavaCommand;
    }

    @Override
    public String getJavaOptions() {
        return bootJavaOptions;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType) {
        return new NutsStoreLocationsMap(storeLocations).get(folderType);
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
        return new NutsHomeLocationsMap(homeLocations).get(layout, folderType);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    @Override
    public Path getNutsJar() {
        return ws.fetch().id(bootAPI).getResultPath();
    }
}
