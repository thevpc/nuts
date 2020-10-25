/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.*;

import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;

import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.filters.NutsSearchIdByDescriptor;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

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

    public FilesFoldersApiIdIterator(NutsWorkspace workspace, NutsRepository repository, String rootUrl, String basePath, NutsIdFilter filter, NutsSession session, FilesFoldersApi.IteratorModel model, int maxDepth) {
        this.repository = repository;
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
                FilesFoldersApi.Item[] children = FilesFoldersApi.getFilesAndFolders(true,true,file.path, session);
//                String[] childrenFiles = FilesFoldersApi.getFiles(file.path, session);
                visitedFoldersCount++;
                boolean deep = file.depth < maxDepth;
                for (FilesFoldersApi.Item child : children) {
                    if(child.isFolder()){
                        if (deep) {
                            //this is a folder
                            if (file.depth < maxDepth) {
                                stack.push(new PathAndDepth(file.path + "/" + child.getName(), true, file.depth + 1));
                            }
                        }
                    }else {
                        if (model.isDescFile(child.getName())) {
                            stack.push(new PathAndDepth(file.path + "/" + child.getName(), false, file.depth));
                        }
                    }
                }
            } else {
                visitedFilesCount++;
                NutsDescriptor t = null;
                try {
                    t = model.parseDescriptor(file.path, workspace.io()
                            .monitor().source(file.path).setSession(session).create(),
                            NutsFetchMode.LOCAL, repository, session);
                } catch (Exception ex) {
                    session.getWorkspace().log().of(FilesFoldersApi.class).with().level(Level.FINE).error(ex).log("Error parsing url : {0} : {1}",file.path,toString());//e.printStackTrace();
                }
                if (t != null) {
                    if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                        NutsDescriptor nutsDescriptor = null;
                        try {
                            nutsDescriptor = NutsWorkspaceExt.of(workspace).resolveEffectiveDescriptor(t, session);
                        } catch (Exception ex) {
                            session.getWorkspace().log().of(FilesFoldersApi.class).with().level(Level.FINE).error(ex).log("Error resolving effective descriptor for {0} in url {1} : {2}",t.getId(),file.path,ex.toString());//e.printStackTrace();
                        }
                        t = nutsDescriptor;
                    }
                    if (t != null && (filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
                        NutsId nutsId = t.getId().builder().setNamespace(repository.getName()).build();
//                        nutsId = nutsId.setAlternative(t.getAlternative());
                        last = nutsId;
                        break;
                    }
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
        throw new NutsUnsupportedOperationException(workspace, "Unsupported Remove");
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
