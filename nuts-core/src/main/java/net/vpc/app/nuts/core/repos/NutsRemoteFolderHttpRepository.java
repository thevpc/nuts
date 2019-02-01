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
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreHttpUtils;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsRemoteFolderHttpRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(NutsRemoteFolderHttpRepository.class.getName());

    public NutsRemoteFolderHttpRepository(String repositoryId, String url, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot) {
        super(new NutsRepositoryConfig(repositoryId, url, NutsConstants.REPOSITORY_TYPE_NUTS), workspace, parentRepository,repositoryRoot, SPEED_SLOW);
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
    public void pushImpl(NutsId id, String repoId, NutsConfirmAction foundAction, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    protected InputStream getDescStream(NutsId id, NutsSession session) {
        String url = getDescPath(id);
        if (URLUtils.isRemoteURL(url)) {
            String message = URLUtils.isRemoteURL(url) ? "Downloading maven" : "Open local file";
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, StringUtils.alignLeft(getName(), 20) + " " + message + " url " + url);
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
        return (URLUtils.buildUrl(getConfigManager().getLocation(), groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/"
                + "nuts.json"
        ));
    }

    protected InputStream openStream(String path, Object source, NutsSession session) {
        return getWorkspace().getIOManager().monitorInputStream(path, source, session);
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        boolean transitive = session.isTransitive();
        InputStream stream = null;
        try {
            try {
                NutsDescriptor descriptor = getWorkspace().getParseManager().parseDescriptor(stream = getDescStream(id, session), true);
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
                    throw new NutsIOException(e);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        try {
            String[] all = httpGetString(URLUtils.buildUrl(getConfigManager().getLocation(), groupId.replace('.', '/') + "/" + artifactId)+"/.folders").split("\n");
            List<NutsId> n=new ArrayList<>();
            for (String s : all) {
                if(!StringUtils.isEmpty(s) && !"LATEST".equals(s) && !"RELEASE".equals(s)){
                    NutsId id2 = id.builder().setVersion(s).build();
                    if(idFilter==null|| idFilter.accept(id2)) {
                        n.add(id2);
                    }
                }
            }
            return n;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public NutsContent fetchContentImpl(NutsId id, String localFile,NutsSession session) {
        try {
            if(localFile==null){
                String path = getPath(id);
                File tempFile = getWorkspace().getIOManager().createTempFile(new File(path).getName(), this);
                helperHttpDownloadToFile(path, tempFile, true);
                return new NutsContent(tempFile.getPath(),false,true);
            }else {
                helperHttpDownloadToFile(getPath(id), new File(localFile), true);
                return new NutsContent(localFile,false,false);
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }

    }


    private String httpGetString(String url) {
        try {
            String s = IOUtils.loadString(CoreHttpUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
            log.log(Level.FINEST, "[SUCCESS] Get URL {0}", url);
            return s;
        } catch (IOException e) {
            log.log(Level.FINEST, "[ERROR  ] Get URL {0}", url);
            throw new NutsIOException(e);
        }
    }

    @Override
    protected void undeployImpl(NutsId id, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsSession session) {
        super.checkAllowedFetch(id, session);
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            throw new NutsNotFoundException(id.toString());
        }
    }

    @Override
    public String getStoreLocation() {
        return null;
    }
}
