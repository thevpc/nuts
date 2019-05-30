/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsId;
import net.vpc.app.nuts.core.util.common.TraceResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.util.FilesFoldersApi;
import net.vpc.app.nuts.core.util.RemoteRepoApi;
import net.vpc.app.nuts.core.util.common.IteratorUtils;
import net.vpc.app.nuts.core.util.io.CommonRootsHelper;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.core.bridges.maven.mvnutil.MavenMetadata;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends AbstractMavenRepository {

    private static final Logger LOG = Logger.getLogger(MavenRemoteRepository.class.getName());
    private MvnClient wrapper;

    private RemoteRepoApi versionApi = RemoteRepoApi.DEFAULT;
    private RemoteRepoApi findApi = RemoteRepoApi.DEFAULT;

    private final FilesFoldersApi.IteratorModel findModel = new FilesFoldersApi.IteratorModel() {
        @Override
        public void undeploy(NutsId id, NutsRepositorySession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(getWorkspace(),"Not supported undeploy.");
        }

        @Override
        public boolean isDescFile(String pathname) {
            return pathname.endsWith(".pom");
        }

        @Override
        public NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsRepositorySession session) throws IOException {
            return MavenUtils.parsePomXml(in, getWorkspace(), session, pathname);
        }
    };

    public MavenRemoteRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, NutsConstants.RepoTypes.MAVEN);
    }

    protected MavenRemoteRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository, String repoType) {
        super(options, workspace, parentRepository, SPEED_SLOW, repoType);
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        if(session.getFetchMode()!=NutsFetchMode.REMOTE){
            throw new NutsNotFoundException(getWorkspace(),id);
        }
        return super.fetchDescriptorImpl(id,session);
    }

    @Override
    public Iterator<NutsId> searchVersionsImpl(final NutsId id, NutsIdFilter idFilter, final NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroup();
            String artifactId = id.getName();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.setFaceDescriptor())
            );

            try (InputStream metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.QueryFaces.CATALOG), session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException ex) {
                //ko not found
            }
            return ret.iterator();
        }
        switch (versionApi) {
            case DEFAULT:
            case MAVEN: {
                return findVersionsImplMetadataXml(id, idFilter, session);
            }
            case GITHUB: {
                return findVersionsImplGithub(id, idFilter, session);
            }
            case FILES_FOLDERS: {
                return findVersionsImplFilesFolders(id, idFilter, session);
            }
            case UNSUPPORTED: {
                return Collections.emptyIterator();
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(),String.valueOf(versionApi));
            }
        }
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, session);
        }
        String location = config().getLocation(true);
        String[] all = location.split("/+");
        String userName = all[2];
        String repo = all[3];
        String apiUrlBase = "https://api.github.com/repos/" + userName + "/" + repo + "/contents";
        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(apiUrlBase, groupId.replace('.', '/') + "/" + artifactId);

            try {
                metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.QueryFaces.CATALOG), session).open();
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(getWorkspace(),id, ex);
            }
            List<Map<String, Object>> info = getWorkspace().io().json().read(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.setVersion(versionName);

                        if (idFilter != null && !idFilter.accept(nutsId, getWorkspace(), session.getSession())) {
                            continue;
                        }
                        ret.add(
                                new DefaultNutsId(
                                        null,
                                        groupId,
                                        artifactId,
                                        versionName,
                                        ""
                                )
                        );
                    }

                }
            }
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return ret.iterator();
    }

    public Iterator<NutsId> findVersionsImplMetadataXml(final NutsId id, NutsIdFilter idFilter, final NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, session);
        }
        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

            try {
                metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.QueryFaces.CATALOG), session).open();
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(getWorkspace(),id, ex);
            }
            MavenMetadata info = MavenUtils.parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    final NutsId nutsId = id.setVersion(version);

                    if (idFilter != null && !idFilter.accept(nutsId, getWorkspace(), session.getSession())) {
                        continue;
                    }
                    ret.add(
                            new DefaultNutsId(
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    ""
                            )
                    );
                }
            }
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return ret.iterator();

    }

    public Iterator<NutsId> findSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, final NutsRepositorySession session) {
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroup();
            String artifactId = id.getName();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.setFaceDescriptor())
            );

            try (InputStream metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.QueryFaces.CATALOG), session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException ex) {
                //ko not found
            }
            return ret.iterator();
        } else {
            throw new NutsIllegalArgumentException(getWorkspace(), "Expected single version in " + id);
        }
    }

    public Iterator<NutsId> findVersionsImplFilesFolders(final NutsId id, NutsIdFilter idFilter, final NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, session);
        }
        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream foldersFileStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String foldersFileUrl = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/.folders");
            String[] foldersFileContent = null;
            try {
                foldersFileStream = openStream(id, foldersFileUrl, id.setFace(NutsConstants.QueryFaces.CATALOG), session).open();
                foldersFileContent = CoreIOUtils.loadString(foldersFileStream, true).split("(\n|\r)+");
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(getWorkspace(),id, ex);
            }
            if (foldersFileContent != null) {
                for (String version : foldersFileContent) {
                    final NutsId nutsId = id.setVersion(version);

                    if (idFilter != null && !idFilter.accept(nutsId, getWorkspace(), session.getSession())) {
                        continue;
                    }
                    ret.add(
                            new DefaultNutsId(
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    ""
                            )
                    );
                }
            }
        } finally {
            if (foldersFileStream != null) {
                try {
                    foldersFileStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return ret.iterator();
    }

    @Override
    public Iterator<NutsId> searchImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        switch (findApi) {
            case DEFAULT:
            case FILES_FOLDERS:
            case GITHUB:
            case MAVEN: {
                List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);
                List<Iterator<NutsId>> li = new ArrayList<>();
                for (CommonRootsHelper.PathBase root : roots) {
                    int depth = root.isDeep() ? Integer.MAX_VALUE : 2;
                    li.add(FilesFoldersApi.createIterator(getWorkspace(), config().name(), config().getLocation(true), root.getName(), filter, session, depth, findModel));
                }
                return IteratorUtils.concat(li);
            }
//            case MAVEN: {
//                //TODO : this will find only in archetype, not in full index....
//                String url = CoreIOUtils.buildUrl(config().getLocation(true), "/archetype-catalog.xml");
//                try {
//                    InputSource s = CoreIOUtils.getCachedUrlWithSHA1(getWorkspace(), url, session.getSession());
//                    final InputStream is = getWorkspace().io().monitorInputStream(s.open(), session);
//                    return MavenUtils.createArchetypeCatalogIterator(is, filter, true);
//                } catch (UncheckedIOException ex) {
//                    return Collections.emptyIterator();
//                }
//            }
            case UNSUPPORTED: {
                return Collections.emptyIterator();
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(),String.valueOf(versionApi));
            }
        }
    }

    private NutsRepository getLocalMavenRepo() {
        for (NutsRepository nutsRepository : getWorkspace().config().getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.RepoTypes.MAVEN)
                    && nutsRepository.config().getLocation(true) != null
                    && nutsRepository.config().getLocation(true).equals(
                            getWorkspace().io().path(getWorkspace().io().expandPath("~/.m2")).toString()
                    )) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected Path getMavenLocalFolderContent(NutsId id) {
        String p = getIdRelativePath(id);
        if (p != null) {
            return getWorkspace().io().path(System.getProperty("user.home"), ".m2", p);
        }
        return null;
    }

    private MvnClient getWrapper() {
        if (true) {
            return null;
        }
        return new MvnClient(getWorkspace());
    }

    @Override
    public NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        if (wrapper == null) {
            wrapper = getWrapper();
        }
        if (wrapper != null && wrapper.get(id, config().getLocation(true), session.getSession())) {
            NutsRepository loc = getLocalMavenRepo();
            if (loc != null) {
                return loc.fetchContent()
                        .id(id)
                        .descriptor(descriptor)
                        .localPath(localPath)
                        .session(session.copy().setFetchMode(NutsFetchMode.LOCAL))
                        .run()
                        .getResult()
                        ;
            }
            //should be already downloaded to m2 folder
            Path content = getMavenLocalFolderContent(id);
            if (content != null && Files.exists(content)) {
                if (localPath == null) {
                    return new DefaultNutsContent(content, true, false);
                } else {
                    Path tempFile = getWorkspace().io().createTempFile(content.getFileName().toString(), this);
                    getWorkspace().io().copy().from(content).to(tempFile).safeCopy().run();
                    return new DefaultNutsContent(tempFile, true, false);
                }
            }
        }
        if (localPath == null) {
            String p = getIdPath(id);
            Path tempFile = getWorkspace().io().createTempFile(new File(p).getName(), this);
            try {
                getWorkspace().io().copy().from(getStream(id, session)).to(tempFile).validator(new NutsPathCopyAction.Validator() {
                    @Override
                    public void validate(Path path) {
                        try (InputStream in = Files.newInputStream(path)) {
                            checkSHA1Hash(id.setFace(NutsConstants.QueryFaces.COMPONENT_HASH), in, session);
                        } catch (IOException ex) {
                            return;
                        }
                    }
                }).run();
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(getWorkspace(),id, null, ex);
            }
            return new DefaultNutsContent(tempFile, false, true);
        } else {
            try {
                getWorkspace().io().copy().from(getStream(id, session)).to(localPath).validator(new NutsPathCopyAction.Validator() {
                    @Override
                    public void validate(Path path) {
                        try (InputStream in = Files.newInputStream(path)) {
                            checkSHA1Hash(id.setFace(NutsConstants.QueryFaces.COMPONENT_HASH), in, session);
                        } catch (IOException ex) {
                            throw new NutsPathCopyAction.ValidationException(ex);
                        }
                    }
                }).run();
            } catch (UncheckedIOException ex) {
                LOG.log(Level.SEVERE, id.toString() + " : " + ex.getMessage());
                throw new NutsNotFoundException(getWorkspace(),id, null, ex);
            }
            return new DefaultNutsContent(localPath, false, false);
        }
    }

//    protected String getPrivateStoreLocation() {
//        return System.getProperty("user.home") + "/.m2";
//    }
    @Override
    protected String getIdPath(NutsId id) {
        return getIdRemotePath(id);
    }

    @Override
    protected InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
        long startTime = System.currentTimeMillis();
        try {
            InputStream in = getWorkspace().io().monitor().source(path).origin(source).session(session).create();
            if (LOG.isLoggable(Level.FINEST)) {
                if (CoreIOUtils.isPathHttp(path)) {
                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading maven" : "Open local file";
                    message += " url=" + path;
                    traceMessage(session, id, TraceResult.SUCCESS, message, startTime);
                }
            }
            return CoreIOUtils.createInputSource(in);
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                if (CoreIOUtils.isPathHttp(path)) {
                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading maven" : "Open local file";
                    message += " url=" + path;
                    traceMessage(session, id, TraceResult.ERROR, message, startTime);
                }
            }
            throw ex;
        }
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsRepositorySession session) {
        super.checkAllowedFetch(id, session);
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(),id);
        }
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
}
