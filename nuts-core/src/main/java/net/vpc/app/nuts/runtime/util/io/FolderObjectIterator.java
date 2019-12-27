/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.main.repos.DefaultNutsInstalledRepository;
import net.vpc.app.nuts.runtime.util.SearchTraceHelper;

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

    public FolderObjectIterator(Path folder, Predicate<T> filter, int maxDepth, NutsSession session, FolderIteratorModel<T> model) {
        this.session = session;
        this.filter = filter;
        this.model = model;
        this.maxDepth = maxDepth;
        if (folder == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        stack.push(new PathAndDepth(folder, 0));
        LOG = session.workspace().log().of(DefaultNutsInstalledRepository.class);
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
                            } catch (Exception e) {
                                session.getWorkspace().log().of(FolderObjectIterator.class).log(Level.FINE, "Unable to test desk file " + pathname, e);
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
                        LOG.with().error(ex).log("Unable to parse %s%n",file.path);
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
            throw new NutsUnsupportedOperationException(session.workspace(), "Unsupported Remove");
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
}
