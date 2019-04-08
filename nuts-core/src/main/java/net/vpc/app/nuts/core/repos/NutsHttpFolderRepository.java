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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

public class NutsHttpFolderRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(NutsHttpFolderRepository.class.getName());

    public NutsHttpFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
    }

    @Override
    public void pushImpl(NutsId id, NutsPushCommand options, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected void deployImpl(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    protected InputStream getDescStream(NutsId id, NutsRepositorySession session) {
        String url = getDescPath(id);
        if (CoreIOUtils.isPathHttp(url)) {
            String message = "Downloading maven" ;//: "Open local file";
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, CoreStringUtils.alignLeft(config().getName(), 20) + " " + message + " url " + url);
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
                + "nuts.json"
        ));
    }

    protected InputStream openStream(String path, Object source, NutsRepositorySession session) {
        return getWorkspace().io().monitorInputStream(path, source, session);
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        InputStream stream = null;
        try {
            try {
                NutsDescriptor descriptor = getWorkspace().parser().parseDescriptor(stream = getDescStream(id, session), true);
                if (descriptor != null) {
                    //String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
                    //if (hash.equals(descriptor.toString())) {
                    return descriptor;
                    //}
                }
            } catch (Exception ex) {
                throw new NutsNotFoundException(id);
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        String groupId = id.getGroup();
        String artifactId = id.getName();
        try {
            String[] all = httpGetString(CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId) + "/.folders").split("\n");
            List<NutsId> n = new ArrayList<>();
            for (String s : all) {
                if (!CoreStringUtils.isBlank(s) && !"LATEST".equals(s) && !"RELEASE".equals(s)) {
                    NutsId id2 = id.builder().setVersion(s).build();
                    if (idFilter == null || idFilter.accept(id2)) {
                        n.add(id2);
                    }
                }
            }
            return n.iterator();
        } catch (Exception ex) {
//            return Collections.emptyIterator();
            return null;
        }

    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {
//        return Collections.EMPTY_LIST.iterator();
        return null;
    }

    @Override
    public NutsContent fetchContentImpl(NutsId id, Path localFile, NutsRepositorySession session) {
        try {
            if (localFile == null) {
                String path = getPath(id);
                Path tempFile = getWorkspace().io().createTempFile(new File(path).getName(), this);
                helperHttpDownloadToFile(path, tempFile, true);
                return new NutsContent(tempFile, false, true);
            } else {
                helperHttpDownloadToFile(getPath(id), localFile, true);
                return new NutsContent(localFile, false, false);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private String httpGetString(String url) {
        try {
            String s = CoreIOUtils.loadString(CoreIOUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
            log.log(Level.FINEST, "[SUCCESS] Get URL {0}", url);
            return s;
        } catch (UncheckedIOException e) {
            log.log(Level.FINEST, "[ERROR  ] Get URL {0}", url);
            throw e;
        }
    }

    @Override
    protected void undeployImpl(NutsId id, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
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