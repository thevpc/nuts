/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Created by vpc on 2/21/17.
 */
public class FolderObjectIterator<T> extends NIteratorBase<T> {
//    private static final Logger LOG=Logger.getLogger(FolderNutIdIterator.class.getName());

    private final Stack<PathAndDepth> stack = new Stack<>();
    private final Predicate<T> filter;
    private final FolderIteratorModel<T> model;
    private final String name;
    private final NPath folder;
    private T last;
    private NPath lastPath;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private final int maxDepth;

    public FolderObjectIterator(String name, NPath folder, Predicate<T> filter, int maxDepth, FolderIteratorModel<T> model) {
        this.filter = filter;
        this.model = model;
        this.name = name;
        this.maxDepth = maxDepth;
        NAssert.requireNonNull(folder, "folder");
        this.folder = folder;
        stack.push(new PathAndDepth(folder, 0));
    }

    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .name("ScanPath")
                .addParam(NElement.ofString(name))
                .set("path", NDescribables.describeResolveOrDestruct(folder))
                .set("maxDepth", maxDepth)
                .set("filter", NDescribables.describeResolveOrDestruct(filter))
                .build();
    }

    private NLog LOG() {
        return NLog.of(DefaultNInstalledRepository.class);
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.pop();
            if (file.path.isDirectory()) {
                NSession.of().getTerminal().printProgress(NMsg.ofC("%-8s %s", "browse", file.path.toCompressedForm()));
                visitedFoldersCount++;
                boolean deep = maxDepth < 0 || file.depth < maxDepth;
                if (file.path.isDirectory()) {
                    try {
                        file.path.stream().filter(
                                        pathname -> {
                                            try {
                                                return (deep && pathname.isDirectory()) || model.isObjectFile(pathname);
                                            } catch (Exception ex) {
                                                NLogOp.of(FolderObjectIterator.class).level(Level.FINE).error(ex)
                                                        .log(NMsg.ofC("unable to test desk file %s", pathname));
                                                return false;
                                            }
                                        }
                                ).redescribe(NDescribables.ofDesc("isDirectory || isObjectFile"))
                                .forEach(item -> {
                                    if (item.isDirectory()) {
                                        if (maxDepth < 0 || file.depth < maxDepth) {
                                            stack.push(new PathAndDepth(item, file.depth + 1));
                                        }
                                    } else {
                                        stack.push(new PathAndDepth(item, file.depth));
                                    }
                                });
                    } catch (Exception ex) {
                        LOG().with().error(ex).log(
                                NMsg.ofC("unable to parse %s", file.path));
                    }
                }
            } else {
                visitedFilesCount++;
                T t = null;
                try {
                    t = model.parseObject(file.path);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if (t != null) {
                    if ((filter == null || filter.test(t))) {
                        last = t;
                        lastPath = file.path;
                        break;
                    }
                }
            }
        }
        return last != null;
    }

    @Override
    public T next() {
        T ret = last;
        last = null;
        lastPath = null;
        return ret;
    }

    @Override
    public void remove() {
        if (last != null) {
            model.remove(last, lastPath);
        } else {
            throw new NUnsupportedOperationException(NMsg.ofPlain("unsupported remove"));
        }
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    @Override
    public String toString() {
        return "FolderIterator<" + name + ">(folder=" + folder + "; depth=" + maxDepth + ')';
    }

    public interface FolderIteratorModel<T> {

        default void remove(T object, NPath objectPath) throws NExecutionException {

        }

        boolean isObjectFile(NPath pathname);

        T parseObject(NPath pathname) throws IOException;
    }

    private static class PathAndDepth {

        private final NPath path;
        private final int depth;

        public PathAndDepth(NPath path, int depth) {
            this.path = path;
            this.depth = depth;
        }

    }

}
