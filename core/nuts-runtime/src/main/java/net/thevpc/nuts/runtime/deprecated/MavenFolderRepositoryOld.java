///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// * <p>
// * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
// * or agreed to in writing, software distributed under the License is
// * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
//package net.thevpc.nuts.runtime.deprecated;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
//import net.thevpc.nuts.runtime.standalone.bridges.maven.AbstractMavenRepositoryHelper;
//import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
//import net.thevpc.nuts.runtime.standalone.repos.NutsCachedRepository;
//import net.thevpc.nuts.runtime.standalone.repos.NutsIdPathIterator;
//import net.thevpc.nuts.runtime.standalone.repos.NutsIdPathIteratorBase;
//import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.FileVisitResult;
//import java.nio.file.FileVisitor;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created by vpc on 1/5/17.
// */
//public class MavenFolderRepositoryOld extends NutsCachedRepository {
//
//    protected final NutsLogger LOG;
//    private final AbstractMavenRepositoryHelper repoHelper = new RepoHelper();
//    private final RepoIter repoIter = new RepoIter();
//
//    public MavenFolderRepositoryOld(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
//        super(options, session, parentRepository,
//                NutsPath.of(options.getConfig().getLocation(),session).isRemote() ?NutsSpeedQualifier.SLOW:NutsSpeedQualifier.FASTER,
//                false, NutsConstants.RepoTypes.MAVEN);
//        LOG = NutsLogger.of(MavenFolderRepositoryOld.class, session);
//        if(!isRemote()) {
//            if (options.getConfig().getStoreLocationStrategy() != NutsStoreLocationStrategy.STANDALONE) {
//                cache.setWriteEnabled(false);
//                cache.setReadEnabled(false);
//            }
//        }
//    }
//
//    @Override
//    protected boolean isSupportedDeployImpl() {
//        return false;
//    }
//
//    @Override
//    protected boolean isAvailableImpl() {
//        try {
//            NutsPath loc = config().setSession(initSession).getLocation(true);
//            return loc.isDirectory();
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private NutsPath getLocationAsPath(NutsSession session) {
//        return config().setSession(session).getLocation(true);
//    }
//
//    //    @Override
////    public Path getComponentsLocation() {
////        return null;
////    }
//    public NutsPath getIdFile(NutsId id, NutsSession session) {
//        NutsPath p = getIdRelativePath(id, session);
//        if (p != null) {
//            return getLocationAsPath(session).resolve(p);
//        }
//        return null;
//    }
//
//    protected NutsPath getLocalGroupAndArtifactFile(NutsId id, NutsSession session) {
//        NutsWorkspaceUtils.of(session).checkShortId(id);
//        NutsPath groupFolder = getLocationAsPath(session).resolve(id.getGroupId().replace('.', File.separatorChar));
//        return groupFolder.resolve(id.getArtifactId());
//    }
//
//    @Override
//    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
//
//        Iterator<NutsId> namedNutIdIterator = null;
////        StringBuilder errors = new StringBuilder();
//        if (fetchMode != NutsFetchMode.REMOTE) {
//            if (id.getVersion().isSingleValue()) {
//                NutsPath f = getIdFile(id.builder().setFaceDescriptor().build(), session);
//                if (f != null && f.isRegularFile()) {
////                    NutsDescriptor d = null;
////                    try {
////                        d = MavenUtils.of(session).parsePomXml(f, fetchMode, this, session);
////                    } catch (Exception ex) {
////                        LOG.with().session(session).level(Level.SEVERE).error(ex)
////                                .log("failed to parse pom file {0} : {1}", f, ex);
////                        //
////                    }
////                    if (d != null) {
//                    return Collections.singletonList(id.builder().setRepository(getName()).build()).iterator();
////                    }
//                }
////                return IteratorUtils.emptyIterator();
//                return null;
//            }
//            try {
//                namedNutIdIterator = findInFolder(getLocationAsPath(session), getLocalGroupAndArtifactFile(id, session),
//                        NutsIdFilters.of(session).nonnull(idFilter).and(
//                                NutsIdFilters.of(session).byName(id.getShortName())
//                        ),
//                        Integer.MAX_VALUE, session);
//            } catch (NutsNotFoundException ex) {
////                errors.append(ex).append(" \n");
//            }
//        }
////        if (namedNutIdIterator == null) {
////            return IteratorUtils.emptyIterator();
////        }
//        return namedNutIdIterator;
//
//    }
//
//    @Override
//    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
//        if (id.getVersion().isBlank() && filter == null) {
//            NutsPath file = getLocalGroupAndArtifactFile(id, session);
//            final NutsId[] bestId = new NutsId[1];
//            if (file.isDirectory()) {
//                file.list().filter(x -> x.isDirectory())
//                        .forEach(versionPath -> {
//                            NutsId id2 = id.builder().setVersion(versionPath.getName()).build();
//                            String fn = getIdFilename(id2.builder().setFaceDescriptor().build(), session);
//                            if (versionPath.resolve(fn).exists()) {
//                                if (bestId[0] == null || id2.getVersion().compareTo(bestId[0].getVersion()) > 0) {
//                                    bestId[0] = id2;
//                                }
//                            }
//                        });
//            }
//            return bestId[0];
//        }
//        return super.searchLatestVersion(id, filter, fetchMode, session);
//    }
//
//    @Override
//    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
//        if (!acceptedFetch(fetchMode)) {
//            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
//        }
//        return repoHelper.fetchDescriptorImpl(id, fetchMode, session);
//    }
//
//    @Override
//    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
//        if (!acceptedFetch(fetchMode)) {
//            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
//        }
//        NutsPath f = getIdFile(id, session);
//        if (f == null) {
//            throw new NutsNotFoundException(session, id, new RuntimeException("Invalid id"));
//        }
//        if (!f.exists()) {
//            throw new NutsNotFoundException(session, id, new IOException("File not found : " + f));
//        }
//        if (localPath == null) {
//            return new NutsDefaultContent(f, true, false);
//        } else {
//            NutsCp.of(session)
//                    .from(f).to(localPath).addOptions(NutsPathOption.SAFE).run();
//            return new NutsDefaultContent(
//                    NutsPath.of(localPath, session), true, false);
//        }
//    }
//
//    @Override
//    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
//        if (!acceptedFetch(fetchMode)) {
//            return null;
//        }
//        List<Iterator<? extends NutsId>> list = new ArrayList<>();
//        for (String root : roots) {
//            session.getTerminal().printProgress("%-8s %s", "browse", NutsPath.of(root, session).toCompressedForm());
//            if (root.endsWith("/*")) {
//                String name = root.substring(0, root.length() - 2);
//                list.add(new NutsIdPathIterator(this, config().getLocation(true), name, filter, session, repoIter, Integer.MAX_VALUE));
//            } else {
//                list.add(new NutsIdPathIterator(this, config().getLocation(true), root, filter, session, repoIter, 2));
//            }
//        }
//        return IteratorUtils.concat(list);
//    }
//
//    @Override
//    public void updateStatistics2(NutsSession session) {
//        try {
//            Files.walkFileTree(config().getLocation(true).toFile(), new FileVisitor<Path>() {
//                        @Override
//                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//
//                            return FileVisitResult.CONTINUE;
//                        }
//
//                        @Override
//                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                            throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
//                        }
//
//                        @Override
//                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                            throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
//                        }
//
//                        @Override
//                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                            throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
//                        }
//                    }
//            );
//        } catch (IOException ex) {
//            throw new NutsIOException(session, ex);
//        }
//    }
//
//    @Override
//    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
//        return isRemote() || mode == NutsFetchMode.LOCAL;
//    }
//
//    @Override
//    public boolean isRemote() {
//        return config().getLocation(true).isRemote();
//    }
//
//    protected boolean acceptedFetch(NutsFetchMode fetchMode) {
//        return (fetchMode == NutsFetchMode.REMOTE) == isRemote();
//    }
//
//    protected Iterator<NutsId> findInFolder(NutsPath rootPath, NutsPath folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
//        if (folder == null || !folder.exists() || !folder.isDirectory()) {
//            return null;//IteratorUtils.emptyIterator();
//        }
//        return new NutsIdPathIterator(this, rootPath, folder.toString(), filter, session, repoIter, maxDepth);
//    }
//
//    @Override
//    protected String getIdExtension(NutsId id, NutsSession session) {
//        return repoHelper.getIdExtension(id, session);
//    }
//
//    class RepoIter extends NutsIdPathIteratorBase {
//        @Override
//        public void undeploy(NutsId id, NutsSession session) {
//            MavenFolderRepositoryOld.this.undeploy().setId(id).setSession(session)
//                    //.setFetchMode(NutsFetchMode.LOCAL)
//                    .run();
//        }
//
//        @Override
//        public boolean isDescFile(NutsPath pathname) {
//            return pathname.getName().endsWith(".pom");
//        }
//
//        @Override
//        public NutsDescriptor parseDescriptor(NutsPath pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, NutsPath rootURL) throws IOException {
//            return MavenUtils.of(session).parsePomXmlAndResolveParents(pathname, NutsFetchMode.LOCAL, MavenFolderRepositoryOld.this);
//        }
//
//        @Override
//        public NutsId parseId(NutsPath pomFile, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
//            pomFile = pomFile.normalize();
//            rootPath = rootPath.normalize();
//            if (pomFile.isRegularFile()) {
//                String fn = pomFile.getName();
//                if (fn.endsWith(".pom")) {
//                    NutsPath versionFolder = pomFile.getParent();
//                    if (versionFolder != null) {
//                        NutsPath vn = versionFolder;
//                        NutsPath artifactFolder = versionFolder.getParent();
//                        if (artifactFolder != null) {
//                            String an = artifactFolder.getName();
//                            if (fn.equals(an + "-" + vn + ".pom")) {
//                                NutsPath groupFolder = artifactFolder.getParent();
//                                if (groupFolder != null) {
//                                    NutsPath gg = groupFolder.subpath(rootPath.getPathCount(), groupFolder.getPathCount());
//                                    StringBuilder gn = new StringBuilder();
//                                    for (int i = 0; i < gg.getPathCount(); i++) {
//                                        String ns = gg.getItem(i);
//                                        if (i > 0) {
//                                            gn.append('.');
//                                        }
//                                        gn.append(ns);
//                                    }
//                                    return validate(
//                                            NutsIdBuilder.of(session)
//                                                    .setGroupId(gn.toString())
//                                                    .setArtifactId(an)
//                                                    .setVersion(vn.getName())
//                                                    .build()
//                                            , null, pomFile, rootPath, filter, repository, session);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            return null;
//        }
//    }
//
//
//    private class RepoHelper extends AbstractMavenRepositoryHelper {
//        public RepoHelper() {
//            super(MavenFolderRepositoryOld.this);
//        }
//
//        @Override
//        protected NutsPath getIdPath(NutsId id, NutsSession session) {
//            return getLocationAsPath(session).resolve(getIdRelativePath(id, session));
//        }
//
//        @Override
//        protected boolean exists(NutsId id, NutsPath path, Object source, String typeName, NutsSession session) {
//            session.getTerminal().printProgress("%-8s %s", "search", path.toCompressedForm());
//            return path.isRegularFile();
//        }
//
//        @Override
//        protected InputStream openStream(NutsId id, NutsPath path, Object source, String typeName, String action, NutsSession session) {
//            return path.getInputStream();
//        }
//
//        @Override
//        public boolean isRemoteRepository() {
//            return MavenFolderRepositoryOld.this.isRemote();
//        }
//    }
//}
