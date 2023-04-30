package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Map;

public class DefaultNWorkspaceLocationModel {
    private final NWorkspace ws;
    private final NPath workspaceLocation;

    public DefaultNWorkspaceLocationModel(NWorkspace ws, String workspaceLocation) {
        this.ws = ws;
        this.workspaceLocation = NPath.of(workspaceLocation, NSessionUtils.defaultSession(ws));
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

    private DefaultNWorkspaceConfigModel cfg() {
        return ((DefaultNWorkspace) ws).getConfigModel();
    }


    public void setHomeLocation(NHomeLocation homeType, String location, NSession session) {
//        if (homeType == null) {
//            throw new NutsIllegalArgumentException(session, NMsg.ofC("invalid store root folder null"));
//        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("home-location", session);
        cfg().getStoreModelBoot().setHomeLocations(new NHomeLocationsMap(cfg().getStoreModelBoot().getHomeLocations()).set(homeType, location).toMapOrNull());
        cfg().onPostUpdateConfig("home-location", session);
    }


    public NPath getWorkspaceLocation() {
        return workspaceLocation;
    }


    public NPath getHomeLocation(NStoreType folderType, NSession session) {
        return cfg().current().getHomeLocation(folderType,session);
    }


    public NPath getStoreLocation(NStoreType folderType, NSession session) {
        try {
            return cfg().current().getStoreLocation(folderType,session);
        } catch (IllegalStateException stillInitializing) {
            NWorkspaceOptions info = NWorkspaceExt.of(ws).getModel().bootModel.getBootUserOptions();
            String h = info.getStoreType(folderType).orNull();
            return h==null?null: NPath.of(h,session);
        }
    }


    public void setStoreLocation(NStoreType folderType, String location, NSession session) {
        NAssert.requireNonNull(folderType, "store root folder", session);
        cfg().onPreUpdateConfig("store-location", session);
        cfg().getStoreModelBoot().setStoreLocations(new NStoreLocationsMap(cfg().getStoreModelBoot().getStoreLocations()).set(folderType, location).toMapOrNull());
        cfg().onPostUpdateConfig("store-location", session);
    }


    public void setStoreStrategy(NStoreStrategy strategy, NSession session) {
        if (strategy == null) {
            strategy = NStoreStrategy.EXPLODED;
        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-strategy", session);
        cfg().getStoreModelBoot().setStoreStrategy(strategy);
        cfg().onPostUpdateConfig("store-strategy", session);
    }


    public void setStoreLayout(NOsFamily layout, NSession session) {
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-layout", session);
        cfg().getStoreModelBoot().setStoreLayout(layout);
        cfg().onPostUpdateConfig("store-layout", session);
    }


    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName, NSession session) {
        if (repositoryIdOrName == null) {
            return getStoreLocation(folderType, session);
        }
        NRepository repositoryById = NRepositories.of(session).setSession(session).findRepository(repositoryIdOrName).get();
        NRepositorySPI nRepositorySPI = NWorkspaceUtils.of(session).repoSPI(repositoryById);
        return nRepositorySPI.config().getStoreLocation(folderType);
    }


    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName, NSession session) {
        if (repositoryIdOrName == null) {
            return getStoreLocation(id, folderType, session);
        }
        NPath storeLocation = getStoreLocation(folderType, repositoryIdOrName, session);
        return storeLocation.resolve(NConstants.Folders.ID).resolve(getDefaultIdBasedir(id, session));
    }


    public NPath getStoreLocation(NId id, NStoreType folderType, NSession session) {
        NPath storeLocation = getStoreLocation(folderType, session);
        if (storeLocation == null) {
            return null;
        }
        return storeLocation.resolve(NConstants.Folders.ID).resolve(getDefaultIdBasedir(id, session));
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    public NStoreStrategy getStoreStrategy(NSession session) {
        return cfg().current().getStoreStrategy();
    }


    public NStoreStrategy getRepositoryStoreStrategy(NSession session) {
        return cfg().current().getRepositoryStoreStrategy();
    }


    public NOsFamily getStoreLayout(NSession session) {
        return cfg().current().getStoreLayout();
    }


    public Map<NStoreType, String> getStoreLocations(NSession session) {
        return cfg().current().getStoreLocations();
    }


    public Map<NHomeLocation, String> getHomeLocations(NSession session) {
        return cfg().current().getHomeLocations();
    }


    public NPath getHomeLocation(NHomeLocation location, NSession session) {
        return cfg().current().getHomeLocation(location,session);
    }


    public NPath getDefaultIdBasedir(NId id, NSession session) {
        CoreNIdUtils.checkShortId(id,session);
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return NPath.of(plainIdPath,session);
        }
        String version = id.getVersion().getValue();
//        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
//        if (a != null) {
//            x += "/" + a;
//        }
        return NPath.of(x,session);
    }


    public String getDefaultIdFilename(NId id, NSession session) {
        String classifier = "";
        String ext = getDefaultIdExtension(id, session);
        if (!ext.equals(NConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }


    public String getDefaultIdContentExtension(String packaging, NSession session) {
        NAssert.requireNonBlank(packaging, "packaging", session);
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
                return NConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            case "rar":
                return ".rar";
            case "zip":
            case "nbm-application":
                return ".zip";
            case "gz":
                return ".gz";
            case "targz":
            case "tar.gz":
                return ".tar.gz";
        }
        return "." + packaging;
    }


    public String getDefaultIdExtension(NId id, NSession session) {
        Map<String, String> q = id.getProperties();
        String f = NStringUtils.trim(q.get(NConstants.IdProperties.FACE));
        switch (f) {
            case NConstants.QueryFaces.DESCRIPTOR: {
                return NConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            }
            case NConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NConstants.QueryFaces.CONTENT_HASH: {
                return getDefaultIdExtension(id.builder().setFaceContent().build(), session) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                return getDefaultIdContentExtension(q.get(NConstants.IdProperties.PACKAGING), session);
            }
            default: {
                if (f.equals("cache") || f.endsWith(".cache")) {
                    return "." + f;
                }
                NAssert.requireNonBlank(f, ()-> NMsg.ofC("missing face in %s", id), session);
                throw new NIllegalArgumentException(session, NMsg.ofC("unsupported face %s in %s", f, id));
            }
        }
    }

}
