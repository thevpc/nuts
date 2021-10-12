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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.bundles.io.CoreSecurityUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.core.filters.id.NutsScriptAwareIdFilter;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.spi.*;

public class NutsHttpSrvRepository extends NutsCachedRepository {

    private final NutsLogger LOG;
    private NutsId remoteId;

    public NutsHttpSrvRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, SPEED_SLOW, false, "nuts:api");
        LOG = session.log().of(NutsHttpSrvRepository.class);
        try {
            remoteId = session.id().parser().setLenient(false).parse((options.getLocation() + "/version"));
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.WARNING).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("unable to initialize Repository NutsId for repository {0}", options.getLocation()));
        }
    }

    public String getUrl(String path) {
        return CoreIOUtils.buildUrl(config().getLocation(true), path);
    }

    public NutsId getRemoteId(NutsSession session) {
        if (remoteId == null) {
            try {
                remoteId = session.id().parser().setLenient(false).parse(httpGetString(getUrl("/version"), session));
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.WARNING).verb(NutsLogVerb.FAIL)
                        .log(NutsMessage.jstyle("unable to resolve Repository NutsId for remote repository {0}", config().getLocation(false)));
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        NutsSession session = command.getSession();
        NutsContent content = lib.fetchContentImpl(command.getId(), null, session);
        NutsDescriptor desc = lib.fetchDescriptorImpl(command.getId(), session);
        if (content == null || desc == null) {
            throw new NutsNotFoundException(session, command.getId());
        }
        NutsWorkspaceUtils.checkSession(getWorkspace(), session);
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        session.descriptor().formatter(desc).print(new OutputStreamWriter(descStream));
        httpUpload(CoreIOUtils.buildUrl(config().getLocation(true), "/deploy?" + resolveAuthURLPart(session)),
                session,
                new NutsTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NutsTransportParamBinaryFilePart("content", content.getPath().getName(), content.getFilePath()),
                new NutsTransportParamParamPart("descriptor-hash", session.io().hash().sha1().setSource(desc).computeString()),
                new NutsTransportParamParamPart("content-hash", CoreIOUtils.evalSHA1Hex(content.getFilePath())),
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
        try (InputStream stream = CoreIOUtils.getHttpClientFacade(session, getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session))).open()) {
            NutsDescriptor descriptor = session.descriptor().parser().setSession(session).parse(stream);
            if (descriptor != null) {
                String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
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
    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            session.getTerminal().printProgress("search version for %s", id.getLongId(), session);
            ret = CoreIOUtils.getHttpClientFacade(session, getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session))).open();
        } catch (UncheckedIOException | NutsIOException e) {
            return IteratorUtils.emptyIterator();
        }
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret,session);
        NutsIdFilter filter2 = session.id().filter().nonnull(idFilter).and(
                session.id().filter().byName(id.getShortName())
        );
        if (filter2 != null) {
            it = IteratorBuilder.of(it).filter(CoreFilterUtils.createFilter(filter2, session)).iterator();
        }
        return it;
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }

        session.getTerminal().printProgress("search into %s " ,Arrays.toString(roots));
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth(session);
        if (filter instanceof NutsScriptAwareIdFilter) {
            String js = ((NutsScriptAwareIdFilter) filter).toJsNutsIdFilterExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)), session,
                        new NutsTransportParamParamPart("root", "/"),
                        new NutsTransportParamParamPart("ul", ulp[0]),
                        new NutsTransportParamParamPart("up", ulp[1]),
                        new NutsTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret,session)).filter(CoreFilterUtils.createFilter(filter, session)).iterator();
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
        return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret,session)).filter(CoreFilterUtils.createFilter(filter, session)).iterator();

    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id, session);
            localPath = session.io().tmp()
                    .setSession(session)
                    .setRepositoryId(getUuid())
                    .createTempFile(new File(p).getName()).toString();
        }

        try {
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session));
            session.io().copy().from(location).to(localPath).setSafe(true).setLogProgress(true).run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
            String lhash = CoreIOUtils.evalSHA1Hex(Paths.get(localPath));
            if (rhash.equalsIgnoreCase(lhash)) {
                return new NutsDefaultContent(
                        session.io().path(localPath)
                        , false, temp);
            }
        } catch (UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url, NutsSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START)
                .log(NutsMessage.jstyle("get URL{0}", url));
        return CoreIOUtils.loadString(CoreIOUtils.getHttpClientFacade(session, url).open(), true);
    }

    private InputStream httpUpload(String url, NutsSession session, NutsTransportParamPart... parts) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START)
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

        String passphrase = env().get(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
        newLogin = new String(CoreSecurityUtils.defaultEncryptChars(NutsUtilStrings.trim(newLogin).toCharArray(), passphrase));
        credentials = CoreSecurityUtils.defaultEncryptChars(credentials, passphrase);
        return new String[]{newLogin, new String(credentials)};
    }

    private String resolveAuthURLPart(NutsSession session) {
        String[] auth = resolveEncryptedAuth(session);
        return "ul=" + CoreIOUtils.urlEncodeString(auth[0]) + "&up=" + CoreIOUtils.urlEncodeString(auth[0]);
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
    private class NamedNutIdFromStreamIterator implements Iterator<NutsId> {

        private final BufferedReader br;
        private String line;
        private NutsSession session;

        public NamedNutIdFromStreamIterator(InputStream ret,NutsSession session) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
            this.session=session;
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
            NutsId nutsId = session.id().parser().setLenient(false).parse(line);
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
