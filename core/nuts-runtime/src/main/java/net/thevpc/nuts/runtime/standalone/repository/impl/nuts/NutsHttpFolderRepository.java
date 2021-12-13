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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsCachedRepository;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIteratorModel;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class NutsHttpFolderRepository extends NutsCachedRepository {

    private final NutsLogger LOG;

    private final NutsIdPathIteratorModel findModel = new NutsIdPathIteratorBase() {
        @Override
        public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("not supported undeploy."));
        }

        @Override
        public boolean isDescFile(NutsPath pathname) {
            return isDescFile0(pathname);
        }

        @Override
        public NutsDescriptor parseDescriptor(NutsPath pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, NutsPath rootURL) throws IOException {
            try {
                return NutsDescriptorParser.of(session).parse(in);
            } finally {
                in.close();
            }
        }

    };

    public NutsHttpFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, NutsSpeedQualifier.SLOW, false, NutsConstants.RepoTypes.NUTS);
        LOG = NutsLogger.of(NutsHttpFolderRepository.class, session);
    }

    private boolean isDescFile0(NutsPath path) {
        String pathname=path.toString();
        return pathname.equals(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith("/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                || pathname.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
    }


    protected InputStream getDescStream(NutsId id, NutsSession session) {
        String url = getDescPath(id, session);
//        if (CoreIOUtils.isPathHttp(url)) {
//            String message = "Downloading maven";//: "Open local file";
//            if (LOG.isLoggable(Level.FINEST)) {
//                LOG.log(Level.FINEST, CoreStringUtils.alignLeft(config().getName(), 20) + " " + message + " url " + url);
//            }
//        }
        return openStream(url, id, "artifact descriptor", session);
    }

    protected NutsPath getPath(NutsId id, NutsSession session) {
        return getIdRemotePath(id, session);
    }

    protected String getDescPath(NutsId id, NutsSession session) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String version = id.getVersion().getValue();
        return (CoreIOUtils.buildUrl(config().getLocation(true).toString(), groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/"
                + NutsConstants.Files.DESCRIPTOR_FILE_NAME
        ));
    }

    protected InputStream openStream(String path, Object source, String sourceTypeName, NutsSession session) {
        return NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(sourceTypeName).create();
    }

    protected InputStream openStream(NutsId id, String path, Object source, String sourceTypeName, NutsSession session) {
        return NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(sourceTypeName).create();
    }

    public NutsIterator<NutsId> findVersionsImplFilesFolders(NutsId id, NutsIdFilter idFilter, NutsSession session) {

        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        try {
            return IteratorBuilder.ofSupplier(
                    ()-> {
                        NutsPath artifactUrl = config().getLocation(true).resolve(groupId.replace('.', '/') + "/" + artifactId);
                        NutsPath[] all = artifactUrl.list().toArray(NutsPath[]::new);
                        List<NutsId> n = new ArrayList<>();
                        for (NutsPath versionFilesUrl : all) {
                            if (versionFilesUrl.isDirectory() && versionFilesUrl.getName().equals("LATEST")) {
                                continue;
                            }
                            NutsPath[] versionFiles = versionFilesUrl.list().toArray(NutsPath[]::new);
                            boolean validVersion = false;
                            for (NutsPath v : versionFiles) {
                                if ("nuts.properties".equals(v.getName())) {
                                    validVersion = true;
                                    break;
                                }
                            }
                            if (validVersion) {
                                NutsId id2 = id.builder().setVersion(versionFilesUrl.getName()).build();
                                if (idFilter == null || idFilter.acceptId(id2, session)) {
                                    n.add(id2);
                                }
                            }
                        }
                        return n.iterator();
                    },e->e.ofString("findVersionsImplFilesFolders"),
                    session).build();
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NutsMessage.jstyle("error find versions : {0}", ex));
//            return IteratorUtils.emptyIterator();
            return IteratorBuilder.emptyIterator();
        }

    }

    @Override
    public NutsIterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true).toString(), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );
            return IteratorBuilder.ofSupplier(
                    ()->{

                        try (InputStream metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session)) {
                            // ok found!!
                            ret.add(id);
                        } catch (UncheckedIOException | IOException ex) {
                            //ko not found
                        }
                        return ret.iterator();
                    }
                    , e-> e.ofObject()
                            .set("type","ScanURL")
                            .set("url",metadataURL)
                            .build(),
                    session).build();
        } else {
            NutsIdFilter filter2 = NutsIdFilters.of(session).nonnull(idFilter).and(
                    NutsIdFilters.of(session).byName(id.getShortName())
            );
            return findVersionsImplFilesFolders(id, filter2, session);
        }
    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        Iterator<NutsId> allVersions = searchVersionsCore(id, filter, fetchMode, session);
        NutsId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        try (InputStream stream = getDescStream(id, session)) {
            return NutsDescriptorParser.of(session).parse(stream);
        } catch (IOException | UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
        }
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localFile, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (descriptor.getLocations().length == 0) {
            NutsPath path = getPath(id, session);
            NutsCp.of(session).from(path).to(localFile).addOptions(NutsPathOption.SAFE,NutsPathOption.LOG).run();
            return new NutsDefaultContent(
                    NutsPath.of(localFile, session), false, false);
        } else {
            for (NutsIdLocation location : descriptor.getLocations()) {
                if (CoreNutsUtils.acceptClassifier(location, id.getClassifier())) {
                    try {
                        NutsCp.of(session).from(location.getUrl()).to(localFile).addOptions(NutsPathOption.SAFE,NutsPathOption.LOG).run();
                        return new NutsDefaultContent(
                                NutsPath.of(localFile, session), false, false);
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.SEVERE).error(ex)
                                .log(NutsMessage.jstyle("unable to download location for id {0} in location {1} : {2}", id, location.getUrl(), ex));
                    }
                }
            }
            return null;
        }
    }

    @Override
    public NutsIterator<NutsId> searchCore(final NutsIdFilter filter, NutsPath[] basePaths, NutsId[] baseIds, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        List<NutsIterator<? extends NutsId>> list = new ArrayList<>();
        NutsPath repoRoot = config().getLocation(true);
        for (NutsPath basePath : basePaths) {
            list.add(
                    (NutsIterator) IteratorBuilder.ofRunnable(
                            () -> session.getTerminal().printProgress("%-8s %s", "browse",
                                    (basePath == null ? repoRoot : repoRoot.resolve(basePath)).toCompressedForm()
                            ),
                            "Log",

                            session).build());
            if (basePath.getName().equals("*")) {
                list.add(new NutsIdPathIterator(this, repoRoot,basePath.getParent(),filter,session, findModel,Integer.MAX_VALUE, "core",null));
            } else {
                list.add(new NutsIdPathIterator(this, repoRoot,basePath,filter,session, findModel,2, "core",null));
            }
        }
        return IteratorUtils.concat(list);
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

}
