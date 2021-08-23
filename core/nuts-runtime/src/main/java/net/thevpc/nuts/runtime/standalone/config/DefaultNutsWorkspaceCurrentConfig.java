package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsBootDescriptor;
import net.thevpc.nuts.boot.NutsBootId;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.NutsHomeLocationsMap;
import net.thevpc.nuts.runtime.standalone.NutsStoreLocationsMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DefaultNutsWorkspaceCurrentConfig {

    private final Map<String, String> userStoreLocations = new HashMap<>();
    private final Map<String, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NutsStoreLocation.values().length];
    private final Map<String, String> homeLocations = new HashMap<>();
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
    private boolean global;
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
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
        return this;
    }

    public void setRuntimeId(String s, NutsSession session) {
        this.bootRuntime = s.contains("#")
                ? session.getWorkspace().id().parser().parse(s)
                : session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + s);
    }

    public DefaultNutsWorkspaceCurrentConfig mergeRuntime(NutsWorkspaceOptions c, NutsSession session) {
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? session.getWorkspace().id().parser().parse(c.getRuntimeId())
                    : session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
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

    public DefaultNutsWorkspaceCurrentConfig build(String workspaceLocation, NutsSession session) {
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        if (repositoryStoreLocationStrategy == null) {
            repositoryStoreLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Path[] homes = new Path[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            String ss = Nuts.getPlatformHomeFolder(getStoreLocationLayout(), type, homeLocations, isGlobal(), getName());
            if (NutsUtilStrings.isBlank(ss)) {
                throw new NutsIllegalArgumentException(session,
                        NutsMessage.cstyle("missing Home for %s", type));
            }
            homes[type.ordinal()] = Paths.get(ss);
        }
        Map<String, String> storeLocations = getStoreLocations();
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            switch (location) {
                default: {
                    String typeLocation = storeLocations.get(typeId);
                    if (NutsUtilStrings.isBlank(typeLocation)) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, Paths.get(workspaceLocation).resolve(location.id()).toString());
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
                                storeLocations.put(typeId, Paths.get(workspaceLocation).resolve(location.id()).toString());
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
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i] = Paths.get(effStoreLocationsMap.get(NutsStoreLocation.values()[i].id()));
        }
        if (apiId == null) {
            apiId = session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
        }
        if (storeLocationLayout == null) {
            storeLocationLayout = session.getWorkspace().env().getOsFamily();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigApi c, NutsSession session) {
        NutsIdParser parser = session.getWorkspace().id().parser();
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
            this.bootRuntime = session.getWorkspace().id().parser().parse(c.getId());
        }
        if (c.getDependencies() != null) {
            this.runtimeBootDescriptor = new NutsBootDescriptor(
                    NutsBootId.parse(this.bootRuntime.toString()),
                    Arrays.stream(c.getDependencies().split(";"))
                            .map(x -> NutsBootId.parse(x)).toArray(NutsBootId[]::new)

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
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
//        this.gui |= c.isGui();
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsBootConfig c, NutsSession session) {
        this.name = c.getName();
        NutsIdParser parser = session.getWorkspace().id().parser();
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
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
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
        return this.global;
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

    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(effStoreLocationsMap);
    }

    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    public DefaultNutsWorkspaceCurrentConfig setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    public String getStoreLocation(NutsStoreLocation folderType) {
        return effStoreLocationPath[folderType.ordinal()].toString();
    }

    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
        String path = new NutsHomeLocationsMap(homeLocations).get(layout, folderType);
        return path;
    }

    public String getHomeLocation(NutsStoreLocation folderType) {
        return Paths.get(Nuts.getPlatformHomeFolder(getStoreLocationLayout(),
                folderType, getHomeLocations(),
                isGlobal(),
                getName()
        )).toString();
    }

    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setUserStoreLocations(Map<String, String> userStoreLocations) {
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


//    public NutsId getArch() {
//        if (arch == null) {
//            arch = ws.id().parser().parse(CorePlatformUtils.getPlatformArch());
//        }
//        return arch;
//    }


//    private static NutsOsFamily getPlatformOsFamily0() {
//        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
//        if (property.startsWith("linux")) {
//            return NutsOsFamily.LINUX;
//        }
//        if (property.startsWith("win")) {
//            return NutsOsFamily.WINDOWS;
//        }
//        if (property.startsWith("mac")) {
//            return NutsOsFamily.MACOS;
//        }
//        if (property.startsWith("sunos")) {
//            return NutsOsFamily.UNIX;
//        }
//        if (property.startsWith("freebsd")) {
//            return NutsOsFamily.UNIX;
//        }
//        return NutsOsFamily.UNKNOWN;
//    }

//    public NutsOsFamily getOsFamily() {
//        if (osFamily == null) {
//            osFamily = getPlatformOsFamily0();
//        }
//        return osFamily;
//    }
//
//
//    public NutsId getOs() {
//        if (os == null) {
//            os = ws.id().parser().parse(CorePlatformUtils.getPlatformOs(ws));
//        }
//        return os;
//    }
//
//    public NutsId getPlatform() {
//        if (platform == null) {
//            platform = NutsWorkspaceConfigManagerExt.of(ws.config())
//                    .createSdkId("java", System.getProperty("java.version"));
//        }
//        return platform;
//    }
//
//
//    public NutsId getOsDist() {
//        if (osdist == null) {
//            osdist = ws.id().parser().parse(CorePlatformUtils.getPlatformOsDist(ws));
//        }
//        return osdist;
//    }

    //
    public String getStoreLocation(String id, NutsStoreLocation folderType, NutsSession session) {
        return getStoreLocation(session.getWorkspace().id().parser().parse(id), folderType, session);
    }

    //
    public String getStoreLocation(NutsId id, NutsStoreLocation folderType, NutsSession session) {
        String storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        switch (folderType) {
            case CACHE:
                return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(session.getWorkspace().locations().getDefaultIdBasedir(id)).toString();
            case CONFIG:
                return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(session.getWorkspace().locations().getDefaultIdBasedir(id)).toString();
        }
        return Paths.get(storeLocation).resolve(session.getWorkspace().locations().getDefaultIdBasedir(id)).toString();
    }

//    
//    public Path getStoreLocation(NutsStoreLocation folderType) {
//        String s=effStoreLocations.get(folderType.id());
//        return s==null?null:Paths.get(s);
////        String n = CoreNutsUtils.getArrItem(getStoreLocations(), folderType.ordinal());
////        switch (getStoreLocationStrategy()) {
////            case STANDALONE: {
////                if (NutsUtilStrings.isBlank(n)) {
////                    n = folderType.toString().toLowerCase();
////                }
////                n = n.trim();
////                return getStoreLocation().resolve(n);
////            }
////            case EXPLODED: {
////                Path storeLocation = repository.getWorkspace().config().getStoreLocation(folderType);
////                //uuid is added as
////                return storeLocation.resolve(NutsConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());
////
////            }
////            default: {
////                throw new NutsIllegalArgumentException(repository.getWorkspace(), "Unsupported strategy type " + getStoreLocation());
////            }
////        }
//    }

}
