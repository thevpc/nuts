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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.NFetchStrategy;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NRepositoryFilter;

import java.util.*;

/**
 *
 * @author thevpc
 */
public class NWorkspaceHelper {
    public static NFetchStrategy validate(NFetchStrategy mode) {
        return mode == null ? NFetchStrategy.ONLINE : mode;
    }

    public static List<NRepository> _getEnabledRepositories(NRepository parent, NRepositoryFilter repositoryFilter) {
        List<NRepository> repos = new ArrayList<>();
        if (parent.config().isSupportedMirroring()) {
            List<NRepository> subrepos = new ArrayList<>();
            boolean ok = false;
            for (NRepository repository : parent.config().getMirrors()) {
                if (repository.isEnabled()) {
                    if (repositoryFilter == null || repositoryFilter.acceptRepository(repository)) {
                        repos.add(repository);
                        ok = true;
                    }
                    if (!ok) {
                        subrepos.add(repository);
                    }
                }
            }
            for (NRepository subrepo : subrepos) {
                repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter));
            }
        }
        return repos;
    }
}
