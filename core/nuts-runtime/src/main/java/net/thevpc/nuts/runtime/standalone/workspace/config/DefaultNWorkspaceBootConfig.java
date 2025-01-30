package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultNWorkspaceBootConfig implements NWorkspaceBootConfig {
    private String bootPath;
    private String effectiveWorkspace;
    private String effectiveWorkspaceName;
    private boolean immediateLocation;

    private String uuid = null;
    private boolean system;
    private String name = null;
    private String workspace = null;
    private String bootRepositories = null;

    private Map<NStoreType, String> storeLocations = null;
    private Map<NHomeLocation, String> homeLocations = null;

    private NStoreStrategy repositoryStoreStrategy = null;
    private NStoreStrategy storeStrategy = null;
    private NOsFamily storeLayout = null;

    private List<Extension> extensions;

    public DefaultNWorkspaceBootConfig(String bootPath, String effectiveWorkspace, String effectiveWorkspaceName, boolean immediateLocation, NWorkspaceConfigBoot bootModel) {
        this.bootPath = bootPath;
        this.effectiveWorkspace = effectiveWorkspace;
        this.immediateLocation = immediateLocation;
        this.effectiveWorkspaceName = effectiveWorkspaceName;
        this.uuid = bootModel.getUuid();
        this.system = bootModel.isSystem();
        this.name = bootModel.getName();
        this.workspace = bootModel.getWorkspace();
        this.bootRepositories = bootModel.getBootRepositories();
        this.storeLocations = bootModel.getStoreLocations()==null?new HashMap<>() : new HashMap<>(bootModel.getStoreLocations());
        this.homeLocations = bootModel.getHomeLocations()==null?new HashMap<>() : new HashMap<>(bootModel.getHomeLocations());
        this.repositoryStoreStrategy = bootModel.getRepositoryStoreStrategy();
        this.storeStrategy = bootModel.getStoreStrategy();
        this.storeLayout = bootModel.getStoreLayout();
        if (storeStrategy == null) {
            storeStrategy = NStoreStrategy.EXPLODED;
        }
        this.storeLocations= NPlatformHome.of(null,system).buildLocations(storeStrategy, storeLocations, homeLocations, effectiveWorkspace);
        List<NWorkspaceConfigBoot.ExtensionConfig> extensions = bootModel.getExtensions();
        if (extensions == null) {
            this.extensions= Collections.emptyList();
        }else {
            this.extensions=Collections.unmodifiableList(extensions.stream().map(x -> new NWorkspaceBootConfigExtensionImpl(x)).collect(Collectors.toList()));
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
    public Map<NStoreType, String> getStoreLocations() {
        return Collections.unmodifiableMap(storeLocations);
    }

    @Override
    public Map<NHomeLocation, String> getHomeLocations() {
        return Collections.unmodifiableMap(homeLocations);
    }


    @Override
    public NStoreStrategy getStoreStrategy() {
        return storeStrategy;
    }


    @Override
    public NOsFamily getStoreLayout() {
        return storeLayout;
    }


    @Override
    public NStoreStrategy getRepositoryStoreStrategy() {
        return repositoryStoreStrategy;
    }

    @Override
    public String getUuid() {
        return uuid;
    }


    @Override
    public boolean isSystem() {
        return system;
    }


    public String getDefaultIdBasedir(NId id) {
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
    public String getStoreLocation(NId id, NStoreType folderType) {
        String storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return Paths.get(storeLocation).resolve(NConstants.Folders.ID).resolve(getDefaultIdBasedir(id)).toString();
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    @Override
    public String getStoreLocation(NStoreType storeType) {
        return storeLocations.get(storeType);
    }


    @Override
    public String getHomeLocation(NHomeLocation homeLocation) {
        return new NHomeLocationsMap(homeLocations).get(homeLocation);
    }


    @Override
    public String getHomeLocation(NStoreType storeType) {
        return NPlatformHome.of(getStoreLayout(), isSystem()).getWorkspaceLocation(
                storeType, getHomeLocations(),
                getName()
        );
    }

}
