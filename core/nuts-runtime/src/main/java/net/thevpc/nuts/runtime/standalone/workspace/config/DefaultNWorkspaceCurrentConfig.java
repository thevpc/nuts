package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.boot.NBootConfig;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.util.NPlatformUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultNWorkspaceCurrentConfig {

    private final Map<NStoreLocation, String> userStoreLocations = new HashMap<>();
    private final Map<NStoreLocation, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NStoreLocation.values().length];
    private final Map<NHomeLocation, String> homeLocations = new HashMap<>();
    private final NWorkspace ws;
    private String name;
    private NId apiId;
    private NId bootRuntime;
    private NDescriptor runtimeBootDescriptor;
    private List<NDescriptor> extensionBootDescriptors;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NStoreLocationStrategy storeLocationStrategy;
    private NStoreLocationStrategy repositoryStoreLocationStrategy;
    private NOsFamily storeLocationLayout;
    private Boolean global;
//    private NutsId platform;
//    private NutsId os;
//    private NutsOsFamily osFamily;
//    private NutsId arch;
//    private NutsId osdist;

    public DefaultNWorkspaceCurrentConfig(NWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceOptions c, NSession session) {
        if (c.getName() != null) {
            this.name = c.getName().orNull();
        }
        if (c.getRuntimeId().isPresent()) {
            setRuntimeId(c.getRuntimeId().get(), session);
        }
        if (c.getJavaCommand().isPresent()) {
            this.bootJavaCommand = c.getJavaCommand().get();
        }
        if (c.getJavaOptions().isPresent()) {
            this.bootJavaOptions = c.getJavaOptions().get();
        }
        if (c.getStoreLocationStrategy().isPresent()) {
            this.storeLocationStrategy = c.getStoreLocationStrategy().get();
        }
        if (c.getRepositoryStoreLocationStrategy().isPresent()) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy().get();
        }
        if (c.getStoreLocationLayout().isPresent()) {
            this.storeLocationLayout = c.getStoreLocationLayout().get();
        }
        for (Map.Entry<NStoreLocation, String> e : new NStoreLocationsMap(c.getStoreLocations().orNull()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations().orNull()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.getGlobal().orElse(false);
        }
        return this;
    }

    public void setRuntimeId(NId s, NSession session) {
        this.bootRuntime = s;
    }

    public DefaultNWorkspaceCurrentConfig mergeRuntime(NWorkspaceOptions c, NSession session) {
        if (c.getRuntimeId().isPresent()) {
            this.bootRuntime = c.getRuntimeId().get();
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
        if (c.getJavaCommand().isPresent()) {
            this.bootJavaCommand = c.getJavaCommand().get();
        }
        if (c.getJavaOptions().isPresent()) {
            this.bootJavaOptions = c.getJavaOptions().get();
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig build(NPath workspaceLocation, NSession session) {
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NStoreLocationStrategy.EXPLODED;
        }
        if (repositoryStoreLocationStrategy == null) {
            repositoryStoreLocationStrategy = NStoreLocationStrategy.EXPLODED;
        }
        Map<NStoreLocation, String> storeLocations = NPlatformUtils.buildLocations(getStoreLocationLayout(), storeLocationStrategy,
                getStoreLocations(), homeLocations, isGlobal(), workspaceLocation.toString(),
                session
        );
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i] = Paths.get(effStoreLocationsMap.get(NStoreLocation.values()[i]));
        }
        if (apiId == null) {
            apiId = NId.ofApi(Nuts.getVersion()).get(session);
        }
        if (storeLocationLayout == null) {
            storeLocationLayout = NEnvs.of(session).getOsFamily();
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigApi c, NSession session) {
        if (c.getApiVersion() != null && !c.getApiVersion().isBlank()) {
            this.apiId = NId.ofApi(c.getApiVersion()).get(session);
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId();
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

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigRuntime c, NSession session) {
        if (c.getId() != null) {
            this.bootRuntime = c.getId();
        }
        if (c.getDependencies() != null) {
            this.runtimeBootDescriptor = new DefaultNDescriptorBuilder()
                    .setId(NId.of(this.bootRuntime.toString()).get())
                    .setDependencies(
                            StringTokenizerUtils.splitSemiColon(c.getDependencies()).stream()
                                    .map(x-> NDependency.of(x).get(session)).collect(Collectors.toList())
                    ).build()
            ;
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigBoot c, NSession session) {
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
        for (Map.Entry<NStoreLocation, String> e : new NStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.isGlobal();
        }
//        this.gui |= c.getGui().orElse(false);
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NBootConfig c, NSession session) {
        this.name = c.getName();
        if (c.getApiVersion() != null) {
            this.apiId = NId.of(NConstants.Ids.NUTS_API + "#" + c.getApiVersion()).get(session);
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId();
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
        for (Map.Entry<NStoreLocation, String> e : new NStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.userStoreLocations.put(e.getKey(),e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if(NBlankable.isBlank(o)){
                this.homeLocations.put(e.getKey(),e.getValue());
            }
        }
        if(this.global==null) {
            this.global = c.isGlobal();
        }
        return this;
    }


    public List<NDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    public DefaultNWorkspaceCurrentConfig setExtensionBootDescriptors(List<NDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = extensionBootDescriptors;
        return this;
    }

    public String getName() {
        return name;
    }

    public DefaultNWorkspaceCurrentConfig setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isGlobal() {
        return this.global!=null && this.global;
    }

    public DefaultNWorkspaceCurrentConfig setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public NVersion getApiVersion() {
        return getApiId().getVersion();
    }

    public NId getApiId() {
        return apiId;
    }

    public DefaultNWorkspaceCurrentConfig setApiId(NId apiId) {
        this.apiId = apiId;
        return this;
    }

    public NId getRuntimeId() {
        return bootRuntime;
    }

    public DefaultNWorkspaceCurrentConfig setRuntimeId(NId bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public NDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    public DefaultNWorkspaceCurrentConfig setRuntimeBootDescriptor(NDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public DefaultNWorkspaceCurrentConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public String getJavaCommand() {
        return bootJavaCommand;
    }

    public String getJavaOptions() {
        return bootJavaOptions;
    }

    public NStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public DefaultNWorkspaceCurrentConfig setStoreLocationStrategy(NStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    public DefaultNWorkspaceCurrentConfig setRepositoryStoreLocationStrategy(NStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public Map<NStoreLocation, String> getStoreLocations() {
        return new LinkedHashMap<>(effStoreLocationsMap);
    }

    public Map<NHomeLocation, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    public DefaultNWorkspaceCurrentConfig setHomeLocations(Map<NHomeLocation, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    public NPath getStoreLocation(NStoreLocation folderType, NSession session) {
        Path p = effStoreLocationPath[folderType.ordinal()];
        return p==null?null: NPath.of(p,session);
    }

    public NPath getHomeLocation(NHomeLocation location, NSession session) {
        String s = new NHomeLocationsMap(homeLocations).get(location);
        return s==null?null: NPath.of(s,session);
    }

    public NPath getHomeLocation(NStoreLocation folderType, NSession session) {
        return NPath.of(Paths.get(NPlatformUtils.getPlatformHomeFolder(getStoreLocationLayout(),
                folderType, getHomeLocations(),
                isGlobal(),
                getName()
        )),session);
    }

    public NOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public DefaultNWorkspaceCurrentConfig setStoreLocationLayout(NOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public DefaultNWorkspaceCurrentConfig setUserStoreLocations(Map<NStoreLocation, String> userStoreLocations) {
        this.userStoreLocations.clear();
        if (userStoreLocations != null) {
            this.userStoreLocations.putAll(userStoreLocations);
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public DefaultNWorkspaceCurrentConfig setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }



    public NPath getStoreLocation(String id, NStoreLocation folderType, NSession session) {
        return getStoreLocation(NId.of(id).get(session), folderType, session);
    }

    public NPath getStoreLocation(NId id, NStoreLocation folderType, NSession session) {
        NPath storeLocation = getStoreLocation(folderType,session);
        if (storeLocation == null) {
            return null;
        }
        switch (folderType) {
            case CACHE:
                return storeLocation.resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(id));
            case CONFIG:
                return storeLocation.resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(NLocations.of(session).getDefaultIdBasedir(id));
    }

}
