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
package net.vpc.app.nuts.core.repos;

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
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.DefaultNutsId;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.NutsPatternIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.core.util.FilesFoldersApi;
import net.vpc.app.nuts.core.util.RemoteRepoApi;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.IteratorUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.io.CommonRootsHelper;
import net.vpc.app.nuts.core.util.io.InputSource;

public class NutsHttpFolderRepository extends NutsCachedRepository {

    private static final Logger LOG = Logger.getLogger(NutsHttpFolderRepository.class.getName());

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

    private boolean isDescFile0(String pathname) {
        return pathname.equals(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith("/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
    }

    public NutsHttpFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
    }

    protected InputStream getDescStream(NutsId id, NutsRepositorySession session) {
        String url = getDescPath(id);
        if (CoreIOUtils.isPathHttp(url)) {
            String message = "Downloading maven";//: "Open local file";
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, CoreStringUtils.alignLeft(config().getName(), 20) + " " + message + " url " + url);
            }
        }
        return openStream(url, id, session);
    }

    protected String getPath(NutsId id) {
        return getIdRemotePath(id);
    }

    protected String getDescPath(NutsId id) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
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
            return null;
        }
    }

    protected InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
        long startTime = System.currentTimeMillis();
        try {
            InputStream in = getWorkspace().io().monitor().source(path).origin(source).session(session.getSession()).create();
            if (LOG.isLoggable(Level.FINEST)) {
                if (CoreIOUtils.isPathHttp(path)) {
                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading" : "Open local file";
                    message += " url=" + path;
                    traceMessage(session, id, TraceResult.SUCCESS, message, startTime);
                }
            }
            return CoreIOUtils.createInputSource(in);
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                if (CoreIOUtils.isPathHttp(path)) {
                    String message = CoreIOUtils.isPathHttp(path) ? "Downloading" : "Open local file";
                    message += " url=" + path;
                    traceMessage(session, id, TraceResult.ERROR, message, startTime);
                }
            }
            throw ex;
        }
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
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
                throw new NutsNotFoundException(getWorkspace(), id, ex);
            }
            List<Map<String, Object>> info = getWorkspace().json().parse(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.setVersion(versionName);

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

        String groupId = id.getGroup();
        String artifactId = id.getName();
        try {
            String artifactUrl = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId);
            String[] all = FilesFoldersApi.getFolders(artifactUrl, session.getSession());
            List<NutsId> n = new ArrayList<>();
            if (all != null) {
                for (String s : all) {
                    if (!DefaultNutsVersion.isBlank(s)) {
                        String versionFilesUrl = artifactUrl + "/" + s;
                        String[] versionFiles = FilesFoldersApi.getFiles(versionFilesUrl, session.getSession());
                        boolean validVersion = false;
                        for (String v : versionFiles) {
                            if ("nuts.properties".equals(v)) {
                                validVersion = true;
                                break;
                            }
                        }
                        if (validVersion) {
                            NutsId id2 = id.builder().setVersion(s).build();
                            if (idFilter == null || idFilter.accept(id2, session.getSession())) {
                                n.add(id2);
                            }
                        }
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
            NutsIdFilter filter2 = new NutsIdFilterAnd(idFilter,
                    new NutsPatternIdFilter(id.getSimpleNameId())
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
    public Iterator<NutsId> searchImpl2(final NutsIdFilter filter, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return null;
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
            return new DefaultNutsContent(localFile, false, false);
        } else {
            for (String location : descriptor.getLocations()) {
                try {
                    getWorkspace().io().copy().session(session.getSession()).from(location).to(localFile).safeCopy().monitorable().run();
                    return new DefaultNutsContent(localFile, false, false);
                } catch (Exception ex) {
                    //ignore!!
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
