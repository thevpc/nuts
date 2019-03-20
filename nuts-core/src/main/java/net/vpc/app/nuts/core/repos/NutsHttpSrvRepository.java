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
import net.vpc.app.nuts.core.filters.id.NutsJsAwareIdFilter;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;
import net.vpc.common.util.IteratorFilter;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsHttpSrvRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(NutsHttpSrvRepository.class.getName());
    private NutsId remoteId;

    public NutsHttpSrvRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW);
        try {
            remoteId = CoreNutsUtils.parseRequiredNutsId(httpGetString(options.getLocation() + "/version"));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to initialize Repository NutsId for repository {0}", options.getLocation());
        }
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
    public void pushImpl(NutsId id, String repoId, NutsPushOptions options, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    public String getUrl(String path) {
        return URLUtils.buildUrl(getConfigManager().getLocation(true), path);
    }

    public NutsId getRemoteId() {
        if (remoteId == null) {
            try {
                remoteId = CoreNutsUtils.parseRequiredNutsId(httpGetString(getUrl("/version")));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Unable to resolve Repository NutsId for remote repository {0}", getConfigManager().getLocation());
            }
        }
        return remoteId;
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsDeployOptions options, NutsSession session) {
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            throw new NutsIllegalArgumentException("Offline");
        }
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor, new OutputStreamWriter(descStream));
        File file1 = new File(file);
        httpUpload(URLUtils.buildUrl(getConfigManager().getLocation(true), "/deploy?" + resolveAuthURLPart()),
                new NutsTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NutsTransportParamBinaryFilePart("content", file1.getName(), file1),
                new NutsTransportParamParamPart("descriptor-hash", getWorkspace().getIOManager().getSHA1(descriptor)),
                new NutsTransportParamParamPart("content-hash", CoreSecurityUtils.evalSHA1(file1)),
                new NutsTransportParamParamPart("force", String.valueOf(options))
        );
        //TODO should read the id
        return id;
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        boolean transitive = session.isTransitive();
        InputStream stream = null;
        try {
            try {
                stream = CoreHttpUtils.getHttpClientFacade(getWorkspace(), getUrl("/fetch-descriptor?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
                NutsDescriptor descriptor = getWorkspace().getParseManager().parseDescriptor(stream, true);
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
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    protected void copyToImpl(NutsId id, String localPath, NutsSession session) {
        boolean transitive = session.isTransitive();

        try {
            helperHttpDownloadToFile(getUrl("/fetch?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), new File(localPath), true);
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = CoreSecurityUtils.evalSHA1(new File(localPath));
            if (rhash.equals(lhash)) {
                return;
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            ret = CoreHttpUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-all-versions?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
        if (idFilter != null) {
            it = new IteratorFilter<NutsId>(it, CoreNutsUtils.createFilter(idFilter));
        }
        return CollectionUtils.toList(it);
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth();
        if (filter instanceof NutsJsAwareIdFilter) {
            String js = ((NutsJsAwareIdFilter) filter).toJsNutsIdFilterExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                        new NutsTransportParamParamPart("root", "/"),
                        new NutsTransportParamParamPart("ul", ulp[0]),
                        new NutsTransportParamParamPart("up", ulp[1]),
                        new NutsTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return new IteratorFilter<>(new NamedNutIdFromStreamIterator(ret), CoreNutsUtils.createFilter(filter));
            }
        } else {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                    new NutsTransportParamParamPart("root", "/"),
                    new NutsTransportParamParamPart("ul", ulp[0]),
                    new NutsTransportParamParamPart("up", ulp[1]),
                    new NutsTransportParamParamPart("pattern", ("*")),
                    new NutsTransportParamParamPart("transitive", String.valueOf(transitive))
            );
        }
        if (filter == null) {
            return new NamedNutIdFromStreamIterator(ret);
        }
        return new IteratorFilter<>(new NamedNutIdFromStreamIterator(ret), CoreNutsUtils.createFilter(filter));
    }

    //    @Override
//    public int getSupportLevel(NutsId id, boolean transitive) throws IOException {
//        int val = super.getSupportLevel(id, transitive);
//        if (val > 1) {
//            return val;
//        }
//        String s = httpGetString(getIdPath("/get-support/?id=" + HttpUtils.urlEncodeString(id.toString())  + (transitive ? ("&transitive") : "") + resolveAuthURLPart()));
//        return s == null ? -1 : Integer.parseInt(s);
//    }
//    @Override
//    public NutsDefinition fetchImpl(NutsId id, NutsSession session) {
//        return helperFetchImplByCopy(id,session);
//    }
    @Override
    protected NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session) {
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id);
            localPath = getWorkspace().getIOManager().createTempFile(new File(p).getName(), this).getPath();
        }

        try {
            helperHttpDownloadToFile(getUrl("/fetch?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), new File(localPath), true);
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreHttpUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = CoreSecurityUtils.evalSHA1(new File(localPath));
            if (rhash.equals(lhash)) {
                return new NutsContent(localPath, false, temp);
            }
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, ex);
            //
        }
        throw new NutsNotFoundException(id);

    }

    private String httpGetString(String url) {
        log.log(Level.FINEST, "Get URL{0}", url);
        try {
            return IOUtils.loadString(CoreHttpUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    private InputStream httpUpload(String url, NutsTransportParamPart... parts) {
        log.log(Level.FINEST, "Uploading URL {0}", url);
        try {
            return CoreHttpUtils.getHttpClientFacade(getWorkspace(), url).upload(parts);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth() {
        String login = getWorkspace().getSecurityManager().getCurrentLogin();
        NutsUserConfig security = getConfigManager().getUser(login);
        String newLogin = "";
        String credentials = "";
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous";
        } else {
            newLogin = security.getMappedUser();
            if (StringUtils.isEmpty(newLogin)) {
                NutsEffectiveUser security2 = getWorkspace().getSecurityManager().findUser(login);
                if (security2 != null) {
                    newLogin = security2.getMappedUser();
                }
            }
            if (StringUtils.isEmpty(newLogin)) {
                newLogin = login;
            }
            credentials = getWorkspace().getConfigManager().createAuthenticationAgent(security.getAuthenticationAgent())
                    .getCredentials(credentials, security.getAuthenticationAgent(),
                            getConfigManager());
        }

        String passphrase = getConfigManager().getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE, true);
        newLogin = new String(CoreSecurityUtils.httpEncrypt(StringUtils.trim(newLogin).getBytes(), passphrase));
        credentials = new String(CoreSecurityUtils.httpEncrypt(StringUtils.trim(credentials).getBytes(), passphrase));
        return new String[]{newLogin, credentials};
    }

    private String resolveAuthURLPart() {
        String[] auth = resolveEncryptedAuth();
        return "ul=" + CoreHttpUtils.urlEncodeString(auth[0]) + "&up=" + CoreHttpUtils.urlEncodeString(auth[0]);
    }

    @Override
    protected void undeployImpl(NutsId id, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsSession session) {
        super.checkAllowedFetch(id, session);
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            throw new NutsNotFoundException(id);
        }
    }

    private class NamedNutIdFromStreamIterator implements Iterator<NutsId> {

        private final BufferedReader br;
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
            NutsId nutsId = getWorkspace().getParseManager().parseRequiredId(line);
            return nutsId.setNamespace(getName());
        }
    }

    @Override
    public String getStoreLocation() {
        return null;
    }
}
