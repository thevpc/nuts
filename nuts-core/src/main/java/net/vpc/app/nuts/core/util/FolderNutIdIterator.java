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
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Stack;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.core.filters.NutsSearchIdByDescriptor;

/**
 * Created by vpc on 2/21/17.
 */
public class FolderNutIdIterator implements Iterator<NutsId> {

    private final String repository;
    private NutsId last;
    private final Stack<Path> stack = new Stack<>();
    private final NutsIdFilter filter;
    private final NutsRepositorySession session;
    private final NutsWorkspace workspace;
    private final FolderNutIdIteratorModel model;
    private long visitedFoldersCount;
    private long visitedFilesCount;
    private boolean deep;

    public FolderNutIdIterator(NutsWorkspace workspace, String repository, Path folder, NutsIdFilter filter, NutsRepositorySession session, FolderNutIdIteratorModel model, boolean deep) {
        this.repository = repository;
        this.session = session;
        this.filter = filter;
        this.workspace = workspace;
        this.model = model;
        this.deep = deep;
        if (folder == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        stack.push(folder);
    }

    @Override
    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            Path file = stack.pop();
            if (Files.isDirectory(file)) {
                visitedFoldersCount++;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path pathname) throws IOException {
                        try {
                            return (deep && Files.isDirectory(pathname)) || model.isDescFile(pathname);
                        } catch (Exception e) {
                            //ignore
                            return false;
                        }
                    }
                })) {
                    for (Path item : stream) {
                        stack.push(item);
                    }
                }catch(IOException ex){
                    //
                }
            } else {
                visitedFilesCount++;
                NutsDescriptor t = null;
                try {
                    t = model.parseDescriptor(file, session);
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
                    if (t != null && (filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), workspace))) {
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
        throw new NutsUnsupportedOperationException("Unsupported Remove");
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    public interface FolderNutIdIteratorModel {

        void undeploy(NutsId id, NutsRepositorySession session) throws NutsExecutionException;

        boolean isDescFile(Path pathname);

        NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException;
    }
}
