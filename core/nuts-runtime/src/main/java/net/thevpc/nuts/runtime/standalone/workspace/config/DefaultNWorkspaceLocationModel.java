package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.*;

import java.util.Map;

public class DefaultNWorkspaceLocationModel {
    private final NWorkspace workspace;
    private final NPath workspaceLocation;

    public DefaultNWorkspaceLocationModel(NWorkspace workspace, String workspaceLocation) {
        this.workspace = workspace;
        this.workspaceLocation = NPath.of(workspaceLocation);
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    private DefaultNWorkspaceConfigModel cfg() {
        return ((DefaultNWorkspace) workspace).getConfigModel();
    }


    public void setHomeLocation(NHomeLocation homeType, String location) {
//        if (homeType == null) {
//            throw new NutsIllegalArgumentException(session, NMsg.ofC("invalid store root folder null"));
//        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("home-location");
        cfg().getStoreModelBoot().setHomeLocations(new NHomeLocationsMap(cfg().getStoreModelBoot().getHomeLocations()).set(homeType, location).toMapOrNull());
        cfg().onPostUpdateConfig("home-location");
    }


    public NPath getWorkspaceLocation() {
        return workspaceLocation;
    }


    public NPath getHomeLocation(NStoreType folderType) {
        return cfg().current().getHomeLocation(folderType);
    }


    public NPath getStoreLocation(NStoreType folderType) {
        try {
            return cfg().current().getStoreLocation(folderType);
        } catch (IllegalStateException stillInitializing) {
            NWorkspaceOptions info = NWorkspaceExt.of().getModel().bootModel.getBootUserOptions();
            String h = info.getStoreType(folderType).orNull();
            NSession session = getWorkspace().currentSession();
            return h==null?null: NPath.of(h);
        }
    }


    public void setStoreLocation(NStoreType folderType, String location) {
        NSession session = getWorkspace().currentSession();
        NAssert.requireNonNull(folderType, "store root folder");
        cfg().onPreUpdateConfig("store-location");
        cfg().getStoreModelBoot().setStoreLocations(new NStoreLocationsMap(cfg().getStoreModelBoot().getStoreLocations()).set(folderType, location).toMapOrNull());
        cfg().onPostUpdateConfig("store-location");
    }


    public void setStoreStrategy(NStoreStrategy strategy) {
        if (strategy == null) {
            strategy = NStoreStrategy.EXPLODED;
        }
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-strategy");
        cfg().getStoreModelBoot().setStoreStrategy(strategy);
        cfg().onPostUpdateConfig("store-strategy");
    }


    public void setStoreLayout(NOsFamily layout) {
//        session = CoreNutsUtils.validate(session, ws);
        cfg().onPreUpdateConfig("store-layout");
        cfg().getStoreModelBoot().setStoreLayout(layout);
        cfg().onPostUpdateConfig("store-layout");
    }


    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName) {
        NSession session = getWorkspace().currentSession();
        if (repositoryIdOrName == null) {
            return getStoreLocation(folderType);
        }
        NRepository repositoryById = NRepositories.of().findRepository(repositoryIdOrName).get();
        NRepositorySPI nRepositorySPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(repositoryById);
        return nRepositorySPI.config().getStoreLocation(folderType);
    }


    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName) {
        NSession session = getWorkspace().currentSession();
        if (repositoryIdOrName == null) {
            return getStoreLocation(id, folderType);
        }
        NPath storeLocation = getStoreLocation(folderType, repositoryIdOrName);
        return storeLocation.resolve(NConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
    }


    public NPath getStoreLocation(NId id, NStoreType folderType) {
        NPath storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        NSession session = getWorkspace().currentSession();
        return storeLocation.resolve(NConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    public NStoreStrategy getStoreStrategy() {
        return cfg().current().getStoreStrategy();
    }


    public NStoreStrategy getRepositoryStoreStrategy() {
        return cfg().current().getRepositoryStoreStrategy();
    }


    public NOsFamily getStoreLayout() {
        return cfg().current().getStoreLayout();
    }


    public Map<NStoreType, String> getStoreLocations() {
        return cfg().current().getStoreLocations();
    }


    public Map<NHomeLocation, String> getHomeLocations() {
        return cfg().current().getHomeLocations();
    }


    public NPath getHomeLocation(NHomeLocation location) {
        return cfg().current().getHomeLocation(location);
    }


    public NPath getDefaultIdBasedir(NId id) {
        CoreNIdUtils.checkShortId(id);
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return NPath.of(plainIdPath);
        }
        String version = id.getVersion().getValue();
//        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
//        if (a != null) {
//            x += "/" + a;
//        }
        return NPath.of(x);
    }


    public String getDefaultIdFilename(NId id) {
        String classifier = "";
        NSession session = getWorkspace().currentSession();
        String ext = getDefaultIdExtension(id);
        if (!ext.equals(NConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }


    public String getDefaultIdContentExtension(String packaging) {
        NSession session = getWorkspace().currentSession();
        NAssert.requireNonBlank(packaging, "packaging");
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


    public String getDefaultIdExtension(NId id) {
        NSession session = getWorkspace().currentSession();
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
                return getDefaultIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                return getDefaultIdContentExtension(q.get(NConstants.IdProperties.PACKAGING));
            }
            default: {
                if (f.equals("cache") || f.endsWith(".cache")) {
                    return "." + f;
                }
                NAssert.requireNonBlank(f, ()-> NMsg.ofC("missing face in %s", id));
                throw new NIllegalArgumentException(NMsg.ofC("unsupported face %s in %s", f, id));
            }
        }
    }

}
