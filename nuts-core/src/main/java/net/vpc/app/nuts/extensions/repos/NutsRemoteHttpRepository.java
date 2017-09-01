/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.AbstractNutsRepository;
import net.vpc.app.nuts.extensions.util.CoreHttpUtils;
import net.vpc.app.nuts.extensions.util.IteratorFilter;
import net.vpc.app.nuts.extensions.util.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.boot.NutsIdPatternFilter;
import net.vpc.app.nuts.extensions.util.VersionFilterToNutsIdFilterAdapter;
import net.vpc.app.nuts.util.*;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsRemoteHttpRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(NutsRemoteHttpRepository.class.getName());
    private NutsId remoteId;

    public NutsRemoteHttpRepository(String repositoryId, String url, NutsWorkspace workspace, File root) throws IOException {
        super(new NutsRepositoryConfig(repositoryId, url, NutsConstants.DEFAULT_REPOSITORY_TYPE), workspace, root, SPEED_SLOW);
        try {
            remoteId = NutsId.parseOrError(httpGetString(url + "/version"));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to initialize Repository NutsId for repository {0}", url);
        }
    }

    @Override
    public void pushImpl(NutsId id, String repoId, NutsSession session) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    public String getUrl(String path) {
        return IOUtils.buildUrl(getConfig().getLocation(), path);
    }

    public NutsId getRemoteId() {
        if (remoteId == null) {
            try {
                remoteId = NutsId.parseOrError(httpGetString(getUrl("/version")));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Unable to resolve Repository NutsId for remote repository {0}", getConfig().getLocation());
            }
        }
        return remoteId;
    }

    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, File file, NutsSession session) throws IOException {
        if (session.getFetchMode()==FetchMode.OFFLINE) {
            throw new IllegalArgumentException("Offline");
        }
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        descriptor.write(descStream);
        httpUpload(IOUtils.buildUrl(getConfig().getLocation(), "/deploy?" + resolveAuthURLPart()),
                new TransportParamBinaryStreamPart("descriptor", "Project.nuts", new ByteArrayInputStream(descStream.toByteArray())),
                new TransportParamBinaryFilePart("content", file.getName(), file),
                new TransportParamParamPart("descriptor-hash", descriptor.getSHA1()),
                new TransportParamParamPart("content-hash", SecurityUtils.evalSHA1(file))
        );
        //TODO should read the id
        return id;
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        InputStream stream = null;
        try {
            try {
                stream = NutsUtils.getHttpClientFacade(getWorkspace(), getUrl("/fetch-descriptor?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
                NutsDescriptor descriptor = NutsDescriptor.parse(stream);
                if (descriptor != null) {
                    String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
                    if (hash.equals(descriptor.toString())) {
                        return descriptor;
                    }
                }
            } catch (IOException ex) {
                throw new NutsNotFoundException(id);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        throw new IOException("Unable to load file " + id);
    }

    private void httpDownloadToFile(String url, File file, boolean mkdirs) throws IOException {
        log.log(Level.FINEST, "downloading url {0} To {1}", new Object[]{url, file});
        IOUtils.copy(NutsUtils.getHttpClientFacade(getWorkspace(), url).open(), file, mkdirs, true);
    }

    @Override
    protected File fetchImpl(NutsId id, NutsSession session, File localPath) throws IOException {
        boolean transitive = session.isTransitive();
        if(localPath.isDirectory()){
            NutsDescriptor desc = fetchDescriptor(id, session);
            localPath=new File(localPath,NutsUtils.getNutsFileName(id,IOUtils.getFileExtension(desc.getExt())));
        }

        httpDownloadToFile(getUrl("/fetch?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), localPath, true);
        String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
        String lhash = SecurityUtils.evalSHA1(localPath);
        if (rhash.equals(lhash)) {
            return localPath;
        }
        throw new NutsNotFoundException(id.toString());
    }

    @Override
    public boolean fetchDescriptorImpl(NutsId id, NutsSession session, File localPath) throws IOException {
        boolean transitive = session.isTransitive();
        httpDownloadToFile(getUrl("/fetch-descriptor?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), localPath, true);
        String rhash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
        String lhash = SecurityUtils.evalSHA1(localPath);
        if (rhash.equals(lhash)) {
            return true;
        }
        return false;
    }

    @Override
    protected String fetchHashImpl(NutsId id, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        return httpGetString(getUrl("/fetch-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
    }

    @Override
    public String fetchDescriptorHashImpl(NutsId id, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        return httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
    }

    @Override
    public NutsId resolveIdImpl(NutsId id, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        String s = null;
        try {
            s = httpGetString(getUrl("/resolve-id?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
        } catch (Exception ex) {
            //ignore error
        }
        if (s == null) {
            throw new NutsNotFoundException(id);
        }
        return NutsId.parseOrError(s).setNamespace(getRepositoryId());
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsVersionFilter versionFilter, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        InputStream ret = NutsUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-all-versions?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
        if (versionFilter != null) {
            it = new IteratorFilter<NutsId>(it, new VersionFilterToNutsIdFilterAdapter(versionFilter));
        }
        return it;
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        InputStream ret = NutsUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-all-versions?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
        if (versionFilter != null) {
            it = new IteratorFilter<NutsId>(it, new NutsIdFilter() {
                @Override
                public boolean accept(NutsId id) {
                    try {
                        return versionFilter.accept(fetchDescriptor(id,session));
                    } catch (IOException e) {
                        return false;
                    }
                }
            });
        }
        return it;
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsDescriptorFilter filter, NutsSession session) throws IOException {
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveAuth();
        if (filter == null) {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                    new TransportParamParamPart("root", "/"),
                    new TransportParamParamPart("ul", ulp[0]),
                    new TransportParamParamPart("up", ulp[1]),
                    new TransportParamParamPart("pattern", ("*")),
                    new TransportParamParamPart("transitive", String.valueOf(transitive))
            );
        } else if (filter instanceof NutsIdPatternFilter) {
            NutsIdPatternFilter filter1 = (NutsIdPatternFilter) filter;
            String sb = JsonUtils.serializeObj(filter, new JsonUtils.SerializeOptions().setPretty(true)).build().toString();
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                    new TransportParamParamPart("root", "/"),
                    new TransportParamParamPart("ul", ulp[0]),
                    new TransportParamParamPart("up", ulp[1]),
                    new TransportParamParamPart("pattern", sb)
            );
        } else if (filter instanceof NutsDescriptorJavascriptFilter) {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                    new TransportParamParamPart("root", "/"),
                    new TransportParamParamPart("ul", ulp[0]),
                    new TransportParamParamPart("up", ulp[1]),
                    new TransportParamTextReaderPart("js", "search.js", new StringReader(((NutsDescriptorJavascriptFilter) filter).getCode()))
            );
        } else {
            throw new UnsupportedOperationException();
        }
        return new NamedNutIdFromStreamIterator(ret);
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
    public NutsFile fetchImpl(NutsId id, NutsSession session) throws IOException {
        NutsDescriptor descriptor = fetchDescriptor(id, session);
        File tempFile = IOUtils.createTempFile(descriptor);
        fetch(id, session, tempFile);
        NutsDescriptor ed = getWorkspace().fetchEffectiveDescriptor(descriptor, session);
        return new NutsFile(ed.getId(), descriptor, tempFile, false, true);
    }

    private String httpGetString(String url) throws IOException {
        log.log(Level.FINEST, "downloading url {0}", url);
        return IOUtils.readStreamAsString(NutsUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
    }

    private InputStream httpUpload(String url, TransportParamPart... parts) throws IOException {
        log.log(Level.FINEST, "uploading url {0}", url);
        return NutsUtils.getHttpClientFacade(getWorkspace(), url).upload(parts);
    }

    private class NamedNutIdFromStreamIterator implements Iterator<NutsId> {

        private BufferedReader br;
        private String line;

        public NamedNutIdFromStreamIterator(InputStream ret) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
        }

        @Override
        public boolean hasNext() {
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    return false;
                }
                if (line == null) {
                    return false;
                }
                line = line.trim();
                if (line.length() > 0) {
                    return true;
                }
            }
        }

        @Override
        public NutsId next() {
            NutsId nutsId = NutsId.parseOrError(line);
            return nutsId.setNamespace(getRepositoryId());
        }
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveAuth() throws IOException {
        String login = getWorkspace().getCurrentLogin();
        NutsSecurityEntityConfig security = getConfig().getSecurity(login);
        String newLogin = "";
        String credentials = "";
        String passphrase = getConfig().getEnv(NutsConstants.ENV_KEY_PASSPHRASE, NutsConstants.DEFAULT_PASSPHRASE);
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous";
        } else {
            newLogin = security.getMappedUser();
            if (StringUtils.isEmpty(newLogin)) {
                NutsSecurityEntityConfig security2 = getWorkspace().getConfig().getSecurity(login);
                if (security2 != null) {
                    newLogin = security2.getMappedUser();
                }
            }
            if (StringUtils.isEmpty(newLogin)) {
                newLogin = login;
            }
            credentials = security.getCredentials();
            //credentials are already encrypted with default passphrase!
            if (!StringUtils.isEmpty(credentials)) {
                credentials = new String(SecurityUtils.httpDecrypt(credentials, NutsConstants.DEFAULT_PASSPHRASE));
            }
        }
        newLogin = SecurityUtils.httpEncrypt(StringUtils.trim(newLogin).getBytes(), passphrase);
        credentials = SecurityUtils.httpEncrypt(StringUtils.trim(credentials).getBytes(), passphrase);
        return new String[]{newLogin, credentials};
    }

    private String resolveAuthURLPart() throws IOException {
        String[] auth = resolveAuth();
        return "ul=" + CoreHttpUtils.urlEncodeString(auth[0]) + "&up=" + CoreHttpUtils.urlEncodeString(auth[0]);
    }

    @Override
    protected void undeployImpl(NutsId id, NutsSession session) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAllowedFetch(NutsSession session) {
        return super.isAllowedFetch(session) && session.getFetchMode()!=FetchMode.OFFLINE;
    }
}
