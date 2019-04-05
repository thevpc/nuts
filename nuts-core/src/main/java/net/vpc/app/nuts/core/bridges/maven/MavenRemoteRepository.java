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
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.TraceResult;
import net.vpc.common.io.URLUtils;
import net.vpc.common.mvn.MavenMetadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends AbstractMavenRepository {

    private static final Logger log = Logger.getLogger(MavenRemoteRepository.class.getName());
    private MvnClient wrapper;

    public MavenRemoteRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW);
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(final NutsId id, NutsIdFilter idFilter, final NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }

        //maven-metadata.xml
        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = URLUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

            try {
                metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.FACE_CATALOG), session);
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(id, ex);
            }
            MavenMetadata info = MavenUtils.parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    final NutsId nutsId = id.setVersion(version);

                    if (idFilter != null && !idFilter.accept(nutsId)) {
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

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return Collections.emptyIterator();
        }
        String url = URLUtils.buildUrl(config().getLocation(true), "/archetype-catalog.xml");
        try {
            InputStream s = openStream(null, url, CoreNutsUtils.parseNutsId("internal:repository").setQueryProperty("location", config().getLocation(true)).setFace(NutsConstants.FACE_CATALOG), session);
            return MavenUtils.createArchetypeCatalogIterator(s, filter, true);
        } catch (UncheckedIOException ex) {
            return Collections.emptyIterator();
        }
    }

    private NutsRepository getLocalMavenRepo() {
        for (NutsRepository nutsRepository : getWorkspace().config().getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_MAVEN)
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
    protected NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        if (wrapper == null) {
            wrapper = getWrapper();
        }
        if (wrapper != null && wrapper.get(id, config().getLocation(true), session.getSession())) {
            NutsRepository loc = getLocalMavenRepo();
            if (loc != null) {
                return loc.fetchContent(id, localPath, session.copy().setFetchMode(NutsFetchMode.LOCAL));
            }
            //should be already downloaded to m2 folder
            Path content = getMavenLocalFolderContent(id);
            if (content != null && Files.exists(content)) {
                if (localPath == null) {
                    return new NutsContent(content, true, false);
                } else {
                    Path tempFile = getWorkspace().io().createTempFile(content.getFileName().toString(), this);
                    getWorkspace().io().copy().from(content).to(tempFile).safeCopy().run();
                    return new NutsContent(tempFile, true, false);
                }
            }
        }
        if (localPath == null) {
            String p = getIdPath(id);
            Path tempFile = getWorkspace().io().createTempFile(new File(p).getName(), this);
            try {
                getWorkspace().io().copy().from(getStream(id, session)).to(tempFile).check(new NutsIOCopyAction.Checker() {
                    @Override
                    public void check(Path path) {
                        try (InputStream in = Files.newInputStream(path)) {
                            checkSHA1Hash(id.setFace(NutsConstants.FACE_COMPONENT_HASH), in, session);
                        } catch (IOException ex) {
                            return;
                        }
                    }
                }).run();
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(id, null, ex);
            }
            return new NutsContent(tempFile, false, true);
        } else {
            try {
                getWorkspace().io().copy().from(getStream(id, session)).to(localPath).check(new NutsIOCopyAction.Checker() {
                    @Override
                    public void check(Path path) {
                        try (InputStream in = Files.newInputStream(path)) {
                            checkSHA1Hash(id.setFace(NutsConstants.FACE_COMPONENT_HASH), in, session);
                        } catch (IOException ex) {
                            throw new NutsIOCopyAction.ValidationException(ex);
                        }
                    }
                }).run();
            } catch (UncheckedIOException ex) {
                log.log(Level.SEVERE, id.toString() + " : " + ex.getMessage());
                throw new NutsNotFoundException(id, null, ex);
            }
            return new NutsContent(localPath, false, false);
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
    protected InputStream openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
        long startTime = System.currentTimeMillis();
        try {
            InputStream in = getWorkspace().io().monitorInputStream(path, source, session);
            if (log.isLoggable(Level.FINEST)) {
                if (URLUtils.isRemoteURL(path)) {
                    String message = URLUtils.isRemoteURL(path) ? "Downloading maven" : "Open local file";
                    message += " url=" + path;
                    traceMessage(session, id, TraceResult.SUCCESS, message, startTime);
                }
            }
            return in;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                if (URLUtils.isRemoteURL(path)) {
                    String message = URLUtils.isRemoteURL(path) ? "Downloading maven" : "Open local file";
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
            throw new NutsNotFoundException(id);
        }
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
}
