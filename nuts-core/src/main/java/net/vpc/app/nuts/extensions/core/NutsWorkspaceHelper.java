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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceHelper {

    public static List<NutsRepository> filterRepositories(List<NutsRepository> repos, NutsId id, NutsRepositoryFilter repositoryFilter, NutsSession session) {
        return filterRepositories(repos, id, repositoryFilter, session, false, null);
    }

    public static List<NutsRepository> filterRepositories(
            List<NutsRepository> repos,
            NutsId id,
            NutsRepositoryFilter repositoryFilter, NutsSession session,
            boolean sortByLevelDesc,
            final Comparator<NutsRepository> postComp) {
        class RepoAndLevel {

            NutsRepository r;
            int level;

            public RepoAndLevel(NutsRepository r, int level) {
                this.r = r;
                this.level = level;
            }
        }
        List<RepoAndLevel> repos2 = new ArrayList<>();
//        List<Integer> reposLevels = new ArrayList<>();
        for (NutsRepository repository : repos) {
            if(repositoryFilter==null || repositoryFilter.accept(repository)) {
                int t = 0;
                try {
                    t = repository.getSupportLevel(id, session);
                } catch (Exception e) {
                    //ignore...
                }
                if (t > 0) {
                    repos2.add(new RepoAndLevel(repository, t));
//                    reposLevels.add(t);
                }
            }
        }
        if (sortByLevelDesc) {
            Collections.sort(repos2, new Comparator<RepoAndLevel>() {
                @Override
                public int compare(RepoAndLevel o1, RepoAndLevel o2) {
                    int x = Integer.compare(o2.level, o1.level);
                    if (x != 0) {
                        return x;
                    }
                    if (postComp != null) {
                        return postComp.compare(o1.r, o2.r);
                    }
                    return 0;
                }
            });
        }
        List<NutsRepository> ret = new ArrayList<>();
        for (RepoAndLevel repoAndLevel : repos2) {
            ret.add(repoAndLevel.r);
        }
        return ret;
    }

    public static NutsId configureFetchEnv(NutsId id, NutsWorkspace ws) {
        Map<String, String> face = id.getQueryMap();
        if (face.get(NutsConstants.QUERY_FACE) == null && face.get("arch") == null && face.get("os") == null && face.get("osdist") == null && face.get("platform") == null) {
            face.put("arch", ws.getConfigManager().getPlatformArch().toString());
            face.put("os", ws.getConfigManager().getPlatformOs().toString());
            face.put("osdist", ws.getConfigManager().getPlatformOsDist().toString());
            return id.setQuery(face);
        }
        return id;
    }

    public static String resolveImmediateWorkspacePath(String workspace, String defaultName, String workspaceRoot) {
        if (StringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        }
        return workspace;
    }

}
