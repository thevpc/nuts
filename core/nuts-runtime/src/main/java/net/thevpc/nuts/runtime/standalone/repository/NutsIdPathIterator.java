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
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.nfo.NutsIteratorBase;
import net.thevpc.nuts.NutsDescribables;

import java.util.Stack;
import java.util.logging.Level;

/**
 * Created by vpc on 2/21/17.
 */
public class NutsIdPathIterator extends NutsIteratorBase<NutsId> {

    private final NutsRepository repository;
    private final Stack<PathAndDepth> stack = new Stack<>();
    private final NutsIdFilter filter;
    private final NutsSession session;
    private final NutsIdPathIteratorModel model;
    private final int maxDepth;
    private final NutsPath basePath;
    private final NutsPath rootFolder;
    private NutsId last;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private NutsObjectElement extraProperties;

    public NutsIdPathIterator(NutsRepository repository, NutsPath rootFolder, NutsPath basePath, NutsIdFilter filter, NutsSession session, NutsIdPathIteratorModel model, int maxDepth, NutsObjectElement extraProperties) {
        this.repository = repository;
        this.extraProperties = extraProperties;
        this.session = session;
        this.filter = filter;
        this.model = model;
        this.maxDepth = maxDepth;
        if (rootFolder == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("could not iterate over null rootFolder"));
        }
        this.basePath = basePath;
        this.rootFolder = rootFolder;
        NutsPath startUrl = rootFolder;
        if (basePath != null) {
            if (basePath.isAbsolute()) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("expected relative path : %s", basePath));
            } else {
                startUrl = startUrl.resolve(basePath);
            }
        }
        stack.push(new PathAndDepth(startUrl, true, 0));
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return elems.ofObject()
                .set("type", "ScanPath")
                .set("repository", repository == null ? null : repository.getName())
                .set("filter", NutsDescribables.resolveOrDestruct(filter, elems))
                .set("path", elems.toElement(basePath))
                .set("root", elems.toElement(rootFolder))
                .set("maxDepth", maxDepth)
                .addAll(extraProperties)
                .build();
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.pop();
            if (file.folder) {
                session.getTerminal().printProgress("%-8s %s", "search folder", file.path.toCompressedForm());
                visitedFoldersCount++;
                NutsPath[] children = file.path.list().toArray(NutsPath[]::new);
                boolean deep = file.depth < maxDepth;
                for (NutsPath child : children) {
                    if (child.isDirectory()) {
                        if (deep) {
                            stack.push(new PathAndDepth(child, true, file.depth + 1));
                        }
                    } else {
                        if (model.isDescFile(child)) {
                            stack.push(new PathAndDepth(child, false, file.depth));
                        }
                    }
                }
            } else {
                visitedFilesCount++;

                NutsId t = null;
                try {
                    t = model.parseId(file.path, rootFolder, filter, repository, session);
                } catch (Exception ex) {
                    NutsLoggerOp.of(NutsIdPathIterator.class, session).level(Level.FINE).error(ex)
                            .log(NutsMessage.jstyle("error parsing : {0} : {1}", file.path, toString()));
                }
                if (t != null) {
                    last = t;
                    //break;
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
        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported Remove"));
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    private static class PathAndDepth {

        private final NutsPath path;
        private final int depth;
        private final boolean folder;

        public PathAndDepth(NutsPath path, boolean folder, int depth) {
            this.path = path;
            this.folder = folder;
            this.depth = depth;
        }

    }
}
