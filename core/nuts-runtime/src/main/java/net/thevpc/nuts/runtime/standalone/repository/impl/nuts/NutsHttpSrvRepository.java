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
 *
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
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsIdLocationUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.NutsIteratorBase;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsCachedRepository;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NutsDigestUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsStringUtils;

public class NutsHttpSrvRepository extends NutsCachedRepository {

    private final NutsLogger LOG;
    private NutsId remoteId;

    public NutsHttpSrvRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, NutsSpeedQualifier.SLOW, false, NutsConstants.RepoTypes.NUTS,true);
        LOG = NutsLogger.of(NutsHttpSrvRepository.class,session);
        try {
            remoteId = getRemoteId(session);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.WARNING).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.jstyle("unable to initialize Repository NutsId for repository {0}", options.getLocation()));
        }
    }

    public String getUrl(String path) {
        return config().getLocationPath().resolve(path).toString();
    }

    public NutsId getRemoteId(NutsSession session) {
        if (remoteId == null) {
            try {
                remoteId = NutsId.of(httpGetString(getUrl("/version"),session)).get(session);
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.WARNING).verb(NutsLoggerVerb.FAIL)
                        .log(NutsMessage.jstyle("unable to resolve Repository NutsId for remote repository {0}", config().getLocation()));
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        NutsSession session = command.getSession();
        NutsPath content = lib.fetchContentImpl(command.getId(), null, session);
        NutsDescriptor desc = lib.fetchDescriptorImpl(command.getId(), session);
        if (content == null || desc == null) {
            throw new NutsNotFoundException(session, command.getId());
        }
        NutsSessionUtils.checkSession(getWorkspace(), session);
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        desc.formatter(session).print(new OutputStreamWriter(descStream));
        httpUpload(CoreIOUtils.buildUrl(config().getLocationPath().toString(), "/deploy?" + resolveAuthURLPart(session)),
                session,
                new NutsTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NutsTransportParamBinaryFilePart("content", content.getName(), content),
                new NutsTransportParamParamPart("descriptor-hash", NutsDigest.of(session).sha1().setSource(desc).computeString()),
                new NutsTransportParamParamPart("content-hash", NutsDigestUtils.evalSHA1Hex(content,session)),
                new NutsTransportParamParamPart("force", String.valueOf(session.isYes()))
        );
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        session.getTerminal().printProgress("loading descriptor for ", id.getLongId());
        try (InputStream stream = NutsPath.of(getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString(),session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)),session).getInputStream()) {
            NutsDescriptor descriptor = NutsDescriptorParser.of(session).parse(stream).get(session);
            if (descriptor != null) {
                String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreIOUtils.urlEncodeString(id.toString(),session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
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
    public NutsIterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            session.getTerminal().printProgress("search version for %s", id.getLongId(), session);
            ret = NutsPath.of(getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString(),session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)),session).getInputStream();
        } catch (UncheckedIOException | NutsIOException e) {
            return IteratorBuilder.emptyIterator();
        }
        NutsIterator<NutsId> it = new NamedNutIdFromStreamIterator(ret,session);
        NutsIdFilter filter2 = NutsIdFilters.of(session).nonnull(idFilter).and(
                NutsIdFilters.of(session).byName(id.getShortName())
        );
        if (filter2 != null) {
            it = IteratorBuilder.of(it, session).filter(CoreFilterUtils.createFilter(filter2, session)).iterator();
        }
        return it;
    }

    @Override
    public NutsIterator<NutsId> searchCore(final NutsIdFilter filter, NutsPath[] basePaths, NutsId[] baseIds, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }

        session.getTerminal().printProgress("search into %s " ,Arrays.toString(basePaths));
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth(session);
        if (filter instanceof NutsExprIdFilter) {
            String js = ((NutsExprIdFilter) filter).toExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)), session,
                        new NutsTransportParamParamPart("root", "/"),
                        new NutsTransportParamParamPart("ul", ulp[0]),
                        new NutsTransportParamParamPart("up", ulp[1]),
                        new NutsTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret,session), session).filter(CoreFilterUtils.createFilter(filter, session)).iterator();
            }
        } else {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)), session,
                    new NutsTransportParamParamPart("root", "/"),
                    new NutsTransportParamParamPart("ul", ulp[0]),
                    new NutsTransportParamParamPart("up", ulp[1]),
                    new NutsTransportParamParamPart("pattern", ("*")),
                    new NutsTransportParamParamPart("transitive", String.valueOf(transitive))
            );
        }
        if (filter == null) {
            return new NamedNutIdFromStreamIterator(ret,session);
        }
        return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret,session), session).filter(CoreFilterUtils.createFilter(filter, session)).iterator();

    }

    @Override
    public NutsPath fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (NutsIdLocationUtils.fetch(id, descriptor.getLocations(), localPath, session)) {
            return NutsPath.of(localPath, session);
        }
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id, session);
            localPath = NutsTmp.of(session)
                    .setRepositoryId(getUuid())
                    .createTempFile(new File(p).getName()).toString();
        }

        try {
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString(),session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session));
            NutsCp.of(session).from(location).to(localPath).addOptions(NutsPathOption.SAFE,NutsPathOption.LOG, NutsPathOption.TRACE).run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString(),session) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
            String lhash = NutsDigestUtils.evalSHA1Hex(NutsPath.of(localPath,session),session);
            if (rhash.equalsIgnoreCase(lhash)) {
                return NutsPath.of(localPath,session).setUserCache(false).setUserTemporary(temp)
                       ;
            }
        } catch (UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url, NutsSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.START)
                .log(NutsMessage.jstyle("get URL{0}", url));
        return CoreIOUtils.loadString(NutsPath.of(url,session).getInputStream(), true,session);
    }

    private InputStream httpUpload(String url, NutsSession session, NutsTransportParamPart... parts) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.START)
                .log(NutsMessage.jstyle("uploading URL {0}", url));
        return CoreIOUtils.getHttpClientFacade(session, url).upload(parts);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth(NutsSession session) {
        String login = session.security().setSession(session).getCurrentUsername();
        NutsUserConfig security = NutsRepositoryConfigManagerExt.of(config()).getModel().getUser(login, session);
        String newLogin = "";
        char[] credentials = new char[0];
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous".toCharArray();
        } else {
            newLogin = security.getRemoteIdentity();
            if (NutsBlankable.isBlank(newLogin)) {
                NutsUser security2 = session.security().findUser(login);
                if (security2 != null) {
                    newLogin = security2.getRemoteIdentity();
                }
            }
            if (NutsBlankable.isBlank(newLogin)) {
                newLogin = login;
            } else {
                security = NutsRepositoryConfigManagerExt.of(config()).getModel().getUser(newLogin, session);
                if (security == null) {
                    newLogin = "anonymous";
                    credentials = "anonymous".toCharArray();
                }
            }
            if (security != null) {
                credentials = security.getRemoteCredentials() == null ? null : security.getRemoteCredentials().toCharArray();
                credentials = session.security().getCredentials(credentials);
            }
        }

        String passphrase = config().getConfigProperty(CoreSecurityUtils.ENV_KEY_PASSPHRASE)
                .flatMap(NutsValue::asString)
                .orElse(CoreSecurityUtils.DEFAULT_PASSPHRASE);
        newLogin = new String(CoreSecurityUtils.defaultEncryptChars(NutsStringUtils.trim(newLogin).toCharArray(), passphrase,session));
        credentials = CoreSecurityUtils.defaultEncryptChars(credentials, passphrase,session);
        return new String[]{newLogin, new String(credentials)};
    }

    private String resolveAuthURLPart(NutsSession session) {
        String[] auth = resolveEncryptedAuth(session);
        return "ul=" + CoreIOUtils.urlEncodeString(auth[0],session) + "&up=" + CoreIOUtils.urlEncodeString(auth[0],session);
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
    private class NamedNutIdFromStreamIterator extends NutsIteratorBase<NutsId> {

        private final BufferedReader br;
        private String line;
        private NutsSession session;
        private InputStream source0;

        public NamedNutIdFromStreamIterator(InputStream ret,NutsSession session) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
            this.session=session;
        }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofObject()
                .set("type","ScanArchetypeCatalog")
                .set("source",source0.toString())
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
        public NutsId next() {
            NutsId nutsId = NutsId.of(line).get(session);
            return nutsId.builder().setRepository(getName()).build();
        }
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
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
