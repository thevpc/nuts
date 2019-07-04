/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.*;

import java.util.Iterator;
import java.util.Stack;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.filters.NutsSearchIdByDescriptor;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 * Created by vpc on 2/21/17.
 */
class FilesFoldersApiIdIterator implements Iterator<NutsId> {

    private final String repository;
    private final Stack<PathAndDepth> stack = new Stack<>();
    private final NutsIdFilter filter;
    private final NutsRepositorySession session;
    private final NutsWorkspace workspace;
    private final FilesFoldersApi.IteratorModel model;
    private final int maxDepth;
    private NutsId last;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private String rootUrl;

    public FilesFoldersApiIdIterator(NutsWorkspace workspace, String repository, String rootUrl, String basePath, NutsIdFilter filter, NutsRepositorySession session, FilesFoldersApi.IteratorModel model, int maxDepth) {
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
                String[] childrenFolders = FilesFoldersApi.getFolders(file.path, session.getSession());
                String[] childrenFiles = FilesFoldersApi.getFiles(file.path, session.getSession());
                visitedFoldersCount++;
                boolean deep = file.depth < maxDepth;
                if (deep && childrenFolders != null) {
                    for (String child : childrenFolders) {
                        if (file.depth < maxDepth) {
                            stack.push(new PathAndDepth(file.path + "/" + child, true, file.depth + 1));
                        }
                    }
                }
                if (childrenFiles != null) {
                    for (String child : childrenFiles) {
                        if (model.isDescFile(child)) {
                            stack.push(new PathAndDepth(file.path + "/" + child, false, file.depth));
                        }
                    }
                }
            } else {
                visitedFilesCount++;
                NutsDescriptor t = null;
                try {
                    t = model.parseDescriptor(file.path, workspace.io()
                            .monitor().source(file.path).session(session.getSession()).create(),
                            session);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if (t != null) {
                    if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                        NutsDescriptor nutsDescriptor = null;
                        try {
                            nutsDescriptor = NutsWorkspaceExt.of(workspace).resolveEffectiveDescriptor(t, session.getSession());
                        } catch (Exception e) {
                            //throw new NutsException(e);
                        }
                        t = nutsDescriptor;
                    }
                    if (t != null && (filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session.getSession()))) {
                        NutsId nutsId = t.getId().setNamespace(repository);
                        nutsId = nutsId.setAlternative(t.getAlternative());
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
