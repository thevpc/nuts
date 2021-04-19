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
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class NutsHttpFolderRepository extends NutsCachedRepository {

    private final NutsLogger LOG;

    private RemoteRepoApi versionApi = RemoteRepoApi.DEFAULT;
    private RemoteRepoApi findApi = RemoteRepoApi.DEFAULT;

    private FilesFoldersApi.IteratorModel findModel = new FilesFoldersApi.IteratorModel() {
        @Override
        public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(session, "Not supported undeploy.");
        }

        @Override
        public boolean isDescFile(String pathname) {
            return isDescFile0(pathname);
        }

        @Override
        public NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) throws IOException {
            try {
                return getWorkspace().descriptor().parser().setSession(session).parse(in);
            } finally {
                in.close();
            }
        }
    };

    public NutsHttpFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
        LOG = session.getWorkspace().log().of(NutsHttpFolderRepository.class);
    }

    private boolean isDescFile0(String pathname) {
        return pathname.equals(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith("/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
    }


    protected InputStream getDescStream(NutsId id, NutsSession session) {
        String url = getDescPath(id, session);
//        if (CoreIOUtils.isPathHttp(url)) {
//            String message = "Downloading maven";//: "Open local file";
//            if (LOG.isLoggable(Level.FINEST)) {
//                LOG.log(Level.FINEST, CoreStringUtils.alignLeft(config().getName(), 20) + " " + message + " url " + url);
//            }
//        }
        return openStream(url, id, "artifact descriptor", session);
    }

    protected String getPath(NutsId id, NutsSession session) {
        return getIdRemotePath(id, session);
    }

    protected String getDescPath(NutsId id, NutsSession session) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String version = id.getVersion().getValue();
        return (CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/"
                + NutsConstants.Files.DESCRIPTOR_FILE_NAME
        ));
    }

    protected InputStream openStream(String path, Object source, String sourceTypeName, NutsSession session) {
        return getWorkspace().io().monitor().setSource(path).setOrigin(source).setSourceTypeName(sourceTypeName).setSession(session).create();
    }

    protected NutsInput openStream(NutsId id, String path, Object source, String sourceTypeName, NutsSession session) {
        return getWorkspace().io().monitor().setSource(path).setOrigin(source).setSourceTypeName(sourceTypeName).setSession(session).createSource();
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsSession session) {
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
                metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open();
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, ex);
            }
            List<Map<String, Object>> info = getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.builder().setVersion(versionName).build();

                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(getWorkspace().id().builder()
                                .setGroupId(groupId)
                                .setArtifactId(artifactId)
                                .setVersion(versionName).build()
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

    public Iterator<NutsId> findVersionsImplFilesFolders(NutsId id, NutsIdFilter idFilter, RemoteRepoApi versionApi, NutsSession session) {

        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        try {
            String artifactUrl = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId);
            FilesFoldersApi.Item[] all = FilesFoldersApi.getDirItems(true, false, versionApi, artifactUrl, session);
            List<NutsId> n = new ArrayList<>();
            for (FilesFoldersApi.Item s : all) {
                if (s.isFolder() && s.getName().equals("LATEST")) {
                    continue;
                }
                String versionFilesUrl = artifactUrl + "/" + s.getName();
                FilesFoldersApi.Item[] versionFiles = FilesFoldersApi.getDirItems(false, true, versionApi, versionFilesUrl, session);
                boolean validVersion = false;
                for (FilesFoldersApi.Item v : versionFiles) {
                    if ("nuts.properties".equals(v.getName())) {
                        validVersion = true;
                        break;
                    }
                }
                if (validVersion) {
                    NutsId id2 = id.builder().setVersion(s.getName()).build();
                    if (idFilter == null || idFilter.acceptId(id2, session)) {
                        n.add(id2);
                    }
                }
            }
            return n.iterator();
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex).log("error find versions : {0}", ex);
//            return IteratorUtils.emptyIterator();
            return null;
        }

    }

    @Override
    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );

            try (InputStream metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException ex) {
                //ko not found
            }
            return ret.iterator();
        } else {
            NutsIdFilter filter2 = getWorkspace().id().filter().nonnull(idFilter).and(
                    getWorkspace().id().filter().byName(id.getShortName())
            );
            switch (versionApi) {
                case DEFAULT:
                case MAVEN:
                case DIR_TEXT: {
                    return findVersionsImplFilesFolders(id, filter2, RemoteRepoApi.DIR_TEXT, session);
                }
                case DIR_LIST: {
                    return findVersionsImplFilesFolders(id, filter2, RemoteRepoApi.DIR_LIST, session);
                }
                case GITHUB: {
                    return findVersionsImplGithub(id, filter2, session);
                }
                case UNSUPPORTED: {
                    return null;
                }
                default: {
                    throw new NutsUnsupportedArgumentException(session, String.valueOf(versionApi));
                }
            }
        }
    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        Iterator<NutsId> allVersions = searchVersionsCore(id, filter, fetchMode, session);
        NutsId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        try (InputStream stream = getDescStream(id, session)) {
            return getWorkspace().descriptor().parser().setSession(session).parse(stream);
        } catch (IOException | UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
        }
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localFile, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (descriptor.getLocations().length == 0) {
            String path = getPath(id, session);
            getWorkspace().io().copy().setSession(session).from(path).to(localFile).setSafe(true).setLogProgress(true).run();
            return new NutsDefaultContent(localFile, false, false);
        } else {
            for (NutsIdLocation location : descriptor.getLocations()) {
                if (CoreNutsUtils.acceptClassifier(location, id.getClassifier())) {
                    try {
                        getWorkspace().io().copy().setSession(session).from(location.getUrl()).to(localFile).setSafe(true).setLogProgress(true).run();
                        return new NutsDefaultContent(localFile, false, false);
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.SEVERE).error(ex).log("unable to download location for id {0} in location {1} : {2}", id, location.getUrl(), ex);
                    }
                }
            }
            return null;
        }
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        switch (findApi) {
            case DEFAULT:
            case DIR_TEXT:
            case GITHUB:
            case MAVEN: {
                List<Iterator<NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    if (root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), name, filter, RemoteRepoApi.DIR_TEXT, session, Integer.MAX_VALUE, findModel));
                    } else {
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), root, filter, RemoteRepoApi.DIR_TEXT, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
            case DIR_LIST: {
                List<Iterator<NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    if (root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), name, filter, RemoteRepoApi.DIR_LIST, session, Integer.MAX_VALUE, findModel));
                    } else {
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), root, filter, RemoteRepoApi.DIR_LIST, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedArgumentException(session, String.valueOf(versionApi));
            }
        }
    }
    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return true;
    }

}
