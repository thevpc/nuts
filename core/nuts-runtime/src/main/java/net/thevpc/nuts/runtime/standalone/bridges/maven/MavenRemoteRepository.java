/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.bundles.mvn.MavenMetadata;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repos.FilesFoldersApi;
import net.thevpc.nuts.runtime.standalone.repos.NutsCachedRepository;
import net.thevpc.nuts.runtime.standalone.repos.RemoteRepoApi;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends NutsCachedRepository {

    private final NutsLogger LOG;
    private final FilesFoldersApi.IteratorModel findModel = new FilesFoldersApi.AbstractIteratorModel() {
        @Override
        public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("not supported undeploy."));
        }

        @Override
        public boolean isDescFile(String pathname) {
            return pathname.endsWith(".pom");
        }

        @Override
        public NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, String rootURL) throws IOException {
            session.getTerminal().printProgress("%-8s %s", "parse", NutsPath.of(pathname,session).toCompressedForm());
            return MavenUtils.of(session).parsePomXmlAndResolveParents(in, fetchMode, pathname, repository);
        }

        @Override
        public NutsId parseId(String path, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
            String fn = CoreIOUtils.getURLName(path);
            if (fn.endsWith(".pom")) {
                String versionFolder = CoreIOUtils.getURLParentPath(path);
                if (versionFolder!=null) {
                    String vn = CoreIOUtils.getURLParentPath(versionFolder);
                    String artifactFolder = CoreIOUtils.getURLParentPath(versionFolder);
                    if (artifactFolder!=null) {
                        String an = CoreIOUtils.getURLName(artifactFolder);
                        if (fn.equals(an + "-" + vn + ".pom")) {
                            String groupFolder = CoreIOUtils.getURLParentPath(artifactFolder);
                            if (groupFolder!=null) {
                                String[] gg = CoreIOUtils.urlTrimFirstSlash(groupFolder.substring(rootPath.length())).split("/");
                                StringBuilder gn = new StringBuilder();
                                for (int i = 0; i < gg.length; i++) {
                                    String ns = gg[i];
                                    if (i > 0) {
                                        gn.append('.');
                                    }
                                    gn.append(ns);
                                }
                                return validate(
                                        NutsIdBuilder.of(session)
                                                .setGroupId(gn.toString())
                                                .setArtifactId(an)
                                                .setVersion(vn)
                                                .build(),
                                        null, path, rootPath, filter, repository, session);
                            }
                        }
                    }
                }
            }
            return null;
        }

    };
    private MvnClient wrapper;
    private RemoteRepoApi versionApi = RemoteRepoApi.DEFAULT;
    private RemoteRepoApi findApi = RemoteRepoApi.DEFAULT;
    private AbstractMavenRepositoryHelper helper = new AbstractMavenRepositoryHelper(this) {
        @Override
        protected String getIdPath(NutsId id, NutsSession session) {
            return getIdRemotePath(id, session);
        }

        @Override
        protected boolean exists(NutsId id, String path, Object source, String typeName, NutsSession session) {
            session.getTerminal().printProgress("%-8s %s", "search", NutsPath.of(path,session).toCompressedForm());
            try {
                try (InputStream s = NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source)
                        .setSourceTypeName(typeName).create()) {
                    //
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected InputStream openStream(NutsId id, String path, Object source, String typeName, String action, NutsSession session) {
            session.getTerminal().printProgress("%-8s %s", action, NutsPath.of(path,session).toCompressedForm());
            return NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(typeName).create();
        }

    };

    //    public MavenRemoteRepository(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository,RemoteRepoApi api) {
//        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.MAVEN);
//        LOG=workspace.log().of(MavenRemoteRepository.class);
//    }
    protected MavenRemoteRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository, String repoType) {
        super(options, session, parentRepository, NutsSpeedQualifier.SLOW, false, repoType);
        LOG = NutsLogger.of(MavenRemoteRepository.class,session);
        switch (repoType) {
            case "maven": {
                this.findApi = RemoteRepoApi.MAVEN;
                this.versionApi = RemoteRepoApi.MAVEN;
                break;
            }
            case "maven+dirtext": {
                this.findApi = RemoteRepoApi.DIR_TEXT;
                this.versionApi = RemoteRepoApi.DIR_TEXT;
                break;
            }
            case "maven+dirlist": {
                this.findApi = RemoteRepoApi.DIR_LIST;
                this.versionApi = RemoteRepoApi.DIR_LIST;
                break;
            }
            case "maven+github": {
                this.findApi = RemoteRepoApi.GITHUB;
                this.versionApi = RemoteRepoApi.GITHUB;
                break;
            }
            default: {
                throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("unsupported maven repo type: %s", repoType));
            }
        }
    }

    @Override
    public Iterator<NutsId> searchVersionsCore(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().setSession(session).getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );

            if (helper.exists(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session)) {
                // ok found!!
                ret.add(id);
            }
            return ret.iterator();
        }
        NutsIdFilter filter2 = NutsIdFilters.of(session).nonnull(idFilter).and(
                NutsIdFilters.of(session).byName(id.getShortName())
        );
        switch (versionApi) {
            case DEFAULT:
            case MAVEN: {
                return findVersionsImplMetadataXml(id, filter2, fetchMode, session);
            }
            case GITHUB: {
                return findVersionsImplGithub(id, filter2, fetchMode, session);
            }
            case DIR_TEXT: {
                return findVersionsImplFilesFolders(id, filter2, fetchMode, RemoteRepoApi.DIR_TEXT, session);
            }
            case DIR_LIST: {
                return findVersionsImplFilesFolders(id, filter2, fetchMode, RemoteRepoApi.DIR_LIST, session);
            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedEnumException(session, versionApi);
            }
        }
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        return helper.fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (wrapper == null) {
            wrapper = getWrapper(session);
        }
        NutsWorkspace ws = session.getWorkspace();
        if (wrapper != null && wrapper.get(id, config().getLocation(true), session)) {
            NutsRepository repo = getLocalMavenRepo(session);
            if (repo != null) {
                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
                return repoSPI.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setLocalPath(localPath)
                        .setSession(session)
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            Path content = getMavenLocalFolderContent(id, session);
            if (content != null && Files.exists(content)) {
                if (localPath == null) {
                    return new NutsDefaultContent(
                            NutsPath.of(content,session), true, false);
                } else {
                    String tempFile = NutsTmp.of(session)
                            .setRepositoryId(getUuid())
                            .createTempFile(content.getFileName().toString()).toString();
                    NutsCp.of(session)
                            .from(content).to(tempFile).setSafe(true).run();
                    return new NutsDefaultContent(
                            NutsPath.of(tempFile,session), true, false);
                }
            }
        }
        if (localPath == null) {
            String p = helper.getIdPath(id, session);
            String tempFile = NutsTmp.of(session)
                    .setRepositoryId(getUuid())
                    .createTempFile(new File(p).getName()).toString();
            try {
                NutsCp.of(session)
                        .from(helper.getStream(id, "artifact binaries", "retrieve", session)).to(tempFile).setValidator(new NutsIOCopyValidator() {
                            @Override
                            public void validate(InputStream in) throws IOException {
                                helper.checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact binaries", session);
                            }
                        }).run();
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, null, ex);
            }
            return new NutsDefaultContent(
                    NutsPath.of(tempFile,session), true, true);
        } else {
            try {
                NutsCp.of(session)
                        .from(helper.getStream(id, "artifact content", "retrieve", session)).to(localPath)
                        .setValidator(in -> helper.checkSHA1Hash(
                                        id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(),
                                        in, "artifact binaries", session
                                )
                        ).setLogProgress(true)
                        .run();
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, null, ex);
            }
            return new NutsDefaultContent(
                    NutsPath.of(localPath,session), true, false);
        }
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        NutsRepositoryConfigManager config = config().setSession(session);
        switch (findApi) {
            case DEFAULT:
            case DIR_TEXT:
            case GITHUB: {
                List<Iterator<? extends NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    session.getTerminal().printProgress("%-8s %s", "browse", NutsPath.of(root,session).toCompressedForm());
                    if (root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(session, this, config.getLocation(true), name, filter, RemoteRepoApi.DIR_TEXT, session, Integer.MAX_VALUE, findModel));
                    } else {
                        li.add(FilesFoldersApi.createIterator(session, this, config.getLocation(true), root, filter, RemoteRepoApi.DIR_TEXT, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
            case DIR_LIST: {
                List<Iterator<? extends NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    session.getTerminal().printProgress("%-8s %s", "browse", NutsPath.of(root,session).toCompressedForm());
                    if (root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(session, this, config.getLocation(true), name, filter, RemoteRepoApi.DIR_LIST, session, Integer.MAX_VALUE, findModel));
                    } else {
                        li.add(FilesFoldersApi.createIterator(session, this, config.getLocation(true), root, filter, RemoteRepoApi.DIR_LIST, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
            case MAVEN: {
                // this will find only in archetype, not in full index....
                String url = CoreIOUtils.buildUrl(config.getLocation(true), "/archetype-catalog.xml");
                try {
                    InputStream s = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog.xml",
                            true,
                            session
                    );
                    final InputStream is = NutsInputStreamMonitor.of(session).setSource(s).create();
                    return MavenUtils.of(session)
                            .createArchetypeCatalogIterator(is, filter, true, session);
                } catch (UncheckedIOException | NutsIOException ex) {
                    return IteratorUtils.emptyIterator();
                }
            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedEnumException(session, versionApi);
            }
        }
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return IteratorUtils.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String location = config().getLocation(true);
        String[] all = location.split("/+");
        String userName = all[2];
        String repo = all[3];
        String apiUrlBase = "https://api.github.com/repos/" + userName + "/" + repo + "/contents";
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(apiUrlBase, groupId.replace('.', '/') + "/" + artifactId);

            try {
                metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", "retrieve", session);
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, ex);
            }
            List<Map<String, Object>> info = NutsElements.of(session).json().parse(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.builder().setVersion(versionName).build();

                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(
                                NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(versionName).build()
                        );
                    }

                }
            }
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
                    throw new NutsIOException(session, e);
                }
            }
        }
        return ret.iterator();
    }

    public Iterator<NutsId> findVersionsImplMetadataXml(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

            try {
                metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", "retrieve", session);
            } catch (UncheckedIOException | NutsIOException ex) {
                return null;
            }
            MavenMetadata info = MavenUtils.of(session).parseMavenMetaData(metadataStream, session);
            if (info != null) {
                for (String version : info.getVersions()) {
                    final NutsId nutsId = id.builder().setVersion(version).build();

                    if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                        continue;
                    }
                    ret.add(
                            NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(version).build()
                    );
                }
            }
        } catch (UncheckedIOException | NutsIOException ex) {
            //unable to access
            return null;
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
//                    throw new NutsIOException(getWorkspace(),e);
                    return null;
                }
            }
        }
        return ret.iterator();

    }

    public Iterator<NutsId> findSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );

            if (helper.exists(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session)) {
                // ok found!!
                ret.add(id);
            }
            return ret.iterator();
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("expected single version in %s", id));
        }
    }

    public Iterator<NutsId> findVersionsImplFilesFolders(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, RemoteRepoApi versionApi, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return IteratorUtils.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        List<NutsId> ret = new ArrayList<>();
        String foldersFileUrl = CoreIOUtils.buildUrl(config().setSession(session).getLocation(true), groupId.replace('.', '/') + "/" + artifactId);
        FilesFoldersApi.Item[] all = FilesFoldersApi.getDirItems(true, false, versionApi, foldersFileUrl, session);

        if (all != null) {
            for (FilesFoldersApi.Item version : all) {
                final NutsId nutsId = id.builder().setVersion(version.getName()).build();

                if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                    continue;
                }
                ret.add(
                        NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(version.getName()).build()
                );
            }
        }
        return ret.iterator();
    }

    private NutsRepository getLocalMavenRepo(NutsSession session) {
        for (NutsRepository nutsRepository : session.repos().setSession(session).getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.RepoTypes.MAVEN)
                    && nutsRepository.config().getLocation(true) != null
                    && nutsRepository.config().getLocation(true).equals(
                    Paths.get(NutsPath.of("~/.m2",session).builder().withWorkspaceBaseDir().build().toString()).toString()
            )) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected Path getMavenLocalFolderContent(NutsId id, NutsSession session) {
        String p = getIdRelativePath(id, session);
        if (p != null) {
            return Paths.get(System.getProperty("user.home"), ".m2", p);
        }
        return null;
    }

    private MvnClient getWrapper(NutsSession session) {
        if (true) {
            return null;
        }
        return new MvnClient(session);
    }

    @Override
    protected String getIdExtension(NutsId id, NutsSession session) {
        return helper.getIdExtension(id, session);
    }

}
