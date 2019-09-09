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
package net.vpc.app.nuts.core.impl.def.repos;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.io.FilesFoldersApi;
import net.vpc.app.nuts.core.util.RemoteRepoApi;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.io.InputSource;

public class NutsHttpFolderRepository extends NutsCachedRepository {

    private final NutsLogger LOG;

    private RemoteRepoApi versionApi = RemoteRepoApi.DEFAULT;
    private RemoteRepoApi findApi = RemoteRepoApi.DEFAULT;

    private FilesFoldersApi.IteratorModel findModel = new FilesFoldersApi.IteratorModel() {
        @Override
        public void undeploy(NutsId id, NutsRepositorySession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(getWorkspace(), "Not supported undeploy.");
        }

        @Override
        public boolean isDescFile(String pathname) {
            return isDescFile0(pathname);
        }

        @Override
        public NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsRepositorySession session) throws IOException {
            try {
                return getWorkspace().descriptor().parse(in);
            } finally {
                in.close();
            }
        }
    };

    public NutsHttpFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
        LOG=workspace.log().of(NutsHttpFolderRepository.class);
    }

    private boolean isDescFile0(String pathname) {
        return pathname.equals(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith("/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
    }


    protected InputStream getDescStream(NutsId id, NutsRepositorySession session) {
        String url = getDescPath(id);
//        if (CoreIOUtils.isPathHttp(url)) {
//            String message = "Downloading maven";//: "Open local file";
//            if (LOG.isLoggable(Level.FINEST)) {
//                LOG.log(Level.FINEST, CoreStringUtils.alignLeft(config().getName(), 20) + " " + message + " url " + url);
//            }
//        }
        return openStream(url, id, session);
    }

    protected String getPath(NutsId id) {
        return getIdRemotePath(id);
    }

    protected String getDescPath(NutsId id) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String version = id.getVersion().getValue();
        return (CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/"
                + NutsConstants.Files.DESCRIPTOR_FILE_NAME
        ));
    }

    protected InputStream openStream(String path, Object source, NutsRepositorySession session) {
        return getWorkspace().io().monitor().source(path).origin(source).session(session.getSession()).create();
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl2(NutsId id, NutsRepositorySession session) {
        try (InputStream stream = getDescStream(id, session)) {
            return getWorkspace().descriptor().parse(stream);
        } catch (IOException ex) {
            throw new NutsNotFoundException(getWorkspace(),id,ex);
        } catch (UncheckedIOException ex) {
            throw new NutsNotFoundException(getWorkspace(),id,ex);
        }
    }

    protected InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
//        long startTime = System.currentTimeMillis();
//        try {
            InputStream in = getWorkspace().io().monitor().source(path).origin(source).session(session.getSession()).create();
//            if (LOG.isLoggable(Level.FINER)) {
//                if (CoreIOUtils.isPathHttp(path)) {
//                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading" : "Open local file";
//                    message += " url=" + path;
//                    traceMessage(session, Level.FINER, id, TraceResult.SUCCESS, message, startTime,null);
//                }
//            }
            return CoreIOUtils.createInputSource(in);
//        } catch (RuntimeException ex) {
//            if (LOG.isLoggable(Level.FINEST)) {
//                if (CoreIOUtils.isPathHttp(path)) {
//                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading" : "Open local file";
//                    message += " url=" + path;
//                    traceMessage(session,Level.FINEST, id, TraceResult.FAIL, message, startTime,ex.getMessage());
//                }
//            }
//            throw ex;
//        }
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
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
                metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), session).open();
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(getWorkspace(), id, ex);
            }
            List<Map<String, Object>> info = getWorkspace().json().parse(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.builder().setVersion(versionName).build();

                        if (idFilter != null && !idFilter.accept(nutsId, session.getSession())) {
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

    public Iterator<NutsId> findVersionsImplFilesFolders(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        try {
            String artifactUrl = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId);
            FilesFoldersApi.Item[] all = FilesFoldersApi.getFilesAndFolders(false,true,artifactUrl, session.getSession());
            List<NutsId> n = new ArrayList<>();
            for (FilesFoldersApi.Item s : all) {
                String versionFilesUrl = artifactUrl + "/" + s.getName();
                FilesFoldersApi.Item[] versionFiles = FilesFoldersApi.getFilesAndFolders(true,false,versionFilesUrl, session.getSession());
                boolean validVersion = false;
                for (FilesFoldersApi.Item v : versionFiles) {
                    if ("nuts.properties".equals(v.getName())) {
                        validVersion = true;
                        break;
                    }
                }
                if (validVersion) {
                    NutsId id2 = id.builder().setVersion(s.getName()).build();
                    if (idFilter == null || idFilter.accept(id2, session.getSession())) {
                        n.add(id2);
                    }
                }
            }
            return n.iterator();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error Find Versions : " + ex.toString(), ex);
//            return IteratorUtils.emptyIterator();
            return null;
        }

    }

    @Override
    public Iterator<NutsId> searchVersionsImpl2(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build())
            );

            try (InputStream metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException ex) {
                //ko not found
            }
            return ret.iterator();
        } else {
            NutsIdFilter filter2 = new NutsIdFilterAnd(idFilter,
                    new NutsPatternIdFilter(id.getShortNameId())
            ).simplify();
            switch (versionApi) {
                case DEFAULT:
                case MAVEN:
                case FILES_FOLDERS: {
                    return findVersionsImplFilesFolders(id, filter2, session);
                }
                case GITHUB: {
                    return findVersionsImplGithub(id, filter2, session);
                }
                case UNSUPPORTED: {
                    return null;
                }
                default: {
                    throw new NutsUnsupportedArgumentException(getWorkspace(), String.valueOf(versionApi));
                }
            }
        }
    }

    @Override
    public Iterator<NutsId> searchImpl2(final NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return null;
        }
        switch (findApi) {
            case DEFAULT:
            case FILES_FOLDERS:
            case GITHUB:
            case MAVEN: {
                List<Iterator<NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    if(root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), config().name(), config().getLocation(true), name, filter, session, Integer.MAX_VALUE, findModel));
                    }else{
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), config().name(), config().getLocation(true), root, filter, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(), String.valueOf(versionApi));
            }
        }
    }

    @Override
    public NutsContent fetchContentImpl2(NutsId id, NutsDescriptor descriptor, Path localFile, NutsRepositorySession session) {
        if (descriptor.getLocations().length == 0) {
            String path = getPath(id);
            getWorkspace().io().copy().session(session.getSession()).from(path).to(localFile).safeCopy().monitorable().run();
            return new NutsDefaultContent(localFile, false, false);
        } else {
            for (NutsIdLocation location : descriptor.getLocations()) {
                if(CoreNutsUtils.acceptClassifier(location,id.getClassifier())) {
                    try {
                        getWorkspace().io().copy().session(session.getSession()).from(location.getUrl()).to(localFile).safeCopy().monitorable().run();
                        return new NutsDefaultContent(localFile, false, false);
                    } catch (Exception ex) {
                        LOG.log(Level.FINE,"Unable to download location for id "+id+" : "+location.getUrl(),ex);
                    }
                }
            }
            return null;
        }
    }

    @Override
    public NutsId searchLatestVersion2(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        Iterator<NutsId> allVersions = searchVersionsImpl2(id, filter, session);
        NutsId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

}
