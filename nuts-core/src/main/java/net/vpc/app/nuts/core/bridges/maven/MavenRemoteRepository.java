/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsId;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.TraceResult;
import net.vpc.common.io.FileValidator;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.io.URLUtils;
import net.vpc.common.mvn.MavenMetadata;

import java.io.*;
import java.util.ArrayList;
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

    public MavenRemoteRepository(String repositoryId, String url, NutsWorkspace workspace, NutsRepository parentRepository, String root) {
        super(new NutsRepositoryConfig(repositoryId, url, NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN), workspace, parentRepository,root,SPEED_SLOW);
    }

    @Override
    protected int getSupportLevelCurrent(NutsId id, NutsSession session) {
        switch (session.getFetchMode()) {
            case OFFLINE:
                return 0;
        }
        return super.getSupportLevelCurrent(id, session);
    }

    @Override
    public List<NutsId> findVersionsImpl(final NutsId id, NutsIdFilter idFilter, final NutsSession session) {
        //maven-metadata.xml

        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = URLUtils.buildUrl(getConfigManager().getLocation(), groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

            try {
                metadataStream = openStream(id, metadataURL, id.setFace(NutsConstants.FACE_CATALOG), session);
            } catch (Exception ex) {
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
                    throw new NutsIOException(e);
                }
            }
        }
        return ret;
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        String url = URLUtils.buildUrl(getConfigManager().getLocation(), "/archetype-catalog.xml");
        InputStream s = openStream(null, url, CoreNutsUtils.parseNutsId("internal:repository").setQueryProperty("location", getConfigManager().getLocation()).setFace(NutsConstants.FACE_CATALOG), session);
        return MavenUtils.createArchetypeCatalogIterator(s, filter, true);
    }

    private NutsRepository getLocalMavenRepo() {
        for (NutsRepository nutsRepository : getWorkspace().getRepositoryManager().getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN) && nutsRepository.getConfigManager().getLocation().equals("~/.m2")) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected File getMavenLocalFolderContent(NutsId id) {
        String p = getIdRelativePath(id);
        if(p!=null){
            return new File(System.getProperty("user.home") + "/.m2", p);
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
    protected NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session) {
        if (wrapper == null) {
            wrapper = getWrapper();
        }
        if (wrapper != null && wrapper.get(id, getConfigManager().getLocation(), session)) {
            NutsRepository loc = getLocalMavenRepo();
            if (loc != null) {
                return loc.fetchContent(id, localPath, session.copy().setFetchMode(NutsFetchMode.OFFLINE));
            }
            //should be already downloaded to m2 folder
            File content = getMavenLocalFolderContent(id);
            if (content != null && content.exists()) {
                if (localPath == null) {
                    return new NutsContent(content.getPath(), true, false);
                } else {
                    File tempFile = getWorkspace().getIOManager().createTempFile(content.getName(), this);
                    IOUtils.copy(content,tempFile,true);
                    return new NutsContent(tempFile.getPath(), true, false);
                }
            }
        }
        if (localPath == null) {
            String p = getIdPath(id);
            File tempFile = getWorkspace().getIOManager().createTempFile(new File(p).getName(), this);
            try {
                IOUtils.copy(getStream(id, session), tempFile, true, true, new FileValidator() {
                    @Override
                    public void validateFile(File file) throws IOException {
                        checkSHA1Hash(id.setFace(NutsConstants.FACE_COMPONENT_HASH), new FileInputStream(file), session);
                    }
                });
            } catch (NutsIOException ex) {
                throw new NutsNotFoundException(id, null, ex);
            } catch (RuntimeIOException ex) {
                throw new NutsNotFoundException(id, null, ex);
            }
            return new NutsContent(tempFile.getPath(), false, true);
        } else {
            try {
                IOUtils.copy(getStream(id, session), new File(localPath), true, true, new FileValidator() {
                    @Override
                    public void validateFile(File file) throws IOException {
                        checkSHA1Hash(id.setFace(NutsConstants.FACE_COMPONENT_HASH), new FileInputStream(file), session);
                    }
                });
            } catch (NutsIOException ex) {
                throw new NutsNotFoundException(id, null, ex);
            } catch (RuntimeIOException ex) {
                log.log(Level.SEVERE,id.toString()+" : "+ex.getMessage());
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
    protected InputStream openStream(NutsId id, String path, Object source, NutsSession session) {
        long startTime = System.currentTimeMillis();
        try {
            InputStream in = getWorkspace().getIOManager().monitorInputStream(path, source, session);
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
    public void checkAllowedFetch(NutsId id, NutsSession session) {
        super.checkAllowedFetch(id, session);
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            throw new NutsNotFoundException(id);
        }
    }

    @Override
    public String getStoreLocation() {
        return null;
    }
}
