package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.boot.NBootConfig;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.util.NPlatformHome;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultNWorkspaceCurrentConfig {

    private final Map<NStoreType, String> userStoreLocations = new HashMap<>();
    private final Map<NStoreType, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NStoreType.values().length];
    private final Map<NHomeLocation, String> homeLocations = new HashMap<>();
    private final NWorkspace workspace;
    private String name;
    private NId apiId;
    private NId bootRuntime;
    private NDescriptor runtimeBootDescriptor;
    private List<NDescriptor> extensionBootDescriptors;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NStoreStrategy storeStrategy;
    private NStoreStrategy repositoryStoreStrategy;
    private NOsFamily storeLayout;
    private Boolean system;
//    private NutsId platform;
//    private NutsId os;
//    private NutsOsFamily osFamily;
//    private NutsId arch;
//    private NutsId osdist;

    public DefaultNWorkspaceCurrentConfig(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceOptions c, NSession session) {
        if (c.getName() != null) {
            this.name = c.getName().orNull();
        }
        if (c.getRuntimeId().isPresent()) {
            setRuntimeId(c.getRuntimeId().get());
        }
        if (c.getJavaCommand().isPresent()) {
            this.bootJavaCommand = c.getJavaCommand().get();
        }
        if (c.getJavaOptions().isPresent()) {
            this.bootJavaOptions = c.getJavaOptions().get();
        }
        if (c.getStoreStrategy().isPresent()) {
            this.storeStrategy = c.getStoreStrategy().get();
        }
        if (c.getRepositoryStoreStrategy().isPresent()) {
            this.repositoryStoreStrategy = c.getRepositoryStoreStrategy().get();
        }
        if (c.getStoreLayout().isPresent()) {
            this.storeLayout = c.getStoreLayout().get();
        }
        for (Map.Entry<NStoreType, String> e : new NStoreLocationsMap(c.getStoreLocations().orNull()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.userStoreLocations.put(e.getKey(), e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations().orNull()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.homeLocations.put(e.getKey(), e.getValue());
            }
        }
        if (this.system == null) {
            this.system = c.getSystem().orElse(false);
        }
        return this;
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

    public DefaultNWorkspaceCurrentConfig build(NPath workspaceLocation) {
        if (storeStrategy == null) {
            storeStrategy = NStoreStrategy.EXPLODED;
        }
        if (repositoryStoreStrategy == null) {
            repositoryStoreStrategy = NStoreStrategy.EXPLODED;
        }
        NSession session= workspace.currentSession();
        Map<NStoreType, String> storeLocations =
        NPlatformHome.of(getStoreLayout(),getSystem()).buildLocations(storeStrategy,
                getStoreLocations(), homeLocations, workspaceLocation.toString()
        );
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i] = Paths.get(effStoreLocationsMap.get(NStoreType.values()[i]));
        }
        if (apiId == null) {
            apiId = NId.ofApi(Nuts.getVersion()).get();
        }
        if (storeLayout == null) {
            storeLayout = NEnvs.of().getOsFamily();
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigApi c, NSession session) {
        if (c.getApiVersion() != null && !c.getApiVersion().isBlank()) {
            this.apiId = NId.ofApi(c.getApiVersion()).get();
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

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigRuntime c) {
        if (c.getId() != null) {
            this.bootRuntime = c.getId();
        }
        if (c.getDependencies() != null) {
            NSession session=workspace.currentSession();
            this.runtimeBootDescriptor = new DefaultNDescriptorBuilder()
                    .setId(NId.of(this.bootRuntime.toString()).get())
                    .setDependencies(
                            StringTokenizerUtils.splitSemiColon(c.getDependencies()).stream()
                                    .map(x -> NDependency.of(x).get()).collect(Collectors.toList())
                    ).build()
            ;
        }
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NWorkspaceConfigBoot c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
        if (c.getStoreStrategy() != null) {
            this.storeStrategy = c.getStoreStrategy();
        }
        if (c.getRepositoryStoreStrategy() != null) {
            this.repositoryStoreStrategy = c.getRepositoryStoreStrategy();
        }
        if (c.getStoreLayout() != null) {
            this.storeLayout = c.getStoreLayout();
        }
        for (Map.Entry<NStoreType, String> e : new NStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.userStoreLocations.put(e.getKey(), e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.homeLocations.put(e.getKey(), e.getValue());
            }
        }
        if (this.system == null) {
            this.system = c.isSystem();
        }
//        this.gui |= c.getGui().orElse(false);
        return this;
    }

    public DefaultNWorkspaceCurrentConfig merge(NBootConfig c) {
        this.name = c.getName();
        if (c.getApiVersion() != null) {
            NSession session=workspace.currentSession();
            this.apiId = NId.of(NConstants.Ids.NUTS_API + "#" + c.getApiVersion()).get();
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
        if (c.getStoreStrategy() != null) {
            this.storeStrategy = c.getStoreStrategy();
        }
        if (c.getRepositoryStoreStrategy() != null) {
            this.repositoryStoreStrategy = c.getRepositoryStoreStrategy();
        }
        if (c.getStoreLayout() != null) {
            this.storeLayout = c.getStoreLayout();
        }
        for (Map.Entry<NStoreType, String> e : new NStoreLocationsMap(c.getStoreLocations()).toMap().entrySet()) {
            String o = this.userStoreLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.userStoreLocations.put(e.getKey(), e.getValue());
            }
        }
        for (Map.Entry<NHomeLocation, String> e : new NHomeLocationsMap(c.getHomeLocations()).toMap().entrySet()) {
            String o = this.homeLocations.get(e.getKey());
            if (NBlankable.isBlank(o)) {
                this.homeLocations.put(e.getKey(), e.getValue());
            }
        }
        if (this.system == null) {
            this.system = c.isSystem();
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

    public boolean getSystem() {
        return this.system;
    }

    public boolean isSystem() {
        return this.system != null && this.system;
    }

    public DefaultNWorkspaceCurrentConfig setSystem(boolean system) {
        this.system = system;
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

    public NStoreStrategy getStoreStrategy() {
        return storeStrategy;
    }

    public DefaultNWorkspaceCurrentConfig setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public NStoreStrategy getRepositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    public DefaultNWorkspaceCurrentConfig setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    public Map<NStoreType, String> getStoreLocations() {
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

    public NPath getStoreLocation(NStoreType storeType) {
        NSession session=workspace.currentSession();
        Path p = effStoreLocationPath[storeType.ordinal()];
        return p == null ? null : NPath.of(p);
    }

    public NPath getHomeLocation(NHomeLocation location) {
        NSession session=workspace.currentSession();
        String s = new NHomeLocationsMap(homeLocations).get(location);
        return s == null ? null : NPath.of(s);
    }

    public NPath getHomeLocation(NStoreType storeType) {
        NSession session=workspace.currentSession();
        return NPath.of(Paths.get(NPlatformHome.of(getStoreLayout(), isSystem()).getWorkspaceLocation(
                storeType, getHomeLocations(),
                getName()
        )));
    }

    public NOsFamily getStoreLayout() {
        return storeLayout;
    }

    public DefaultNWorkspaceCurrentConfig setStoreLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    public DefaultNWorkspaceCurrentConfig setUserStoreLocations(Map<NStoreType, String> userStoreLocations) {
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


    public NPath getStoreLocation(String id, NStoreType storeType) {
        NSession session=workspace.currentSession();
        return getStoreLocation(NId.of(id).get(), storeType);
    }

    public NPath getStoreLocation(NId id, NStoreType storeType) {
        NSession session=workspace.currentSession();
        NPath storeLocation = getStoreLocation(storeType);
        if (storeLocation == null) {
            return null;
        }
        switch (storeType) {
            case CACHE:
                return storeLocation.resolve(NConstants.Folders.ID).resolve(NLocations.of().getDefaultIdBasedir(id));
            case CONF:
                return storeLocation.resolve(NConstants.Folders.ID).resolve(NLocations.of().getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(NLocations.of().getDefaultIdBasedir(id));
    }

}
