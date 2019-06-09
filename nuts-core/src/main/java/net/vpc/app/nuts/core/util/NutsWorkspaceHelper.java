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
import java.util.*;
import net.vpc.app.nuts.core.DefaultNutsFetchCommand;
import net.vpc.app.nuts.core.DefaultNutsRepositorySession;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceHelper {

//    public static String resolveImmediateWorkspacePath(String workspace, String defaultName, String workspaceRoot) {
//        if (CoreStringUtils.isEmpty(workspace)) {
//            File file = CoreIOUtils.expandPath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
//            workspace = file == null ? null : file.getPath();
//        } else {
//            File file = CoreIOUtils.expandPath(workspace, null, workspaceRoot);
//            workspace = file == null ? null : file.getPath();
//        }
//        return workspace;
//    }
    public static NutsRepositorySession createNoRepositorySession(NutsWorkspace ws, NutsSession session, NutsFetchMode mode, NutsFetchCommand options) {
        return new DefaultNutsRepositorySession(ws,session)
                .setTransitive(options.isTransitive())
                .setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
    }

    public static NutsRepositorySession createRepositorySession(NutsWorkspace ws, NutsSession session, NutsRepository repo, NutsFetchMode mode, NutsFetchCommand options) {
        if(options==null){
            options=new DefaultNutsFetchCommand(repo.getWorkspace()).setIndexed(true);
        }
        return new DefaultNutsRepositorySession(ws,session).setTransitive(options.isTransitive()).setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
    }

    public static NutsFetchMode[] resolveFetchModes(boolean offline) {
        return offline ? new NutsFetchMode[]{NutsFetchMode.LOCAL} : new NutsFetchMode[]{NutsFetchMode.LOCAL, NutsFetchMode.REMOTE};
    }

    public static NutsFetchStrategy validate(NutsFetchStrategy mode) {
        return mode == null ? NutsFetchStrategy.ONLINE : mode;
    }

    public static List<NutsRepository> _getEnabledRepositories(NutsRepository parent, NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = new ArrayList<>();
        NutsRepository repo = (NutsRepository) parent;
        if (repo.config().isSupportedMirroring()) {
            List<NutsRepository> subrepos = new ArrayList<>();
            boolean ok = false;
            for (NutsRepository repository : repo.config().getMirrors()) {
                if (repository.config().isEnabled()) {
                    if (repositoryFilter == null || repositoryFilter.accept(repository)) {
                        repos.add(repository);
                        ok = true;
                    }
                    if (!ok) {
                        subrepos.add(repository);
                    }
                }
            }
            for (NutsRepository subrepo : subrepos) {
                repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter));
            }
        }
        return repos;
    }
}
