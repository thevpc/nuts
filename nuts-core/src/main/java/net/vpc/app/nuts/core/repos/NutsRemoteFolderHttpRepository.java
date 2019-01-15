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
import net.vpc.app.nuts.core.util.CoreNutsUtils;
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

    public NutsRemoteFolderHttpRepository(String repositoryId, String url, NutsWorkspace workspace, NutsRepository parentRepository, String root) {
        super(new NutsRepositoryConfig(repositoryId, url, NutsConstants.REPOSITORY_TYPE_NUTS), workspace, parentRepository,
                root != null ? root : CoreIOUtils.resolvePath(repositoryId, CoreIOUtils.createFile(
                        workspace.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
                        workspace.getConfigManager().getHomeLocation()).getPath()
                , SPEED_SLOW);
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

    public String getUrl(String path) {
        return URLUtils.buildUrl(getConfigManager().getLocation(), path);
    }


    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    protected InputStream getStream(NutsId id, String extension, NutsSession session) {
        String url = getPath(id, extension);
        if (URLUtils.isRemoteURL(url)) {
            String message = URLUtils.isRemoteURL(url) ? "Downloading maven" : "Open local file";
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, StringUtils.alignLeft(getRepositoryId(), 20) + " " + message + " " + StringUtils.alignLeft("\'" + extension + "\'", 20) + " url " + url);
            }
        }
        return openStream(url, id, session);
    }

    protected InputStream getDescStream(NutsId id, NutsSession session) {
        String url = getDescPath(id);
        if (URLUtils.isRemoteURL(url)) {
            String message = URLUtils.isRemoteURL(url) ? "Downloading maven" : "Open local file";
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, StringUtils.alignLeft(getRepositoryId(), 20) + " " + message + " url " + url);
            }
        }
        return openStream(url, id, session);
    }

    protected String getPath(NutsId id, String extension) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String version = id.getVersion().getValue();
        return (URLUtils.buildUrl(getConfigManager().getLocation(), groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/"
                +getQueryFilename(id,extension)
        ));
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

    private void httpDownloadToFile(String path, File file, boolean mkdirs) throws IOException {
        InputStream stream = CoreHttpUtils.getHttpClientFacade(getWorkspace(), path).open();
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
            } else {
                log.log(Level.FINEST, "downloading url {0} to file {1}", new Object[]{path, file});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0} to file {1}", new Object[]{path, file});
        }
        CoreNutsUtils.copy(stream, file, mkdirs, true);
    }

    @Override
    protected String copyToImpl(NutsId id, String localPath, NutsSession session) {
        NutsDescriptor desc = null;
        String fileExtension = null;
        desc = fetchDescriptor(id, session);
        fileExtension = desc.getExt();
        if (new File(localPath).isDirectory()) {
            localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, fileExtension)).getPath();
        }
        for (String location : desc.getLocations()) {
            if(!StringUtils.isEmpty(location)){
                try {
                    getWorkspace().getIOManager().downloadPath(location, new File(localPath), null, session);
                    return localPath;
                }catch (Exception ex){
                    //ignore
                }
            }
        }
        try {
            httpDownloadToFile(getPath(id, fileExtension), new File(localPath), true);
            return localPath;
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public String copyDescriptorToImpl(NutsId id, String localPath, NutsSession session) {
        try {
            httpDownloadToFile(getDescPath(id), new File(localPath), true);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    protected String fetchHashImpl(NutsId id, NutsSession session) {
        return null;
//        boolean transitive = session.isTransitive();
//        return httpGetString(getUrl("/fetch-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
    }

    @Override
    public String fetchDescriptorHashImpl(NutsId id, NutsSession session) {
        return null;
//        boolean transitive = session.isTransitive();
//        return httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
    }

    //    @Override
//    public NutsId resolveIdImpl(NutsId id, NutsSession session) {
//        boolean transitive = session.isTransitive();
//        String s = null;
//        try {
//            s = httpGetString(getUrl("/resolve-id?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
//        } catch (Exception ex) {
//            //ignore error
//        }
//        if (s == null) {
//            throw new NutsNotFoundException(id);
//        }
//        return CoreNutsUtils.parseRequiredId(s).setNamespace(getRepositoryId());
//    }
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
//        boolean transitive = session.isTransitive();
//        InputStream ret = null;
//        try {
//            ret = CoreHttpUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-all-versions?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
//        } catch (IOException e) {
//            throw new NutsIOException(e);
//        }
//        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
//        if (idFilter != null) {
//            it = new IteratorFilter<NutsId>(it,CoreNutsUtils.createFilter(idFilter));
//        }
//        return it;
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        return Collections.EMPTY_LIST.iterator();
    }

    //    @Override
//    public int getSupportLevel(NutsId id, boolean transitive) throws IOException {
//        int val = super.getSupportLevel(id, transitive);
//        if (val > 1) {
//            return val;
//        }
//        String s = httpGetString(getPath("/get-support/?id=" + HttpUtils.urlEncodeString(id.toString())  + (transitive ? ("&transitive") : "") + resolveAuthURLPart()));
//        return s == null ? -1 : Integer.parseInt(s);
//    }
    @Override
    public NutsDefinition fetchImpl(NutsId id, NutsSession session) {
        NutsDescriptor descriptor = fetchDescriptor(id, session);
        String tempFile = CoreIOUtils.createTempFile(descriptor, false).getPath();
        copyTo(id, tempFile, session);
        NutsDescriptor ed = getWorkspace().resolveEffectiveDescriptor(descriptor, session);
        return new NutsDefinition(ed.getId(), descriptor, tempFile, false, true, null,null);
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
