/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
