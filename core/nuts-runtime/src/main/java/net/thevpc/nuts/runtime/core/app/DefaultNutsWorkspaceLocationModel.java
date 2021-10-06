package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.NutsHomeLocationsMap;
import net.thevpc.nuts.runtime.standalone.NutsStoreLocationsMap;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.nio.file.Paths;
import java.util.Map;

public class DefaultNutsWorkspaceLocationModel {
    private NutsWorkspace ws;
    private String workspaceLocation;
    private NutsWorkspaceInitInformation info;

    public DefaultNutsWorkspaceLocationModel(NutsWorkspace ws, NutsWorkspaceInitInformation info, String workspaceLocation) {
        this.ws = ws;
        this.info = info;
        this.workspaceLocation = workspaceLocation;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    private DefaultNutsWorkspaceConfigModel cfg() {
        return ((DefaultNutsWorkspace) ws).getConfigModel();
    }


    public void setHomeLocation(NutsHomeLocation homeType, String location, NutsSession session) {
//        if (homeType == null) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid store root folder null"));
//        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("home-location", session);
        cfg().getStoreModelBoot().setHomeLocations(new NutsHomeLocationsMap(cfg().getStoreModelBoot().getHomeLocations()).set(homeType, location).toMapOrNull());
        cfg().onPostUpdateConfig("home-location", session);
    }


    public String getWorkspaceLocation() {
        return workspaceLocation;
    }


    public String getHomeLocation(NutsStoreLocation folderType, NutsSession session) {
        return cfg().current().getHomeLocation(folderType);
    }


    public String getStoreLocation(NutsStoreLocation folderType, NutsSession session) {
        try {
            return cfg().current().getStoreLocation(folderType);
        } catch (IllegalStateException stillInitializing) {
            return info.getStoreLocation(folderType);
        }
    }


    public void setStoreLocation(NutsStoreLocation folderType, String location, NutsSession session) {
        if (folderType == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid store root folder null"));
        }
//        options = CoreNutsUtils.validate(options, ws);
        cfg().onPreUpdateConfig("store-location", session);
        cfg().getStoreModelBoot().setStoreLocations(new NutsStoreLocationsMap(cfg().getStoreModelBoot().getStoreLocations()).set(folderType, location).toMapOrNull());
        cfg().onPostUpdateConfig("store-location", session);
    }


    public void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsSession session) {
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.EXPLODED;
        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-location-strategy", session);
        cfg().getStoreModelBoot().setStoreLocationStrategy(strategy);
        cfg().onPostUpdateConfig("store-location-strategy", session);
    }


    public void setStoreLocationLayout(NutsOsFamily layout, NutsSession session) {
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-location-layout", session);
        cfg().getStoreModelBoot().setStoreLocationLayout(layout);
        cfg().onPostUpdateConfig("store-location-layout", session);
    }


    public String getStoreLocation(NutsStoreLocation folderType, String repositoryIdOrName, NutsSession session) {
        if (repositoryIdOrName == null) {
            return getStoreLocation(folderType, session);
        }
        NutsRepository repositoryById = ws.repos().setSession(session).getRepository(repositoryIdOrName);
        NutsRepositorySPI nutsRepositorySPI = NutsWorkspaceUtils.of(session).repoSPI(repositoryById);
        return nutsRepositorySPI.config().getStoreLocation(folderType);
    }


    public String getStoreLocation(NutsId id, NutsStoreLocation folderType, String repositoryIdOrName, NutsSession session) {
        if (repositoryIdOrName == null) {
            return getStoreLocation(id, folderType, session);
        }
        String storeLocation = getStoreLocation(folderType, repositoryIdOrName, session);
        return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id, session)).toString();
    }


    public String getStoreLocation(NutsId id, NutsStoreLocation folderType, NutsSession session) {
        String storeLocation = getStoreLocation(folderType, session);
        if (storeLocation == null) {
            return null;
        }
        return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id, session)).toString();
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy(NutsSession session) {
        return cfg().current().getStoreLocationStrategy();
    }


    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy(NutsSession session) {
        return cfg().current().getRepositoryStoreLocationStrategy();
    }


    public NutsOsFamily getStoreLocationLayout(NutsSession session) {
        return cfg().current().getStoreLocationLayout();
    }


    public Map<NutsStoreLocation, String> getStoreLocations(NutsSession session) {
        return cfg().current().getStoreLocations();
    }


    public Map<NutsHomeLocation, String> getHomeLocations(NutsSession session) {
        return cfg().current().getHomeLocations();
    }


    public String getHomeLocation(NutsHomeLocation location, NutsSession session) {
        return cfg().current().getHomeLocation(location);
    }


    public String getDefaultIdBasedir(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkShortId(id);
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


    public String getDefaultIdFilename(NutsId id, NutsSession session) {
        String classifier = "";
        String ext = getDefaultIdExtension(id, session);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NutsBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }


    public String getDefaultIdContentExtension(String packaging, NutsSession session) {
        if (NutsBlankable.isBlank(packaging)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported empty packaging"));
        }
        switch (packaging) {
            case "jar":
            case "bundle":
            case "nuts-extension":
            case "maven-archetype":
            case "maven-plugin":
            case "ejb-client":
            case "test-jar":
            case "ejb":
            case "java-source":
            case "javadoc":
            case "eclipse-plugin":
                return ".jar";
            case "dll":
            case "so":
            case "jnilib":
                return "-natives.jar";
            case "war":
                return ".war";
            case "ear":
                return ".ear";
            case "pom":
                return ".pom";
            case "nuts":
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            case "rar":
                return ".rar";
            case "zip":
            case "nbm-application":
                return ".zip";
        }
        return "." + packaging;
    }


    public String getDefaultIdExtension(NutsId id, NutsSession session) {
        Map<String, String> q = id.getProperties();
        String f = NutsUtilStrings.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.CONTENT_HASH: {
                return getDefaultIdExtension(id.builder().setFaceContent().build(), session) + ".sha1";
            }
            case NutsConstants.QueryFaces.CONTENT: {
                return getDefaultIdContentExtension(q.get(NutsConstants.IdProperties.PACKAGING), session);
            }
            default: {
                if (f.equals("cache") || f.endsWith(".cache")) {
                    return "." + f;
                }
                if (NutsBlankable.isBlank(f)) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing face in %s", id));
                }
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported face %s in %s", f, id));
            }
        }
    }

}
