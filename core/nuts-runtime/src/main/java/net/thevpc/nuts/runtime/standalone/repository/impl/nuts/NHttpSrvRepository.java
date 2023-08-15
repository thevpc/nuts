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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.util.NIdLocationUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.NIteratorBase;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.NCachedRepository;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

public class NHttpSrvRepository extends NCachedRepository {

    private final NLog LOG;
    private NId remoteId;

    public NHttpSrvRepository(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        super(options, session, parentRepository, NSpeedQualifier.SLOW, false, NConstants.RepoTypes.NUTS, true);
        LOG = NLog.of(NHttpSrvRepository.class, session);
        try {
            remoteId = getRemoteId(session);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.WARNING).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("unable to initialize Repository NutsId for repository {0}", options.getLocation()));
        }
    }

    public String getUrl(String path) {
        return config().getLocationPath().resolve(path).toString();
    }

    public NId getRemoteId(NSession session) {
        if (remoteId == null) {
            try {
                remoteId = NId.of(httpGetString(getUrl("/version"), session)).get(session);
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.WARNING).verb(NLogVerb.FAIL)
                        .log(NMsg.ofJ("unable to resolve Repository NutsId for remote repository {0}", config().getLocation()));
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NPushRepositoryCommand command) {
        NSession session = command.getSession();
        NPath content = lib.fetchContentImpl(command.getId(), null, session);
        NDescriptor desc = lib.fetchDescriptorImpl(command.getId(), session);
        if (content == null || desc == null) {
            throw new NNotFoundException(session, command.getId());
        }
        NSessionUtils.checkSession(getWorkspace(), session);
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        desc.formatter(session).print(new OutputStreamWriter(descStream));
        httpUpload(CoreIOUtils.buildUrl(config().getLocationPath().toString(), "/deploy?" + resolveAuthURLPart(session)),
                session,
                new NTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NTransportParamBinaryFilePart("content", content.getName(), content),
                new NTransportParamParamPart("descriptor-hash", NDigest.of(session).sha1().setSource(desc).computeString()),
                new NTransportParamParamPart("content-hash", NDigestUtils.evalSHA1Hex(content, session)),
                new NTransportParamParamPart("force", String.valueOf(session.isYes()))
        );
    }

    @Override
    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        session.getTerminal().printProgress(NMsg.ofC("loading descriptor for %s", id.getLongId()));
        try (InputStream stream = NPath.of(getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString(), session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session).getInputStream()) {
            NDescriptor descriptor = NDescriptorParser.of(session).parse(stream).get(session);
            if (descriptor != null) {
                String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreIOUtils.urlEncodeString(id.toString(), session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
                if (hash.equals(descriptor.toString())) {
                    return descriptor;
                }
            }
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    @Override
    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            session.getTerminal().printProgress(NMsg.ofC("search version for %s", id.getLongId()));
            ret = NPath.of(getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString(), session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            return IteratorBuilder.emptyIterator();
        }
        NIterator<NId> it = new NamedNIdFromStreamIterator(ret, session);
        NIdFilter filter2 = NIdFilters.of(session).nonnull(idFilter).and(
                NIdFilters.of(session).byName(id.getShortName())
        );
        if (filter2 != null) {
            it = IteratorBuilder.of(it, session).filter(CoreFilterUtils.createFilter(filter2, session)).iterator();
        }
        return it;
    }

    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }

        session.getTerminal().printProgress(NMsg.ofC("search into %s ", Arrays.toString(basePaths)));
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth(session);
        if (filter instanceof NExprIdFilter) {
            String js = ((NExprIdFilter) filter).toExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)), session,
                        new NTransportParamParamPart("root", "/"),
                        new NTransportParamParamPart("ul", ulp[0]),
                        new NTransportParamParamPart("up", ulp[1]),
                        new NTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return IteratorBuilder.of(new NamedNIdFromStreamIterator(ret, session), session).filter(CoreFilterUtils.createFilter(filter, session)).iterator();
            }
        } else {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)), session,
                    new NTransportParamParamPart("root", "/"),
                    new NTransportParamParamPart("ul", ulp[0]),
                    new NTransportParamParamPart("up", ulp[1]),
                    new NTransportParamParamPart("pattern", ("*")),
                    new NTransportParamParamPart("transitive", String.valueOf(transitive))
            );
        }
        if (filter == null) {
            return new NamedNIdFromStreamIterator(ret, session);
        }
        return IteratorBuilder.of(new NamedNIdFromStreamIterator(ret, session), session).filter(CoreFilterUtils.createFilter(filter, session)).iterator();

    }

    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (NIdLocationUtils.fetch(id, descriptor.getLocations(), localPath, session)) {
            return NPath.of(localPath, session);
        }
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id, session);
            localPath = NPath
                    .ofTempRepositoryFile(new File(p).getName(), getUuid(),session).toString();
        }

        try {
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString(), session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session));
            NCp.of(session).from(
                    NPath.of(location,session)
            ).to(NPath.of(localPath,session)).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString(), session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
            String lhash = NDigestUtils.evalSHA1Hex(NPath.of(localPath, session), session);
            if (rhash.equalsIgnoreCase(lhash)) {
                return NPath.of(localPath, session).setUserCache(false).setUserTemporary(temp)
                        ;
            }
        } catch (UncheckedIOException | NIOException ex) {
            throw new NNotFoundException(session, id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url, NSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("get URL{0}", url));
        return CoreIOUtils.loadString(NPath.of(url, session).getInputStream(), true, session);
    }

    private InputStream httpUpload(String url, NSession session, NTransportParamPart... parts) {
        LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("uploading URL {0}", url));
        return CoreIOUtils.getHttpClientFacade(session, url).upload(parts);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth(NSession session) {
        String login = NWorkspaceSecurityManager.of(session).getCurrentUsername();
        NUserConfig security = NRepositoryConfigManagerExt.of(config()).getModel().findUser(login, session).orNull();
        String newLogin = "";
        char[] credentials = new char[0];
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous".toCharArray();
        } else {
            newLogin = security.getRemoteIdentity();
            if (NBlankable.isBlank(newLogin)) {
                NUser security2 = NWorkspaceSecurityManager.of(session).findUser(login);
                if (security2 != null) {
                    newLogin = security2.getRemoteIdentity();
                }
            }
            if (NBlankable.isBlank(newLogin)) {
                newLogin = login;
            } else {
                security = NRepositoryConfigManagerExt.of(config()).getModel().findUser(newLogin, session).orNull();
                if (security == null) {
                    newLogin = "anonymous";
                    credentials = "anonymous".toCharArray();
                }
            }
            if (security != null) {
                credentials = security.getRemoteCredentials() == null ? null : security.getRemoteCredentials().toCharArray();
                credentials = NWorkspaceSecurityManager.of(session).getCredentials(credentials);
            }
        }

        String passphrase = config().getConfigProperty(CoreSecurityUtils.ENV_KEY_PASSPHRASE)
                .flatMap(NLiteral::asString)
                .orElse(CoreSecurityUtils.DEFAULT_PASSPHRASE);
        newLogin = new String(CoreSecurityUtils.defaultEncryptChars(NStringUtils.trim(newLogin).toCharArray(), passphrase, session));
        credentials = CoreSecurityUtils.defaultEncryptChars(credentials, passphrase, session);
        return new String[]{newLogin, new String(credentials)};
    }

    private String resolveAuthURLPart(NSession session) {
        String[] auth = resolveEncryptedAuth(session);
        return "ul=" + CoreIOUtils.urlEncodeString(auth[0], session) + "&up=" + CoreIOUtils.urlEncodeString(auth[0], session);
    }

    //    @Override
