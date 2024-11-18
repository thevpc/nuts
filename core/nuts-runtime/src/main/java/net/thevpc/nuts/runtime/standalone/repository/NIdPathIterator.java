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
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.common.iter.NIteratorBase;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;

/**
 * Created by vpc on 2/21/17.
 */
public class NIdPathIterator extends NIteratorBase<NId> {

    private final NRepository repository;
    private final StackOrQueue<PathAndDepth> stack;
    private final NIdFilter filter;
    private final NIdPathIteratorModel model;
    private final int maxDepth;
    private final NPath basePath;
    private final NPath rootFolder;
    private NId last;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private final NObjectElement extraProperties;
    private final String kind;

    public NIdPathIterator(NRepository repository, NPath rootFolder, NPath basePath, NIdFilter filter, NIdPathIteratorModel model, int maxDepth, String kind, NObjectElement extraProperties, boolean bfs) {
        this.stack = bfs?new OneQueue<>():new OneStack<>();
        this.repository = repository;
        this.extraProperties = extraProperties;
        this.kind = kind;
        this.filter = filter;
        this.model = model;
        this.maxDepth = maxDepth;
        NSession session = repository.getWorkspace().currentSession();
        if (rootFolder == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("could not iterate over null rootFolder"));
        }
        this.basePath = basePath;
        this.rootFolder = rootFolder;
        NPath startUrl = rootFolder;
        if (basePath != null) {
            if (basePath.isAbsolute()) {
                throw new NIllegalArgumentException(NMsg.ofC("expected relative path : %s", basePath));
            } else {
                startUrl = startUrl.resolve(basePath);
            }
        }
        stack.add(new PathAndDepth(startUrl, true, 0));
    }

    @Override
    public NElement describe() {
        return NElements.of().ofObject()
                .set("type", "ScanPath")
                .set("repository", repository == null ? null : repository.getName())
                .set("filter", NEDesc.describeResolveOrDestruct(filter))
                .set("path", NElements.of().toElement(basePath))
                .set("root", NElements.of().toElement(rootFolder))
                .set("maxDepth", maxDepth)
                .addAll(extraProperties)
                .build();
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.remove();
            NSession session = repository.getWorkspace().currentSession();
            if (file.folder) {
                session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %-8s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm()));
                visitedFoldersCount++;
                NPath[] children = new NPath[0];
                try {
                    children = file.path.stream().toArray(NPath[]::new);
                } catch (NIOException ex) {
                    //just log without stack trace!
                    session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %-8s %s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm(), NTexts.of().ofStyled("failed!", NTextStyle.error())));
                    NLogOp.of(NIdPathIterator.class).level(Level.FINE)//.error(ex)
                            .log(NMsg.ofJ("error listing : {0} : {1} : {2}", file.path, toString(), ex.toString()));
                } catch (Exception ex) {
                    session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %-8s %s %s", repository.getName(), kind, "search folder", file.path.toCompressedForm(), NTexts.of().ofStyled("failed!", NTextStyle.error())));
                    NLogOp.of(NIdPathIterator.class).level(Level.FINE).error(ex)
                            .log(NMsg.ofJ("error listing : {0} : {1}", file.path, toString()));
                }
                boolean deep = file.depth < maxDepth;
                for (NPath child : children) {
                    if (child.isDirectory()) {
                        if (deep) {
                            stack.add(new PathAndDepth(child, true, file.depth + 1));
                        }
                    } else {
                        if (model.isDescFile(child)) {
                            stack.add(new PathAndDepth(child, false, file.depth));
                        }
                    }
                }
            } else {
                visitedFilesCount++;

                NId t = null;
                try {
                    t = model.parseId(file.path, rootFolder, filter, repository);
                } catch (Exception ex) {
                    NLogOp.of(NIdPathIterator.class).level(Level.FINE).error(ex)
                            .log(NMsg.ofJ("error parsing : {0} : {1}", file.path, toString()));
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
            model.undeploy(last);
        }
        NSession session=repository.getWorkspace().currentSession();
        throw new NUnsupportedOperationException(NMsg.ofPlain("unsupported Remove"));
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

    private static class OneStack<T> implements StackOrQueue<T>{
        private Stack<T> all=new Stack<T>();

        @Override
        public void add(T t) {
            all.push(t);
        }

        @Override
        public T remove() {
            return all.pop();
        }

        @Override
        public boolean isEmpty() {
            return all.isEmpty();
        }
    }
    private static class OneQueue<T> implements StackOrQueue<T>{
        private LinkedList<T> all=new LinkedList<>();

        @Override
        public void add(T t) {
            all.add(t);
        }

        @Override
        public T remove() {
            return all.removeFirst();
        }

        @Override
        public boolean isEmpty() {
            return all.isEmpty();
        }
    }
    private interface StackOrQueue<T>{
        void add(T t);
        T remove();
        boolean isEmpty();
    }
}
