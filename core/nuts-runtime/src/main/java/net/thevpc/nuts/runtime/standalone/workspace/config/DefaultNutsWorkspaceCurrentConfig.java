package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsBootDescriptor;
import net.thevpc.nuts.spi.NutsBootId;
import net.thevpc.nuts.runtime.standalone.boot.NutsBootConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DefaultNutsWorkspaceCurrentConfig {

    private final Map<NutsStoreLocation, String> userStoreLocations = new HashMap<>();
    private final Map<NutsStoreLocation, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NutsStoreLocation.values().length];
    private final Map<NutsHomeLocation, String> homeLocations = new HashMap<>();
    private final NutsWorkspace ws;
    private String name;
    private NutsId apiId;
    private NutsId bootRuntime;
    private NutsBootDescriptor runtimeBootDescriptor;
    private NutsBootDescriptor[] extensionBootDescriptors;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NutsStoreLocationStrategy storeLocationStrategy;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;
    private NutsOsFamily storeLocationLayout;
    private Boolean global;
//    private NutsId platform;
//    private NutsId os;
//    private NutsOsFamily osFamily;
//    private NutsId arch;
//    private NutsId osdist;

    public DefaultNutsWorkspaceCurrentConfig(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceOptions c, NutsSession session) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
//        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        if (c.getRuntimeId() != null) {
            setRuntimeId(c.getRuntimeId(), session);
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
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
        for (Map.Entry<NutsStoreLocation, String> e : new NutsStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NutsHomeLocation, String> e : new NutsHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.isGlobal();
        }
        return this;
    }

    public void setRuntimeId(String s, NutsSession session) {
        this.bootRuntime = s.contains("#")
                ? NutsId.of(s,session)
                : NutsId.of(NutsConstants.Ids.NUTS_RUNTIME + "#" + s,session);
    }

    public DefaultNutsWorkspaceCurrentConfig mergeRuntime(NutsWorkspaceOptions c, NutsSession session) {
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? NutsId.of(c.getRuntimeId(),session)
                    : NutsId.of(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId(),session);
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig build(NutsPath workspaceLocation, NutsSession session) {
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        if (repositoryStoreLocationStrategy == null) {
            repositoryStoreLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Map<NutsStoreLocation, String> storeLocations = NutsUtilPlatforms.buildLocations(getStoreLocationLayout(), storeLocationStrategy,
                getStoreLocations(), homeLocations, isGlobal(), workspaceLocation.toString(),
                session
        );
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i] = Paths.get(effStoreLocationsMap.get(NutsStoreLocation.values()[i]));
        }
        if (apiId == null) {
            apiId = NutsId.of(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion(),session);
        }
        if (storeLocationLayout == null) {
            storeLocationLayout = session.env().getOsFamily();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigApi c, NutsSession session) {
        NutsIdParser parser = NutsIdParser.of(session);
        if (c.getApiVersion() != null) {
            this.apiId = parser.parse(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? parser.parse(c.getRuntimeId())
                    : parser.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
//        if (c.getExtensionDependencies() != null) {
//            this.bootExtensionDependencies = c.getExtensionDependencies();
//        }
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigRuntime c, NutsSession session) {
        if (c.getId() != null) {
            this.bootRuntime = NutsId.of(c.getId(),session);
        }
        if (c.getDependencies() != null) {
            this.runtimeBootDescriptor = new NutsBootDescriptor(
                    NutsBootId.parse(this.bootRuntime.toString()),
                    Arrays.stream(c.getDependencies().split(";"))
                            .map(NutsBootId::parse).toArray(NutsBootId[]::new)

            );
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigBoot c, NutsSession session) {
        if (c.getName() != null) {
            this.name = c.getName();
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
        for (Map.Entry<NutsStoreLocation, String> e : new NutsStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NutsHomeLocation, String> e : new NutsHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.isGlobal();
        }
//        this.gui |= c.isGui();
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsBootConfig c, NutsSession session) {
        this.name = c.getName();
        NutsIdParser parser = NutsIdParser.of(session);
        if (c.getApiVersion() != null) {
            this.apiId = parser.parse(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? parser.parse(c.getRuntimeId())
                    : parser.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
        if (c.getRuntimeBootDescriptor() != null) {
            this.runtimeBootDescriptor = c.getRuntimeBootDescriptor();
        }
        if (c.getExtensionBootDescriptors() != null) {
            this.extensionBootDescriptors = c.getExtensionBootDescriptors();
        }
        if (c.getBootRepositories() != null) {
            this.bootRepositories = c.getBootRepositories();
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
        for (Map.Entry<NutsStoreLocation, String> e : new NutsStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NutsHomeLocation, String> e : new NutsHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NutsBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.isGlobal();
        }
        return this;
    }


    public NutsBootDescriptor[] getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public DefaultNutsWorkspaceCurrentConfig setExtensionBootDescriptors(NutsBootDescriptor[] extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCurrentConfig setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isGlobal() {
        return this.global!=null && this.global;
    }

    public DefaultNutsWorkspaceCurrentConfig setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public String getApiVersion() {
        return getApiId().getVersion().getValue();
    }

    public NutsId getApiId() {
        return apiId;
    }

    public DefaultNutsWorkspaceCurrentConfig setApiId(NutsId apiId) {
        this.apiId = apiId;
        return this;
    }

    public NutsId getRuntimeId() {
        return bootRuntime;
    }

    public DefaultNutsWorkspaceCurrentConfig setRuntimeId(NutsId bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public NutsBootDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public DefaultNutsWorkspaceCurrentConfig setRuntimeBootDescriptor(NutsBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getJavaCommand() {
        return bootJavaCommand;
    }

    public String getJavaOptions() {
        return bootJavaOptions;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public DefaultNutsWorkspaceCurrentConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public Map<NutsStoreLocation, String> getStoreLocations() {
        return new LinkedHashMap<>(effStoreLocationsMap);
    }

    public Map<NutsHomeLocation, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    public DefaultNutsWorkspaceCurrentConfig setHomeLocations(Map<NutsHomeLocation, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    public NutsPath getStoreLocation(NutsStoreLocation folderType,NutsSession session) {
        Path p = effStoreLocationPath[folderType.ordinal()];
        return p==null?null:NutsPath.of(p,session);
    }

    public NutsPath getHomeLocation(NutsHomeLocation location,NutsSession session) {
        String s = new NutsHomeLocationsMap(homeLocations).get(location);
        return s==null?null:NutsPath.of(s,session);
    }

    public NutsPath getHomeLocation(NutsStoreLocation folderType,NutsSession session) {
        return NutsPath.of(Paths.get(NutsUtilPlatforms.getPlatformHomeFolder(getStoreLocationLayout(),
                folderType, getHomeLocations(),
                isGlobal(),
                getName()
        )),session);
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setUserStoreLocations(Map<NutsStoreLocation, String> userStoreLocations) {
        this.userStoreLocations.clear();
        if (userStoreLocations != null) {
            this.userStoreLocations.putAll(userStoreLocations);
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }



    public NutsPath getStoreLocation(String id, NutsStoreLocation folderType, NutsSession session) {
        return getStoreLocation(NutsId.of(id,session), folderType, session);
    }

    public NutsPath getStoreLocation(NutsId id, NutsStoreLocation folderType, NutsSession session) {
        NutsPath storeLocation = getStoreLocation(folderType,session);
        if (storeLocation == null) {
            return null;
        }
        switch (folderType) {
            case CACHE:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(id));
            case CONFIG:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(session.locations().getDefaultIdBasedir(id));
    }

}
