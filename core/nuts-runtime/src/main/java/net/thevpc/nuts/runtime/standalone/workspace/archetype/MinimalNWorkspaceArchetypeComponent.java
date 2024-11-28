/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class MinimalNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NWorkspace workspace;

    public MinimalNWorkspaceArchetypeComponent(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public void initializeWorkspace() {
        NSession session = workspace.currentSession();
//        NutsWorkspace ws = session.getWorkspace();

        DefaultNConfigs rm = (DefaultNConfigs) NConfigs.of();
//        defaults.put(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null);
        NRepositoryLocation[] br = rm.getModel().resolveBootRepositoriesList().resolve(
                new NRepositoryLocation[0], NRepositoryDB.of());
        NRepositories repos = NRepositories.of();
        for (NRepositoryLocation s : br) {
            repos.addRepository(s.toString());
        }
        //simple rights for minimal utilization
        NUpdateUserCmd uu = NWorkspaceSecurityManager.of()
                .updateUser(NConstants.Users.ANONYMOUS);
//        for (String right : NutsConstants.Rights.RIGHTS) {
//            if (!NutsConstants.Rights.ADMIN.equals(right)) {
//                uu.addRights(right);
//            }
//        }
        uu.run();
    }

    @Override
    public void startWorkspace() {
        NSession session = workspace.currentSession();
        NBootManager boot = NBootManager.of();
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = boot.getBootOptions().getInitScripts().ifEmpty(true).get();
        boolean initializeLaunchers = boot.getBootOptions().getInitLaunchers().ifEmpty(true).get();
        Boolean installCompanions = NBootManager.of().getBootOptions().getInstallCompanions().orElse(false);

//        if (initializeJava) {
//            NWorkspaceUtils.of(session).installAllJVM();
//        } else {
//            NWorkspaceUtils.of(session).installCurrentJVM();
//        }
        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetchCmd.of().setId(session.getWorkspace().getApiId()).setFailFast(false).getResultId();
            if (api != null) {
                if (initializeScripts || initializeLaunchers) {
                    //api would be null if running in fatjar and no internet/maven is available
                    NWorkspaceUtils.of(session.getWorkspace()).installScriptsAndLaunchers(initializeLaunchers);
                }
                if (installCompanions) {
                    if (api != null) {
                        NWorkspaceUtils.of(session.getWorkspace()).installCompanions();
                    }
                }
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT + 1;
    }
}