//    public void undeployImpl(NutsRepositoryUndeployCommand options) {
//        throw new NutsUnsupportedOperationException(session);
//    }
//    @Override
//    public void checkAllowedFetch(NutsId parse, NutsSession session) {
//        super.checkAllowedFetch(parse, session);
//        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
//            throw new NutsNotFoundException(session(), parse);
//        }
//    }
    private class NamedNIdFromStreamIterator extends NIteratorBase<NId> {

        private final BufferedReader br;
        private String line;
        private NSession session;
        private InputStream source0;

        public NamedNIdFromStreamIterator(InputStream ret, NSession session) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
            this.session = session;
        }

        @Override
        public NElement describe(NSession session) {
            return NElements.of(session).ofObject()
                    .set("type", "ScanArchetypeCatalog")
                    .set("source", source0.toString())
                    .build();
        }


        @Override
        public boolean hasNext() {
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    close();
                    return false;
                }
                if (line == null) {
                    close();
                    return false;
                }
                line = line.trim();
                if (line.length() > 0) {
                    return true;
                }
            }
        }

        private void close() {
            try {
                br.close();
            } catch (IOException ex) {
                //
            }
        }

        @Override
        public NId next() {
            NId nutsId = NId.of(line).get(session);
            return nutsId.builder().setRepository(getName()).build();
        }
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return true;
    }

    //    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
    @Override
    public boolean isRemote() {
        return true;
    }

}
