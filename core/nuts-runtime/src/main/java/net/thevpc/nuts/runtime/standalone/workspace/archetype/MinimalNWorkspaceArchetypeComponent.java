/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import net.thevpc.nuts.core.NConstants;


import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE + 1)
public class MinimalNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    public MinimalNWorkspaceArchetypeComponent() {
    }

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public void initializeWorkspace() {
        NRepositorySpec[] br =
                NRepositoryUtils.resolve(
                        NWorkspaceExt.of().getConfigModel().resolveBootRepositoriesList(),
                        new NRepositorySpec[0]);
        NWorkspace workspace = NWorkspace.of();
        for (NRepositorySpec s : br) {
            workspace.addRepository(s.toString());
        }
        //simple rights for minimal utilization
        NSecurityManager.of().addUser(NUserSpec.of(NConstants.Users.ANONYMOUS));
    }

    @Override
    public void startWorkspace() {
        NWorkspace workspace = NWorkspace.of();
        boolean initializeScripts = workspace.bootOptions().initScripts().onEmpty(true).get();
        boolean initializeLaunchers = workspace.bootOptions().initLaunchers().onEmpty(true).get();
        Boolean installCompanions = workspace.bootOptions().installCompanions().orElse(false);

        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetch.of().id(workspace.apiId())
                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                    .failFast(false).getResultId();
            if (api != null) {
                NWorkspaceUtils nWorkspaceUtils = NWorkspaceUtils.of();
                if (initializeScripts || initializeLaunchers) {
                    //api would be null if running in fatjar and no internet/maven is available
                    nWorkspaceUtils.installScriptsAndLaunchers(initializeLaunchers);
                }
                if (installCompanions) {
                    if (api != null) {
                        nWorkspaceUtils.installCompanions();
                    }
                }
            }
        }
    }
}
