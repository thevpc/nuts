/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceHelper {

    public static List<NutsRepository> filterRepositories(List<NutsRepository> repos, NutsId id, NutsSession session) {
        return filterRepositories(repos, id, session, false, null);
    }

    public static List<NutsRepository> filterRepositories(List<NutsRepository> repos, NutsId id, NutsSession session, boolean sortByLevelDesc, final Comparator<NutsRepository> postComp) {
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

    public static NutsId configureFetchEnv(NutsId id) {
        Map<String, String> face = id.getQueryMap();
        if (face.get(NutsConstants.QUERY_FACE) == null && face.get("arch") == null && face.get("os") == null && face.get("osdist") == null && face.get("platform") == null) {
            face.put("arch", CorePlatformUtils.getArch());
            face.put("os", CorePlatformUtils.getOs());
            face.put("osdist", CorePlatformUtils.getOsdist());
            return id.setQuery(face);
        }
        return id;
    }

    public static String resolveImmediateWorkspacePath(String workspace, String defaultName, String workspaceRoot) {
        if (CoreStringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        }
        return workspace;
    }

}
