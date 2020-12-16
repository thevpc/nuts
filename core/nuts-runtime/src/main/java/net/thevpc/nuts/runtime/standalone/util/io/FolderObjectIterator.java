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
 *
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
package net.thevpc.nuts.runtime.standalone.util.io;

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUnsupportedOperationException;
import net.thevpc.nuts.runtime.standalone.main.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.SearchTraceHelper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Created by vpc on 2/21/17.
 */
public class FolderObjectIterator<T> implements Iterator<T> {
//    private static final Logger LOG=Logger.getLogger(FolderNutIdIterator.class.getName());

    private T last;
    private Path lastPath;

    private static class PathAndDepth {

        private Path path;
        private int depth;

        public PathAndDepth(Path path, int depth) {
            this.path = path;
            this.depth = depth;
        }

    }

    private final Stack<PathAndDepth> stack = new Stack<>();
    private final Predicate<T> filter;
    private final NutsSession session;
    private final FolderIteratorModel<T> model;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private int maxDepth;
    private final NutsLogger LOG;
    private final String name;
    private final Path folder;

    public FolderObjectIterator(String name,Path folder, Predicate<T> filter, int maxDepth, NutsSession session, FolderIteratorModel<T> model) {
        this.session = session;
        this.filter = filter;
        this.model = model;
        this.name = name;
        this.maxDepth = maxDepth;
        if (folder == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        this.folder=folder;
        stack.push(new PathAndDepth(folder, 0));
        LOG = session.getWorkspace().log().of(DefaultNutsInstalledRepository.class);
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.pop();
            if (Files.isDirectory(file.path)) {
                SearchTraceHelper.progressIndeterminate("search " + CoreIOUtils.compressUrl(file.path.toString()), session);
                visitedFoldersCount++;
                boolean deep = maxDepth < 0 || file.depth < maxDepth;
                if (Files.isDirectory(file.path)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.path, new DirectoryStream.Filter<Path>() {
                        @Override
                        public boolean accept(Path pathname) throws IOException {
                            try {
                                return (deep && Files.isDirectory(pathname)) || model.isObjectFile(pathname);
                            } catch (Exception ex) {
                                session.getWorkspace().log().of(FolderObjectIterator.class).with().level(Level.FINE).error(ex).log("Unable to test desk file {0}" ,pathname);
                                return false;
                            }
                        }
                    })) {

                        for (Path item : stream) {
                            if (Files.isDirectory(item)) {
                                if (maxDepth < 0 || file.depth < maxDepth) {
                                    stack.push(new PathAndDepth(item, file.depth + 1));
                                }
                            } else {
                                stack.push(new PathAndDepth(item, file.depth));
                            }
                        }
                    } catch (IOException ex) {
                        LOG.with().error(ex).log("Unable to parse {0}",file.path);
                    }
                }
            } else {
                visitedFilesCount++;
                T t = null;
                try {
                    t = model.parseObject(file.path, session);
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
            model.remove(last, lastPath, session);
        } else {
            throw new NutsUnsupportedOperationException(session.getWorkspace(), "Unsupported Remove");
        }
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    public interface FolderIteratorModel<T> {

        default void remove(T object, Path objectPath, NutsSession session) throws NutsExecutionException {

        }

        boolean isObjectFile(Path pathname);

        T parseObject(Path pathname, NutsSession session) throws IOException;
    }

    @Override
    public String toString() {
        return "FolderIterator<"+name+">(folder="+folder+"; depth="+maxDepth+ ')';
    }
    
}
