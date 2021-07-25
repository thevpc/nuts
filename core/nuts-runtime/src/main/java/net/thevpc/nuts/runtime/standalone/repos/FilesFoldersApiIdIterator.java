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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 * Created by vpc on 2/21/17.
 */
class FilesFoldersApiIdIterator implements Iterator<NutsId> {
//    private static final Logger LOG=Logger.getLogger(FilesFoldersApiIdIterator.class.getName());

    private final NutsRepository repository;
    private final Stack<PathAndDepth> stack = new Stack<>();
    private final NutsIdFilter filter;
    private final NutsSession session;
    private final NutsWorkspace workspace;
    private final FilesFoldersApi.IteratorModel model;
    private final int maxDepth;
    private NutsId last;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private String rootUrl;
    private RemoteRepoApi strategy;

    public FilesFoldersApiIdIterator(NutsWorkspace workspace, NutsRepository repository, String rootUrl, String basePath, NutsIdFilter filter,
            RemoteRepoApi strategy,
            NutsSession session, FilesFoldersApi.IteratorModel model, int maxDepth) {
        this.repository = repository;
        this.strategy = strategy;
        if (strategy != RemoteRepoApi.DIR_LIST && strategy != RemoteRepoApi.DIR_TEXT) {
            throw new NutsUnexpectedException(session, NutsMessage.cstyle("unexpected strategy ", strategy));
        }
        this.session = session;
        this.filter = filter;
        this.workspace = workspace;
        this.model = model;
        this.maxDepth = maxDepth;
        if (rootUrl == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        if (rootUrl.endsWith("/") && !rootUrl.endsWith("//")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }
        this.rootUrl = rootUrl;
        String startUrl = rootUrl;
        if (basePath != null && basePath.length() > 0 && !basePath.equals("/")) {
            if (!startUrl.endsWith("/") && !basePath.startsWith("/")) {
                startUrl += "/";
            }
            startUrl += basePath;
        }
        stack.push(new PathAndDepth(startUrl, true, 0));
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            PathAndDepth file = stack.pop();
            if (file.folder) {
                FilesFoldersApi.Item[] children = FilesFoldersApi.getDirItems(true, true, strategy, file.path, session);
//                String[] childrenFiles = FilesFoldersApi.getFiles(file.path, session);
                visitedFoldersCount++;
                boolean deep = file.depth < maxDepth;
                for (FilesFoldersApi.Item child : children) {
                    if (child.isFolder()) {
                        if (deep) {
                            //this is a folder
                            if (file.depth < maxDepth) {
                                stack.push(new PathAndDepth(file.path + "/" + child.getName(), true, file.depth + 1));
                            }
                        }
                    } else {
                        if (model.isDescFile(child.getName())) {
                            stack.push(new PathAndDepth(file.path + "/" + child.getName(), false, file.depth));
                        }
                    }
                }
            } else {
                visitedFilesCount++;

                NutsId t = null;
                try {
                    t = model.parseId(file.path, rootUrl, filter, repository, session);
                } catch (Exception ex) {
                    session.getWorkspace().log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).error(ex).log("error parsing url : {0} : {1}", file.path, toString());//e.printStackTrace();
                }
                if (t != null) {
                    last = t;
                    break;
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

        private String path;
        private int depth;
        private boolean folder;

        public PathAndDepth(String path, boolean folder, int depth) {
            this.path = path;
            this.folder = folder;
            this.depth = depth;
        }

    }
}
