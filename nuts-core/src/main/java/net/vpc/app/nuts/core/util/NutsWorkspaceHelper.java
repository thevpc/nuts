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
import net.vpc.app.nuts.core.DefaultNutsRepositorySession;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceHelper {

    public enum FilterMode {
        FIND,
        DEPLOY
    }

    public static List<NutsRepository> filterRepositories(List<NutsRepository> repos, FilterMode fmode, NutsId id, NutsRepositoryFilter repositoryFilter, NutsSession session, NutsFetchMode mode, NutsQueryOptions options) {
        return filterRepositories(repos, fmode, id, repositoryFilter, session, true, null, mode, options);
    }

    public static List<NutsRepository> filterRepositories(
            List<NutsRepository> repos, FilterMode fmode, NutsId id, NutsRepositoryFilter repositoryFilter,
            NutsSession session, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp, NutsFetchMode mode, NutsQueryOptions options) {
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
            if (repositoryFilter == null || repositoryFilter.accept(repository)) {

                int t = 0;
                try {
                    switch (fmode) {
                        case FIND: {
                            t = repository.config().getFindSupportLevel(id, mode, options.isTransitive());
                            break;
                        }
                        case DEPLOY: {
                            t = repository.config().getDeploymentSupportLevel(id, mode != NutsFetchMode.REMOTE, options.isTransitive());
                            break;
                        }
                    }

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
        Map<String, String> qm = id.getQueryMap();
        if (qm.get(NutsConstants.QUERY_FACE) == null && qm.get("arch") == null && qm.get("os") == null && qm.get("osdist") == null && qm.get("platform") == null) {
            qm.put("arch", ws.config().getPlatformArch().toString());
            qm.put("os", ws.config().getPlatformOs().toString());
            if (ws.config().getPlatformOsDist() != null) {
                qm.put("osdist", ws.config().getPlatformOsDist().toString());
            }
            return id.setQuery(qm);
        }
        return id;
    }

//    public static String resolveImmediateWorkspacePath(String workspace, String defaultName, String workspaceRoot) {
//        if (StringUtils.isEmpty(workspace)) {
//            File file = CoreIOUtils.expandPath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
//            workspace = file == null ? null : file.getPath();
//        } else {
//            File file = CoreIOUtils.expandPath(workspace, null, workspaceRoot);
//            workspace = file == null ? null : file.getPath();
//        }
//        return workspace;
//    }
    public static NutsRepositorySession createNoRepositorySession(NutsSession session, NutsFetchMode mode, NutsQueryOptions options) {
        return new DefaultNutsRepositorySession().setSession(session).setTransitive(options.isTransitive()).setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
    }

    public static NutsRepositorySession createRepositorySession(NutsSession session, NutsRepository repo, NutsFetchMode mode, NutsQueryOptions options) {
        return new DefaultNutsRepositorySession().setSession(session).setTransitive(options.isTransitive()).setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
    }

    public static NutsFetchMode[] resolveFetchModes(boolean offline) {
        return offline ? new NutsFetchMode[]{NutsFetchMode.LOCAL} : new NutsFetchMode[]{NutsFetchMode.LOCAL, NutsFetchMode.REMOTE};
    }

    public static NutsFetchStrategy validate(NutsFetchStrategy mode) {
        return mode == null ? NutsFetchStrategy.ONLINE : mode;
    }

}
