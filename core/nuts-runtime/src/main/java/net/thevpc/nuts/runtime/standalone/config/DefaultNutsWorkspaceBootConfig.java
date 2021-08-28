package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.NutsHomeLocationsMap;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DefaultNutsWorkspaceBootConfig implements NutsWorkspaceBootConfig {
    private String bootPath;
    private String effectiveWorkspace;
    private String effectiveWorkspaceName;
    private boolean immediateLocation;

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

    private List<Extension> extensions;

    public DefaultNutsWorkspaceBootConfig(NutsSession session, String bootPath, String effectiveWorkspace, String effectiveWorkspaceName, boolean immediateLocation, NutsWorkspaceConfigBoot bootModel) {
        this.bootPath = bootPath;
        this.effectiveWorkspace = effectiveWorkspace;
        this.immediateLocation = immediateLocation;
        this.effectiveWorkspaceName = effectiveWorkspaceName;
        this.uuid = bootModel.getUuid();
        this.global = bootModel.isGlobal();
        this.name = bootModel.getName();
        this.workspace = bootModel.getWorkspace();
        this.bootRepositories = bootModel.getBootRepositories();
        this.storeLocations = bootModel.getStoreLocations()==null?new HashMap<>() : new HashMap<>(bootModel.getStoreLocations());
        this.homeLocations = bootModel.getHomeLocations()==null?new HashMap<>() : new HashMap<>(bootModel.getHomeLocations());
        this.repositoryStoreLocationStrategy = bootModel.getRepositoryStoreLocationStrategy();
        this.storeLocationStrategy = bootModel.getStoreLocationStrategy();
        this.storeLocationLayout = bootModel.getStoreLocationLayout();

        String[] homes = new String[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            homes[type.ordinal()] = NutsUtilPlatforms.getPlatformHomeFolder(storeLocationLayout, type, homeLocations,
                    global, name);
            if (NutsUtilStrings.isBlank(homes[type.ordinal()])) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing Home for %s", type.id()));
            }
        }
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            String _storeLocation = storeLocations.get(typeId);
            if (NutsUtilStrings.isBlank(_storeLocation)) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        storeLocations.put(typeId, (effectiveWorkspace + File.separator + typeId));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(typeId, homes[location.ordinal()]);
                        break;
                    }
                }
            } else if (!CoreIOUtils.isAbsolutePath(_storeLocation)) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        storeLocations.put(typeId, (effectiveWorkspace + File.separator + location.id()));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(typeId, homes[location.ordinal()] + CoreIOUtils.syspath("/" + _storeLocation));
                        break;
                    }
                }
            }
        }

        List<NutsWorkspaceConfigBoot.ExtensionConfig> extensions = bootModel.getExtensions();
        if (extensions == null) {
            this.extensions= Collections.emptyList();
        }else {
            this.extensions=Collections.unmodifiableList(extensions.stream().map(x -> new NutsWorkspaceBootConfigExtensionImpl(x)).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean isImmediateLocation() {
        return immediateLocation;
    }

    @Override
    public String getEffectiveWorkspaceName() {
        return effectiveWorkspaceName;
    }

    @Override
    public String getBootPath() {
        return bootPath;
    }



    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public String getEffectiveWorkspace() {
        return effectiveWorkspace;
    }

    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    @Override
    public String getBootRepositories() {
        return bootRepositories;
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return Collections.unmodifiableMap(storeLocations);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        return Collections.unmodifiableMap(homeLocations);
    }


    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }


    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }


    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    @Override
    public String getUuid() {
        return uuid;
    }


    @Override
    public boolean isGlobal() {
        return global;
    }


    public String getDefaultIdBasedir(NutsId id) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return plainIdPath;
        }
        String version = id.getVersion().getValue();
//        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
//        if (a != null) {
//            x += "/" + a;
//        }
        return x;
    }

    @Override
    public String getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        String storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id)).toString();
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    @Override
    public String getStoreLocation(NutsStoreLocation storeLocation) {
        return storeLocations.get(storeLocation.id());
    }


    @Override
    public String getHomeLocation(NutsOsFamily osFamily, NutsStoreLocation storeLocation) {
        return new NutsHomeLocationsMap(homeLocations).get(osFamily, storeLocation);
    }


    @Override
    public String getHomeLocation(NutsStoreLocation storeLocation) {
        return NutsUtilPlatforms.getPlatformHomeFolder(getStoreLocationLayout(),
                storeLocation, getHomeLocations(),
                isGlobal(),
                getName()
        );
    }

}
