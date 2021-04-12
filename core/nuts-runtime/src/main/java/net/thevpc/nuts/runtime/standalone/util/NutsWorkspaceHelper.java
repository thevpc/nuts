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
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsFetchStrategy;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryFilter;
import net.thevpc.nuts.NutsSession;

import java.util.*;

/**
 *
 * @author thevpc
 */
public class NutsWorkspaceHelper {
    public static NutsFetchStrategy validate(NutsFetchStrategy mode) {
        return mode == null ? NutsFetchStrategy.ONLINE : mode;
    }

    public static List<NutsRepository> _getEnabledRepositories(NutsRepository parent, NutsRepositoryFilter repositoryFilter, NutsSession session) {
        List<NutsRepository> repos = new ArrayList<>();
        if (parent.config().isSupportedMirroring()) {
            List<NutsRepository> subrepos = new ArrayList<>();
            boolean ok = false;
            for (NutsRepository repository : parent.config().setSession(session).getMirrors()) {
                if (repository.config().isEnabled()) {
                    if (repositoryFilter == null || repositoryFilter.acceptRepository(repository)) {
                        repos.add(repository);
                        ok = true;
                    }
                    if (!ok) {
                        subrepos.add(repository);
                    }
                }
            }
            for (NutsRepository subrepo : subrepos) {
                repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter, session));
            }
        }
        return repos;
    }
}
