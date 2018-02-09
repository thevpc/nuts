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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by vpc on 2/21/17.
 */
public class FolderNutIdIterator implements Iterator<NutsId> {

    private String repositoryId;
    private NutsId last;
    private Stack<File> stack = new Stack<File>();
    private NutsIdFilter filter;
    private NutsSession session;
    private NutsWorkspace workspace;
    private FolderNutIdIteratorModel model;
    private long visitedFoldersCount;
    private long visitedFilesCount;

    public FolderNutIdIterator(NutsWorkspace workspace, String repositoryId, File folder, NutsIdFilter filter, NutsSession session, FolderNutIdIteratorModel model) {
        this.repositoryId = repositoryId;
        this.session = session;
        this.filter = filter;
        this.workspace = workspace;
        this.model = model;
        if (folder == null) {
            throw new NullPointerException("Could not iterate over null folder");
        }
        stack.push(folder);
    }

    public boolean hasNext() {
        last = null;
        while (!stack.isEmpty()) {
            File file = stack.pop();
            if (file.isDirectory()) {
                visitedFoldersCount++;
                File[] listFiles = file.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        try {
                            return pathname.isDirectory() || model.isDescFile(pathname);
                        } catch (Exception e) {
                            //ignore
                            return false;
                        }
                    }
                });
                if (listFiles != null) {
                    for (File f : listFiles) {
                        stack.push(f);
                    }
                }
            } else {
                visitedFilesCount++;
                NutsDescriptor t = null;
                try {
                    t = model.parseDescriptor(file, session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (t != null) {
                    if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                        NutsDescriptor nutsDescriptor = null;
                        try {
                            nutsDescriptor = workspace.resolveEffectiveDescriptor(t, session);
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                        }
                        t = nutsDescriptor;
                    }
                    if (t != null && (filter == null || filter.accept(t.getId()))) {
                        NutsId nutsId = t.getId().setNamespace(repositoryId);
                        nutsId=nutsId.setFace(CoreStringUtils.isEmpty(t.getFace())?NutsConstants.QUERY_FACE_DEFAULT_VALUE :t.getFace());
                        last = nutsId;
                        break;
                    }
                }
            }
        }
        return last != null;
    }

    public NutsId next() {
        NutsId ret = last;
        last = null;
        return ret;
    }

    public void remove() {
        if (last != null) {
            model.undeploy(last, session);
        }
        throw new NutsExecutionException("Unsupported Remove",1);
    }

    public long getVisitedFoldersCount() {
        return visitedFoldersCount;
    }

    public long getVisitedFilesCount() {
        return visitedFilesCount;
    }

    public interface FolderNutIdIteratorModel {

        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;

        boolean isDescFile(File pathname);

        NutsDescriptor parseDescriptor(File pathname, NutsSession session) throws IOException;
    }
}
