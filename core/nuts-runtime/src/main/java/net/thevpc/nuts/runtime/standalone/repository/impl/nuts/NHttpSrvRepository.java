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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.env.NSpeedQualifier;
import net.thevpc.nuts.env.NUser;
import net.thevpc.nuts.env.NUserConfig;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.util.NIdLocationUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.util.NIteratorBase;
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
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.*;

public class NHttpSrvRepository extends NCachedRepository {

//    private final NLog LOG;
    private NId remoteId;

    public NHttpSrvRepository(NAddRepositoryOptions options, NWorkspace workspace, NRepository parentRepository) {
        super(options, workspace, parentRepository, NSpeedQualifier.SLOW, false, NConstants.RepoTypes.NUTS, true);
        try {
            remoteId = getRemoteId();
        } catch (Exception ex) {
            LOG().with().level(Level.WARNING).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("unable to initialize Repository NutsId for repository {0}", options.getLocation()));
        }
    }

    protected NLog LOG() {
        return NLog.of(NHttpSrvRepository.class);
    }

    public String getUrl(String path) {
        return config().getLocationPath().resolve(path).toString();
    }

    public NId getRemoteId() {
        if (remoteId == null) {
            try {
                NSession session = getWorkspace().currentSession();
                remoteId = NId.of(httpGetString(getUrl("/version"), session)).get();
            } catch (Exception ex) {
                LOG().with().level(Level.WARNING).verb(NLogVerb.FAIL)
                        .log(NMsg.ofJ("unable to resolve Repository NutsId for remote repository {0}", config().getLocation()));
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NPushRepositoryCmd command) {
        NSession session=getWorkspace().currentSession();
        NPath content = lib.fetchContentImpl(command.getId());
        NDescriptor desc = lib.fetchDescriptorImpl(command.getId());
        if (content == null || desc == null) {
            throw new NNotFoundException(command.getId());
        }
        NSessionUtils.checkSession(getWorkspace(), session);
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        NDescriptorFormat.of(desc).print(new OutputStreamWriter(descStream));
        NWebCli nWebCli = NWebCli.of();
        nWebCli.req().post()
                .setUrl(CoreIOUtils.buildUrl(config().getLocationPath().toString(), "/deploy?" + resolveAuthURLPart(session)))
                .addPart("descriptor-hash", NDigest.of().sha1().setSource(desc).computeString())
                .addPart("content-hash", NDigestUtils.evalSHA1Hex(content))
                .addPart("force", NDigestUtils.evalSHA1Hex(content))
                .addPart().setName("descriptor").setFileName("Project.nuts").setBody(
                        NInputSource.of(descStream.toByteArray())).end()
                .run();
    }

    @Override
    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        NSession session=getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        session.getTerminal().printProgress(NMsg.ofC("loading descriptor for %s", id.getLongId()));
        try (InputStream stream = NPath.of(getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session))).getInputStream()) {
            NDescriptor descriptor = NDescriptorParser.of().parse(stream).get();
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
    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        NSession session=getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            session.getTerminal().printProgress(NMsg.ofC("search version for %s", id.getLongId()));
            ret = NPath.of(getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session))).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            return NIteratorBuilder.emptyIterator();
        }
        NIterator<NId> it = new NamedNIdFromStreamIterator(ret, session);
        NIdFilter filter2 = NIdFilters.of().nonnull(idFilter).and(
                NIdFilters.of().byName(id.getShortName())
        );
        if (filter2 != null) {
            it = NIteratorBuilder.of(it).filter(CoreFilterUtils.createFilter(filter2)).iterator();
        }
        return it;
    }

    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        NSession session=getWorkspace().currentSession();

        session.getTerminal().printProgress(NMsg.ofC("search into %s ", Arrays.toString(basePaths)));
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth(session);
        if (filter instanceof NExprIdFilter) {
            String js = ((NExprIdFilter) filter).toExpr();
            if (js != null) {
                NWebCli nWebCli = NWebCli.of();
                ret = nWebCli.req().post()
                        .setUrl(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)))
                        .addPart("root", "/")
                        .addPart("ul", ulp[0])
                        .addPart("up", ulp[1])
                        .addPart("js").setFileName("search.js").setBody(
                                NInputSource.of(js.getBytes())).end()
                        .run()
                        .getContent().getInputStream();
                return NIteratorBuilder.of(new NamedNIdFromStreamIterator(ret, session)).filter(CoreFilterUtils.createFilter(filter)).iterator();
            }
        } else {
            NWebCli nWebCli = NWebCli.of();
            ret = nWebCli.req().post()
                    .setUrl(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart(session)))
                    .addPart("root", "/")
                    .addPart("ul", ulp[0])
                    .addPart("up", ulp[1])
                    .addPart("pattern", ("*"))
                    .addPart("transitive", String.valueOf(transitive))
                    .run()
                    .getContent().getInputStream();
        }
        if (filter == null) {
            return new NamedNIdFromStreamIterator(ret, session);
        }
        return NIteratorBuilder.of(new NamedNIdFromStreamIterator(ret, session)).filter(CoreFilterUtils.createFilter(filter)).iterator();

    }

    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        NPath localPath=NIdLocationUtils.fetch(id, descriptor.getLocations(), this, session);
        if (localPath!=null) {
            return localPath;
        }
        boolean transitive = session.isTransitive();

        try {
            localPath = NPath.ofTempRepositoryFile(new File(this.getIdFilename(id)).getName(), this);
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session));
            NCp.of().from(
                    NPath.of(location)
            ).to(localPath).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart(session)), session);
            String lhash = NDigestUtils.evalSHA1Hex(localPath);
            if (rhash.equalsIgnoreCase(lhash)) {
                return localPath.setUserCache(false);
            }
        } catch (UncheckedIOException | NIOException ex) {
            throw new NNotFoundException(id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url, NSession session) {
        LOG().with().level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("get URL{0}", url));
        return NIOUtils.loadString(NPath.of(url).getInputStream(), true);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth(NSession session) {
        String login = NWorkspaceSecurityManager.of().getCurrentUsername();
        NUserConfig security = NRepositoryConfigManagerExt.of(config()).getModel().findUser(login).orNull();
        String newLogin = "";
        char[] credentials = new char[0];
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous".toCharArray();
        } else {
            newLogin = security.getRemoteIdentity();
            if (NBlankable.isBlank(newLogin)) {
                NUser security2 = NWorkspaceSecurityManager.of().findUser(login);
                if (security2 != null) {
                    newLogin = security2.getRemoteIdentity();
                }
            }
            if (NBlankable.isBlank(newLogin)) {
                newLogin = login;
            } else {
                security = NRepositoryConfigManagerExt.of(config()).getModel().findUser(newLogin).orNull();
                if (security == null) {
                    newLogin = "anonymous";
                    credentials = "anonymous".toCharArray();
                }
            }
            if (security != null) {
                credentials = security.getRemoteCredentials() == null ? null : security.getRemoteCredentials().toCharArray();
                credentials = NWorkspaceSecurityManager.of().getCredentials(credentials);
            }
        }

        String passphrase = config().getConfigProperty(CoreSecurityUtils.ENV_KEY_PASSPHRASE)
                .flatMap(NLiteral::asString)
                .orElse(CoreSecurityUtils.DEFAULT_PASSPHRASE);
        newLogin = new String(CoreSecurityUtils.defaultEncryptChars(NStringUtils.trim(newLogin).toCharArray(), passphrase));
        credentials = CoreSecurityUtils.defaultEncryptChars(credentials, passphrase);
        return new String[]{newLogin, new String(credentials)};
    }

    private String resolveAuthURLPart(NSession session) {
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
        public NElement describe() {
            return NElements.of().ofObject()
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
            NId nutsId = NId.of(line).get();
            return nutsId.builder().setRepository(getName()).build();
        }
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
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
