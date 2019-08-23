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
package net.vpc.app.nuts.core.impl.def.repos;

import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsDefaultContent;
import net.vpc.app.nuts.core.NutsPatternIdFilter;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.core.filters.id.NutsScriptAwareIdFilter;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;

public class NutsHttpSrvRepository extends NutsCachedRepository {

    private static final Logger LOG = Logger.getLogger(NutsHttpSrvRepository.class.getName());
    private NutsId remoteId;

    public NutsHttpSrvRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
        try {
            remoteId = NutsWorkspaceUtils.parseRequiredNutsId(workspace, httpGetString(options.getLocation() + "/version"));
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Unable to initialize Repository NutsId for repository {0}", options.getLocation());
        }
    }

    public String getUrl(String path) {
        return CoreIOUtils.buildUrl(config().getLocation(true), path);
    }

    public NutsId getRemoteId() {
        if (remoteId == null) {
            try {
                remoteId = NutsWorkspaceUtils.parseRequiredNutsId(getWorkspace(), httpGetString(getUrl("/version")));
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Unable to resolve Repository NutsId for remote repository {0}", config().getLocation(false));
            }
        }
        return remoteId;
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        NutsContent content = lib.fetchContentImpl(command.getId(), null, command.getSession());
        NutsDescriptor desc = lib.fetchDescriptorImpl(command.getId(), command.getSession());
        if (content == null || desc == null) {
            throw new NutsNotFoundException(getWorkspace(), command.getId());
        }
        NutsWorkspaceUtils.checkSession(getWorkspace(), command.getSession());
        if (command.getSession().getFetchMode() != NutsFetchMode.REMOTE) {
            throw new NutsIllegalArgumentException(getWorkspace(), "Offline");
        }
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        getWorkspace().descriptor().value(desc).print(new OutputStreamWriter(descStream));
        httpUpload(CoreIOUtils.buildUrl(config().getLocation(true), "/deploy?" + resolveAuthURLPart()),
                new NutsTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NutsTransportParamBinaryFilePart("content", content.getPath().getFileName().toString(), content.getPath()),
                new NutsTransportParamParamPart("descriptor-hash", getWorkspace().io().hash().sha1().source(desc).computeString()),
                new NutsTransportParamParamPart("content-hash", CoreIOUtils.evalSHA1Hex(content.getPath())),
                new NutsTransportParamParamPart("force", String.valueOf(command.getSession().getSession().isYes()))
        );
        //TODO should parse the parse
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl2(NutsId id, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            return null;
        }
        boolean transitive = session.isTransitive();
        try (InputStream stream = CoreIOUtils.getHttpClientFacade(getWorkspace(), getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open()) {
            NutsDescriptor descriptor = getWorkspace().descriptor().parse(stream);
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
    public Iterator<NutsId> searchVersionsImpl2(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            ret = CoreIOUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
        } catch (UncheckedIOException e) {
            return IteratorUtils.emptyIterator();
        }
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
        NutsIdFilter filter2 = new NutsIdFilterAnd(idFilter,
                new NutsPatternIdFilter(id.getShortNameId())
        ).simplify();
        if (filter2 != null) {
            it = IteratorBuilder.of(it).filter(CoreFilterUtils.createFilter(filter2, session.getSession())).iterator();
        }
        return it;
    }

    @Override
    public Iterator<NutsId> searchImpl2(final NutsIdFilter filter, String[] roots, NutsRepositorySession session) {

        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth();
        if (filter instanceof NutsScriptAwareIdFilter) {
            String js = ((NutsScriptAwareIdFilter) filter).toJsNutsIdFilterExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                        new NutsTransportParamParamPart("root", "/"),
                        new NutsTransportParamParamPart("ul", ulp[0]),
                        new NutsTransportParamParamPart("up", ulp[1]),
                        new NutsTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret)).filter(CoreFilterUtils.createFilter(filter, session.getSession())).iterator();
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
        return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret)).filter(CoreFilterUtils.createFilter(filter, session.getSession())).iterator();

    }

    @Override
    public NutsContent fetchContentImpl2(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id);
            localPath = getWorkspace().io().createTempFile(new File(p).getName(), this);
        }

        try {
            String location = getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart());
            getWorkspace().io().copy().session(session.getSession()).from(location).to(localPath).safeCopy().monitorable().run();
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = CoreIOUtils.evalSHA1Hex(localPath);
            if (rhash.equalsIgnoreCase(lhash)) {
                return new NutsDefaultContent(localPath, false, temp);
            }
        } catch (UncheckedIOException ex) {
            throw new NutsNotFoundException(getWorkspace(), id, ex);
            //
        }
        return null;
    }

    private String httpGetString(String url) {
        LOG.log(Level.FINEST, "Get URL{0}", url);
        return CoreIOUtils.loadString(CoreIOUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
    }

    private InputStream httpUpload(String url, NutsTransportParamPart... parts) {
        LOG.log(Level.FINEST, "Uploading URL {0}", url);
        return CoreIOUtils.getHttpClientFacade(getWorkspace(), url).upload(parts);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth() {
        String login = getWorkspace().security().getCurrentUsername();
        NutsUserConfig security = NutsRepositoryConfigManagerExt.of(config()).getUser(login);
        String newLogin = "";
        char[] credentials = new char[0];
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous".toCharArray();
        } else {
            newLogin = security.getRemoteIdentity();
            if (CoreStringUtils.isBlank(newLogin)) {
                NutsUser security2 = getWorkspace().security().findUser(login);
                if (security2 != null) {
                    newLogin = security2.getRemoteIdentity();
                }
            }
            if (CoreStringUtils.isBlank(newLogin)) {
                newLogin = login;
            } else {
                security = NutsRepositoryConfigManagerExt.of(config()).getUser(newLogin);
                if (security == null) {
                    newLogin = "anonymous";
                    credentials = "anonymous".toCharArray();
                }
            }
            if (security != null) {
                credentials = security.getRemoteCredentials() == null ? null : security.getRemoteCredentials().toCharArray();
                credentials = getWorkspace().security().getCredentials(credentials);
            }
        }

        String passphrase = config().getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE, true);
        newLogin = new String(CoreSecurityUtils.httpEncrypt(CoreStringUtils.trim(newLogin).getBytes(), passphrase));
        credentials = CoreSecurityUtils.defaultEncryptChars(credentials, passphrase);
        return new String[]{newLogin, new String(credentials)};
    }

    private String resolveAuthURLPart() {
        String[] auth = resolveEncryptedAuth();
        return "ul=" + CoreIOUtils.urlEncodeString(auth[0]) + "&up=" + CoreIOUtils.urlEncodeString(auth[0]);
    }

//    @Override
//    public void undeployImpl(NutsRepositoryUndeployCommand options) {
//        throw new NutsUnsupportedOperationException(getWorkspace());
//    }
//    @Override
//    public void checkAllowedFetch(NutsId parse, NutsRepositorySession session) {
//        super.checkAllowedFetch(parse, session);
//        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
//            throw new NutsNotFoundException(getWorkspace(), parse);
//        }
//    }
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
            NutsId nutsId = getWorkspace().id().parseRequired(line);
            return nutsId.builder().setNamespace(config().getName()).build();
        }
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
}
