package net.thevpc.nuts.runtime.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.NutsHomeLocationsMap;
import net.thevpc.nuts.runtime.NutsStoreLocationsMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

public final class DefaultNutsWorkspaceCurrentConfig {

    private String name;
    private NutsId apiId;
    private NutsId bootRuntime;
    private String bootRuntimeDependencies;
    private String bootExtensionDependencies;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NutsStoreLocationStrategy storeLocationStrategy;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;
    private NutsOsFamily storeLocationLayout;
    private final Map<String, String> userStoreLocations = new HashMap<>();
    private final Map<String, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NutsStoreLocation.values().length];
    private final Map<String, String> homeLocations = new HashMap<>();
    private boolean global;
    private final NutsWorkspace ws;
    private NutsId platform;
    private NutsId os;
    private NutsOsFamily osFamily;
    private NutsId arch;
    private NutsId osdist;

    public DefaultNutsWorkspaceCurrentConfig(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceOptions c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
//        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        if (c.getRuntimeId() != null) {
            setRuntimeId(c.getRuntimeId());
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

    public void setRuntimeId(String s) {
        this.bootRuntime = s.contains("#")
                ? CoreNutsUtils.parseNutsId(s)
                : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + s);
    }

    public DefaultNutsWorkspaceCurrentConfig mergeRuntime(NutsWorkspaceOptions c) {
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
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

    public DefaultNutsWorkspaceCurrentConfig build(Path workspaceLocation) {
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        if (repositoryStoreLocationStrategy == null) {
            repositoryStoreLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Path[] homes = new Path[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            String ss = Nuts.getPlatformHomeFolder(getStoreLocationLayout(), type, homeLocations, isGlobal(), getName());
            if (CoreStringUtils.isBlank(ss)) {
                throw new NutsIllegalArgumentException(null, "Missing Home for " + type.id());
            }
            homes[type.ordinal()] = Paths.get(ss);
        }
        Map<String, String> storeLocations = getStoreLocations();
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
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i] = Paths.get(effStoreLocationsMap.get(NutsStoreLocation.values()[i].id()));
        }
        if (apiId == null) {
            apiId = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
        }
        if (storeLocationLayout == null) {
            storeLocationLayout = getOsFamily();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigApi c) {
        if (c.getApiVersion() != null) {
            this.apiId = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
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

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigRuntime c) {
        if (c.getId() != null) {
            this.bootRuntime = CoreNutsUtils.parseNutsId(c.getId());
        }
        if (c.getDependencies() != null) {
            this.bootRuntimeDependencies = c.getDependencies();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfigBoot c) {
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

    public DefaultNutsWorkspaceCurrentConfig merge(NutsBootConfig c) {
        this.name = c.getName();
        if (c.getApiVersion() != null) {
            this.apiId = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
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


    public String getExtensionDependencies() {
        return bootExtensionDependencies;
    }


    public String getName() {
        return name;
    }


    public boolean isGlobal() {
        return this.global;
    }


    public String getApiVersion() {
        return getApiId().getVersion().getValue();
    }


    public NutsId getApiId() {
        return apiId;
    }


    public NutsId getRuntimeId() {
        return bootRuntime;
    }


    public String getRuntimeDependencies() {
        return bootRuntimeDependencies;
    }


    public String getBootRepositories() {
        return bootRepositories;
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


    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }


    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(effStoreLocationsMap);
    }


    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }


    public Path getStoreLocation(NutsStoreLocation folderType) {
        return effStoreLocationPath[folderType.ordinal()];
    }


    public Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
        String path = new NutsHomeLocationsMap(homeLocations).get(layout, folderType);
        return path == null ? null : Paths.get(path);
    }


    public Path getHomeLocation(NutsStoreLocation folderType) {
        return Paths.get(Nuts.getPlatformHomeFolder(getStoreLocationLayout(),
                folderType, getHomeLocations(),
                isGlobal(),
                getName()
        ));
    }


    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public DefaultNutsWorkspaceCurrentConfig setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setUserStoreLocations(Map<String, String> userStoreLocations) {
        this.userStoreLocations.clear();
        if (userStoreLocations != null) {
            this.userStoreLocations.putAll(userStoreLocations);
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setName(String name) {
        this.name = name;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setApiId(NutsId apiId) {
        this.apiId = apiId;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setRuntimeId(NutsId bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setRuntimeDependencies(String bootRuntimeDependencies) {
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setExtensionDependencies(String bootExtensionDependencies) {
        this.bootExtensionDependencies = bootExtensionDependencies;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
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

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setGlobal(boolean global) {
        this.global = global;
        return this;
    }


    public NutsId getArch() {
        if (arch == null) {
            arch = ws.id().parser().parse(CorePlatformUtils.getPlatformArch());
        }
        return arch;
    }


    private static NutsOsFamily getPlatformOsFamily0() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (property.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (property.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (property.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (property.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        return NutsOsFamily.UNKNOWN;
    }

    public NutsOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = getPlatformOsFamily0();
        }
        return osFamily;
    }


    public NutsId getOs() {
        if (os == null) {
            os = ws.id().parser().parse(CorePlatformUtils.getPlatformOs(ws));
        }
        return os;
    }

    public NutsId getPlatform() {
        if (platform == null) {
            platform = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .createSdkId("java", System.getProperty("java.version"));
        }
        return platform;
    }


    public NutsId getOsDist() {
        if (osdist == null) {
            osdist = ws.id().parser().parse(CorePlatformUtils.getPlatformOsDist(ws));
        }
        return osdist;
    }

    //
    public Path getStoreLocation(String id, NutsStoreLocation folderType) {
        return getStoreLocation(ws.id().parser().parse(id), folderType);
    }

    //
    public Path getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        Path storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        switch (folderType) {
            case CACHE:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id));
            case CONFIG:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(ws.locations().getDefaultIdBasedir(id));
    }

//    
//    public Path getStoreLocation(NutsStoreLocation folderType) {
//        String s=effStoreLocations.get(folderType.id());
//        return s==null?null:Paths.get(s);
////        String n = CoreNutsUtils.getArrItem(getStoreLocations(), folderType.ordinal());
////        switch (getStoreLocationStrategy()) {
////            case STANDALONE: {
////                if (CoreStringUtils.isBlank(n)) {
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
