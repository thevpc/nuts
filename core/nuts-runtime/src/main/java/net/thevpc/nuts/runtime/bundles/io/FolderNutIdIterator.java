/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdById;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 * Created by vpc on 2/21/17.
 */
public class FolderNutIdIterator implements Iterator<NutsId> {
//    private static final Logger LOG=Logger.getLogger(FolderNutIdIterator.class.getName());

    private final String repository;
    private NutsId last;

    private static class PathAndDepth {

        private Path path;
        private int depth;

        public PathAndDepth(Path path, int depth) {
            this.path = path;
            this.depth = depth;
        }

    }
    private final Stack<PathAndDepth> stack = new Stack<>();
    private final NutsIdFilter filter;
    private final NutsSession session;
    private final NutsWorkspace workspace;
    private final FolderNutIdIteratorModel model;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private int maxDepth;
    private Path folder;
    private Path rootFolder;

    public FolderNutIdIterator(String repository, Path folder, Path rootFolder,NutsIdFilter filter, NutsSession session, FolderNutIdIteratorModel model, int maxDepth) {
        this.repository = repository;
        this.session = session;
        this.filter = filter;
        this.workspace = session.getWorkspace();
        this.model = model;
        this.maxDepth = maxDepth;
        if (folder == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        this.folder = folder;
        this.rootFolder = rootFolder;
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            stack.push(new PathAndDepth(folder, 0));
        }
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.pop();
            if (Files.isDirectory(file.path)) {
                session.getTerminal().printProgress("%-8s %s", "search",session.io().path(file.path.toString()).toCompressedForm());
                visitedFoldersCount++;
                boolean deep = file.depth < maxDepth;
                if (Files.isDirectory(file.path)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.path, new DirectoryStream.Filter<Path>() {
                        @Override
                        public boolean accept(Path pathname) throws IOException {
                            try {
                                return (deep && Files.isDirectory(pathname)) || model.isDescFile(pathname);
                            } catch (Exception ex) {
                                session.log().of(FolderNutIdIterator.class).with().session(session).level(Level.FINE).error(ex)
                                        .log(NutsMessage.jstyle("unable to test desc file {0}", pathname));
                                return false;
                            }
                        }
                    })) {

                        for (Path item : stream) {
                            if (Files.isDirectory(item)) {
                                if (file.depth < maxDepth) {
                                    stack.push(new PathAndDepth(item, file.depth + 1));
                                }
                            } else {
                                stack.push(new PathAndDepth(item, file.depth));
                            }
                        }
                    } catch (IOException ex) {
                        session.log().of(FolderNutIdIterator.class).with().session(session).level(Level.FINEST).error(ex)
                                .log(NutsMessage.jstyle("unable to iterate {0}", file.path));
                    }
                }
            } else {
                visitedFilesCount++;
                NutsId t = null;
                try {
                    t = model.parseId(file.path, rootFolder, filter, repository, session);
                } catch (Exception ex) {
                    session.log().of(FolderNutIdIterator.class).with().session(session).level(Level.FINEST).error(ex)
                            .log(NutsMessage.jstyle("unable to parse id from file {0}", file.path));
                }
                if (t != null) {
                    last = t;
                    return true;
                }
            }
        }
        return last != null;
    }

    @Override
    public NutsId next() {
        NutsId ret = last;
        last = null;
        return ret;
    }

    @Override
    public void remove() {
        if (last != null) {
            model.undeploy(last, session);
        }
        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported remove"));
    }

    @Override
    public String toString() {
        return "FolderNutIdIterator{" +
                "repository='" + repository + '\'' +
                ", folder=" + folder +
                '}';
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    public static abstract class AbstractFolderNutIdIteratorModel implements FolderNutIdIteratorModel {

        public NutsId validate(NutsId id, NutsDescriptor t, NutsIdFilter filter,String repository,NutsSession session) {
            if (t != null) {
                if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                    NutsDescriptor nutsDescriptor = null;
                    try {
                        nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
                    } catch (Exception e) {
                        //throw new NutsException(e);
                    }
                    t = nutsDescriptor;
                }
                if (t != null && (filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
                    NutsId nutsId = t.getId().builder().setRepository(repository).build();
                    return nutsId;
                }
            }
            if (id != null) {
                if (filter == null || filter.acceptSearchId(new NutsSearchIdById(id), session)) {
                    NutsId nutsId = id.builder().setRepository(repository).build();
                    return nutsId;
                }
            }
            return null;
        }
        
        public NutsId parseId(Path pathname, Path rootPath, NutsIdFilter filter, String repository, NutsSession session) throws IOException {
            NutsDescriptor t = null;
            try {
                t = parseDescriptor(pathname, session);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return validate(null, t, filter,repository,session);
        }
    }

    public interface FolderNutIdIteratorModel {

        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;

        boolean isDescFile(Path pathname);

        NutsDescriptor parseDescriptor(Path pathname, NutsSession session) throws IOException;
        
        NutsId parseId(Path pathname, Path rootPath, NutsIdFilter filter, String repository, NutsSession session) throws IOException;

        
    }
}
