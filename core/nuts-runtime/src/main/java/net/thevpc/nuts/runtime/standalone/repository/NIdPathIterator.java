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
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.iter.NIteratorBase;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.util.NLoggerOp;

import java.util.Stack;
import java.util.logging.Level;

/**
 * Created by vpc on 2/21/17.
 */
public class NIdPathIterator extends NIteratorBase<NId> {

    private final NRepository repository;
    private final Stack<PathAndDepth> stack = new Stack<>();
    private final NIdFilter filter;
    private final NSession session;
    private final NIdPathIteratorModel model;
    private final int maxDepth;
    private final NPath basePath;
    private final NPath rootFolder;
    private NId last;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private final NObjectElement extraProperties;
    private final String kind;

    public NIdPathIterator(NRepository repository, NPath rootFolder, NPath basePath, NIdFilter filter, NSession session, NIdPathIteratorModel model, int maxDepth, String kind, NObjectElement extraProperties) {
        this.repository = repository;
        this.extraProperties = extraProperties;
        this.kind = kind;
        this.session = session;
        this.filter = filter;
        this.model = model;
        this.maxDepth = maxDepth;
        if (rootFolder == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("could not iterate over null rootFolder"));
        }
        this.basePath = basePath;
        this.rootFolder = rootFolder;
        NPath startUrl = rootFolder;
        if (basePath != null) {
            if (basePath.isAbsolute()) {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("expected relative path : %s", basePath));
            } else {
                startUrl = startUrl.resolve(basePath);
            }
        }
        stack.push(new PathAndDepth(startUrl, true, 0));
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofObject()
                .set("type", "ScanPath")
                .set("repository", repository == null ? null : repository.getName())
                .set("filter", NDescribables.resolveOrDestruct(filter, session))
                .set("path", NElements.of(session).toElement(basePath))
                .set("root", NElements.of(session).toElement(rootFolder))
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
                session.getTerminal().printProgress("%-14s %-8s %-8s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm());
                visitedFoldersCount++;
                NPath[] children = new NPath[0];
                try {
                    children = file.path.stream().toArray(NPath[]::new);
                } catch (NIOException ex) {
                    //just log without stack trace!
                    session.getTerminal().printProgress("%-14s %-8s %-8s %s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm(), NTexts.of(session).ofStyled("failed!", NTextStyle.error()));
                    NLoggerOp.of(NIdPathIterator.class, session).level(Level.FINE)//.error(ex)
                            .log(NMsg.ofJstyle("error listing : {0} : {1} : {2}", file.path, toString(), ex.toString()));
                } catch (Exception ex) {
                    session.getTerminal().printProgress("%-14s %-8s %-8s %s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm(), NTexts.of(session).ofStyled("failed!", NTextStyle.error()));
                    NLoggerOp.of(NIdPathIterator.class, session).level(Level.FINE).error(ex)
                            .log(NMsg.ofJstyle("error listing : {0} : {1}", file.path, toString()));
                }
                boolean deep = file.depth < maxDepth;
                for (NPath child : children) {
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

                NId t = null;
                try {
                    t = model.parseId(file.path, rootFolder, filter, repository, session);
                } catch (Exception ex) {
                    NLoggerOp.of(NIdPathIterator.class, session).level(Level.FINE).error(ex)
                            .log(NMsg.ofJstyle("error parsing : {0} : {1}", file.path, toString()));
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
    public NId next() {
        NId ret = last;
        last = null;
        return ret;
    }

    @Override
    public void remove() {
        if (last != null) {
            model.undeploy(last, session);
        }
        throw new NUnsupportedOperationException(session, NMsg.ofPlain("unsupported Remove"));
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    private static class PathAndDepth {

        private final NPath path;
        private final int depth;
        private final boolean folder;

        public PathAndDepth(NPath path, boolean folder, int depth) {
            this.path = path;
            this.folder = folder;
            this.depth = depth;
        }

    }
}
