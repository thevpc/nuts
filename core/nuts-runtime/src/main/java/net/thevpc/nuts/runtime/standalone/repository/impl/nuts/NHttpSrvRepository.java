/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NSpeedQualifier;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.command.NFetchModeNotSupportedException;
import net.thevpc.nuts.net.NWebCli;
import net.thevpc.nuts.core.NAddRepositoryOptions;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.net.NWebRequest;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.util.NIdLocationUtils;
import net.thevpc.nuts.util.NIteratorBase;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.NCachedRepository;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.logging.Level;

import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

public class NHttpSrvRepository extends NCachedRepository {

    //    private final NLog LOG;
    private NId remoteId;

    public NHttpSrvRepository(NAddRepositoryOptions options, NRepository parentRepository) {
        super(options, parentRepository, NSpeedQualifier.SLOW, false, NConstants.RepoTypes.NUTS, true);
        try {
            remoteId = getRemoteId();
        } catch (Exception ex) {
            LOG()
                    .log(NMsg.ofJ("unable to initialize Repository NutsId for repository {0}", options.getLocation()).withLevel(Level.WARNING).withIntent(NMsgIntent.FAIL));
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
                remoteId = NId.get(httpGetString(getUrl("/version"))).get();
            } catch (Exception ex) {
                LOG()
                        .log(NMsg.ofJ("unable to resolve Repository NutsId for remote repository {0}", config().getLocation())
                                .withLevel(Level.WARNING).withIntent(NMsgIntent.FAIL)
                        );
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NPushRepositoryCmd command) {
        NPath content = lib.fetchContentImpl(command.getId());
        NDescriptor desc = lib.fetchDescriptorImpl(command.getId());
        if (content == null || desc == null) {
            throw new NArtifactNotFoundException(command.getId());
        }
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        NDescriptorWriter.of().print(desc, new OutputStreamWriter(descStream));
        NWebCli nWebCli = NWebCli.of();
        nWebCli.req().POST()
                .setUrl(CoreIOUtils.buildUrl(config().getLocationPath().toString(), "/deploy?" + resolveAuthURLPart()))
                .addPart("descriptor-hash", NDigest.of().sha1().setSource(desc).computeString())
                .addPart("content-hash", NDigestUtils.evalSHA1Hex(content))
                .addPart("force", NDigestUtils.evalSHA1Hex(content))
                .addPart().setName("descriptor").setFileName("Project.nuts").setBody(
                        NInputSource.of(descStream.toByteArray())).end()
                .run()
        ;
    }

    @Override
    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NArtifactNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        session.getTerminal().printProgress(NMsg.ofC("loading descriptor for %s", id.getLongId()));
        try (InputStream stream = NPath.of(getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).getInputStream()) {
            NDescriptor descriptor = NDescriptorParser.of().parse(stream).get();
            if (descriptor != null) {
                String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
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
    public NIterator<NId> searchVersionsCore(NId id, NDefinitionFilter idFilter, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NArtifactNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            session.getTerminal().printProgress(NMsg.ofC("search version for %s", id.getLongId()));
            ret = NPath.of(getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            return NIteratorBuilder.emptyIterator();
        }
        NIterator<NId> it = new NamedNIdFromStreamIterator(ret);
        NDefinitionFilter filter2 = NDefinitionFilters.of().nonnull(idFilter).and(
                NDefinitionFilters.of().byName(id.getShortName())
        );
        if (filter2 != null) {
            it = NIteratorBuilder.of(it).filter(NDefinitionFilterUtils.toIdPredicate(filter2)).iterator();
        }
        return it;
    }

    @Override
    public NIterator<NId> searchCore(final NDefinitionFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        NSession session = getWorkspace().currentSession();

        session.getTerminal().printProgress(NMsg.ofC("search into %s ", Arrays.toString(basePaths)));
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        Creds ulp = resolveEncryptedAuth();
        if (filter instanceof NExprIdFilter) {
            String js = ((NExprIdFilter) filter).toExpr();
            if (js != null) {
                NWebCli nWebCli = NWebCli.of();
                ret = nWebCli.req().POST()
                        .setUrl(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()))
                        .addPart("root", "/")
                        .doWith(r->prepareNWebRequest(r,ulp))
                        .addPart("js").setFileName("search.js").setBody(
                                NInputSource.of(js.getBytes())).end()
                        .run()
                        .getContent().getInputStream();
                return NIteratorBuilder.of(new NamedNIdFromStreamIterator(ret)).filter(NDefinitionFilterUtils.toIdPredicate(filter)).iterator();
            }
        } else {
            NWebCli nWebCli = NWebCli.of();
            ret = nWebCli.req().POST()
                    .setUrl(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()))
                    .addPart("root", "/")
                    .doWith(r->prepareNWebRequest(r,ulp))
                    .addPart("pattern", ("*"))
                    .addPart("transitive", String.valueOf(transitive))
                    .run()
                    .getContent().getInputStream();
        }
        if (filter == null) {
            return new NamedNIdFromStreamIterator(ret);
        }
        return NIteratorBuilder.of(new NamedNIdFromStreamIterator(ret)).filter(NDefinitionFilterUtils.toIdPredicate(filter)).iterator();
    }

    private NWebRequest prepareNWebRequest(NWebRequest r, Creds c) {
        if (c.password == null) {
            return r;
        }
        NSecurityManager.of().runWithSecret(c.password, new NSecretRunner() {
            @Override
            public void run(NSecureToken id, NSecureString secretm, Function<String, String> env) {
                secretm.doWithContent(cc->{
                    r.addPart("ul", c.login)
                            .addPart("up", new String(cc));
                });
            }
        });
        return r;
    }

    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            throw new NArtifactNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        NPath localPath = NIdLocationUtils.fetch(id, descriptor.getLocations(), this);
        if (localPath != null) {
            return localPath;
        }
        boolean transitive = session.isTransitive();

        try {
            localPath = NPath.ofTempRepositoryFile(new File(this.getIdFilename(id)).getName(), this);
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart());
            NCp.of().from(
                    NPath.of(location)
            ).to(localPath).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = NDigestUtils.evalSHA1Hex(localPath);
            if (rhash.equalsIgnoreCase(lhash)) {
                return localPath.setUserCache(false);
            }
        } catch (UncheckedIOException | NIOException ex) {
            throw new NArtifactNotFoundException(id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url) {
        LOG().log(NMsg.ofJ("get URL{0}", url)
                .withLevel(Level.FINEST).withIntent(NMsgIntent.START));
        return NIOUtils.loadString(NPath.of(url).getInputStream(), true);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private static class Creds {
        String login;
        NSecureToken password;

        public Creds(String login, NSecureToken password) {
            this.login = login;
            this.password = password;
        }
    }

    private Creds resolveEncryptedAuth() {
        String login = NSecurityManager.of().getCurrentUsername();
        NRepositoryAccess security = NSecurityManager.of().findRepositoryAccess(getUuid(), login).get();
        String newLogin = "";
        NSecureToken credentials = null;
        if (security == null) {
            newLogin = "anonymous";
        } else {
            newLogin = security.getRemoteUserName();
            if (NBlankable.isBlank(newLogin)) {
                newLogin = login;
            }
            if (NBlankable.isBlank(newLogin)) {
                newLogin = login;
            }
            credentials = security.getRemoteCredential();
        }
        return new Creds(newLogin, credentials);
    }

    private String resolveAuthURLPart() {
        Creds auth = resolveEncryptedAuth();
        NRef<String> s = NRef.of();
        if (auth.password == null) {
            return "";
        }
        NSecurityManager.of().runWithSecret(auth.password, new NSecretRunner() {
            @Override
            public void run(NSecureToken id, NSecureString secretm, Function<String, String> env) {
                secretm.doWithContent(cc->{
                    s.set("ul=" + CoreIOUtils.urlEncodeString(auth.login) + "&up=" + CoreIOUtils.urlEncodeString(new String(cc)));
                });
            }
        });
        return s.get();
    }

    //    @Override
//    public void undeployImpl(NutsRepositoryUndeployCommand options) {
//        throw new NutsUnsupportedOperationException(session);
//    }
//    @Override
//    public void checkAllowedFetch(NutsId parse) {
//        super.checkAllowedFetch(parse, session);
//        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
//            throw new NArtifactNotFoundException(session(), parse);
//        }
//    }
    private class NamedNIdFromStreamIterator extends NIteratorBase<NId> {

        private final BufferedReader br;
        private String line;
        private InputStream source0;

        public NamedNIdFromStreamIterator(InputStream ret) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
        }

        @Override
        public NElement describe() {
            return NElement.ofObjectBuilder()
                    .name("ScanArchetypeCatalog")
                    .set("source", source0.toString())
                    .build();
        }


        @Override
        public boolean hasNextImpl() {
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

        public void close() {
            super.close();
            try {
                br.close();
            } catch (IOException ex) {
                //
            }
        }

        @Override
        public NId next() {
            NId nutsId = NId.get(line).get();
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
