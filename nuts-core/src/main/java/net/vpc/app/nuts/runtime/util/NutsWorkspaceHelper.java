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
package net.vpc.app.nuts.runtime.util;

import net.vpc.app.nuts.*;
import java.util.*;
import net.vpc.app.nuts.main.wscommands.DefaultNutsFetchCommand;
import net.vpc.app.nuts.runtime.repos.DefaultNutsRepositorySession;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceHelper {

    public static NutsRepositorySession createNoRepositorySession(NutsSession session) {
        return createNoRepositorySession(session,NutsFetchMode.LOCAL, new DefaultNutsFetchCommand(session.getWorkspace()));
    }

    public static NutsRepositorySession createNoRepositorySession(NutsSession session, NutsFetchMode mode, NutsFetchCommand options) {
        return new DefaultNutsRepositorySession(null,session)
                .setTransitive(options.isTransitive())
                .setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
    }

    public static NutsRepositorySession createRepositorySession(NutsSession session, NutsRepository repo, NutsFetchMode mode, NutsFetchCommand options) {
        if (options == null) {
            options = new DefaultNutsFetchCommand(repo.getWorkspace()).indexed();
        }
        return new DefaultNutsRepositorySession(repo,session).setTransitive(options.isTransitive()).setIndexed(options.isIndexed()).setFetchMode(mode).setCached(options.isCached());
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
