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
// *
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
//import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
//import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
//import net.thevpc.nuts.runtime.core.filters.NutsSearchIdById;
//import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
//
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.Stack;
//import java.util.logging.Level;
//
///**
// * Created by vpc on 2/21/17.
// */
//public class FolderNutIdIterator implements Iterator<NutsId> {
//    private final String repository;
//    private final Stack<PathAndDepth> stack = new Stack<>();
//    private final NutsIdFilter filter;
//    private final NutsSession session;
//    private final FolderNutIdIteratorModel model;
//    private final int maxDepth;
//    private NutsId last;
//    private long visitedFoldersCount;
//    private long visitedFilesCount;
//    private final NutsPath basePath;
//    private final NutsPath rootFolder;
//    public FolderNutIdIterator(String repository, NutsPath rootFolder, NutsPath basePath, NutsIdFilter filter, NutsSession session, FolderNutIdIteratorModel model, int maxDepth) {
//        this.repository = repository;
//        this.session = session;
//        this.filter = filter;
//        this.model = model;
//        this.maxDepth = maxDepth;
//        if (rootFolder == null) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.plain("could not iterate over null rootFolder"));
//        }
//        this.basePath = basePath;
//        this.rootFolder = rootFolder;
//        if (basePath.exists() && basePath.isDirectory()) {
//            stack.push(new PathAndDepth(basePath, 0));
//        }
//    }
//
//    @Override
//    public boolean hasNext() {
//        last = null;
//        while (!stack.isEmpty()) {
//            PathAndDepth file = stack.pop();
//            if (file.path.isDirectory()) {
//                session.getTerminal().printProgress("%-8s %s", "search", file.path.toCompressedForm());
//                visitedFoldersCount++;
//                boolean deep = file.depth < maxDepth;
//                if (file.path.isDirectory()) {
//                    try {
//                        file.path.list()
//                                .filter(pathname -> {
//                                    try {
//                                        return (deep && pathname.isDirectory()) || model.isDescFile(pathname);
//                                    } catch (Exception ex) {
//                                        NutsLoggerOp.of(FolderNutIdIterator.class, session).level(Level.FINE).error(ex)
//                                                .log(NutsMessage.jstyle("unable to test desc file {0}", pathname));
//                                        return false;
//                                    }
//                                }).forEach(
//                                        item -> {
//                                            if (item.isDirectory()) {
//                                                if (file.depth < maxDepth) {
//                                                    stack.push(new PathAndDepth(item, file.depth + 1));
//                                                }
//                                            } else {
//                                                stack.push(new PathAndDepth(item, file.depth));
//                                            }
//                                        }
//                                );
//                    } catch (NutsException ex) {
//                        throw ex;
//                    } catch (Exception ex) {
//                        NutsLoggerOp.of(FolderNutIdIterator.class, session).level(Level.FINEST).error(ex)
//                                .log(NutsMessage.jstyle("unable to iterate {0}", file.path));
//                    }
//                }
//            } else {
//                visitedFilesCount++;
//                NutsId t = null;
//                try {
//                    t = model.parseId(file.path, rootFolder, filter, repository, session);
//                } catch (Exception ex) {
//                    NutsLoggerOp.of(FolderNutIdIterator.class, session).level(Level.FINEST).error(ex)
//                            .log(NutsMessage.jstyle("unable to parse id from file {0}", file.path));
//                }
//                if (t != null) {
//                    last = t;
//                    return true;
//                }
//            }
//        }
//        return last != null;
//    }
//
//    @Override
//    public NutsId next() {
//        NutsId ret = last;
//        last = null;
//        return ret;
//    }
//
//    @Override
//    public void remove() {
//        if (last != null) {
//            model.undeploy(last, session);
//        }
//        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported remove"));
//    }
//
//    @Override
//    public String toString() {
//        return "FolderNutIdIterator{" +
//                "repository='" + repository + '\'' +
//                ", folder=" + basePath +
//                '}';
//    }
//
//    public long getVisitedFoldersCount() {
//        return visitedFoldersCount;
//    }
//
//    public long getVisitedFilesCount() {
//        return visitedFilesCount;
//    }
//
//    public interface FolderNutIdIteratorModel {
//
//        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;
//
//        boolean isDescFile(NutsPath pathname);
//
//        NutsDescriptor parseDescriptor(NutsPath pathname, NutsSession session) throws IOException;
//
//        NutsId parseId(NutsPath pathname, NutsPath rootPath, NutsIdFilter filter, String repository, NutsSession session) throws IOException;
//
//
//    }
//
//    private static class PathAndDepth {
//
//        private final NutsPath path;
//        private final int depth;
//
//        public PathAndDepth(NutsPath path, int depth) {
//            this.path = path;
//            this.depth = depth;
//        }
//
//    }
//
//    public static abstract class AbstractFolderNutIdIteratorModel implements FolderNutIdIteratorModel {
//
//        public NutsId validate(NutsId id, NutsDescriptor t, NutsIdFilter filter, String repository, NutsSession session) {
//            if (t != null) {
//                if (!CoreNutsUtils.isEffectiveId(t.getId())) {
//                    NutsDescriptor nutsDescriptor = null;
//                    try {
//                        nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
//                    } catch (Exception e) {
//                        //throw new NutsException(e);
//                    }
//                    t = nutsDescriptor;
//                }
//                if (t != null && (filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
//                    NutsId nutsId = t.getId().builder().setRepository(repository).build();
//                    return nutsId;
//                }
//            }
//            if (id != null) {
//                if (filter == null || filter.acceptSearchId(new NutsSearchIdById(id), session)) {
//                    NutsId nutsId = id.builder().setRepository(repository).build();
//                    return nutsId;
//                }
//            }
//            return null;
//        }
//
//        public NutsId parseId(NutsPath pathname, NutsPath rootPath, NutsIdFilter filter, String repository, NutsSession session) throws IOException {
//            NutsDescriptor t = null;
//            try {
//                t = parseDescriptor(pathname, session);
//            } catch (Exception e) {
//                //e.printStackTrace();
//            }
//            return validate(null, t, filter, repository, session);
//        }
//    }
//}
