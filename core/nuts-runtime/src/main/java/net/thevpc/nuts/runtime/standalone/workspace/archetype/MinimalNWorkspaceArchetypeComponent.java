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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NLog;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class MinimalNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NLog LOG;

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public void initializeWorkspace(NSession session) {
        this.LOG = NLog.of(MinimalNWorkspaceArchetypeComponent.class, session);
//        NutsWorkspace ws = session.getWorkspace();

        DefaultNConfigs rm = (DefaultNConfigs) NConfigs.of(session);
//        defaults.put(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null);
        NRepositoryLocation[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(
                new NRepositoryLocation[0], NRepositoryDB.of(session));
        NRepositories repos = NRepositories.of(session);
        for (NRepositoryLocation s : br) {
            repos.addRepository(s.toString());
        }
        //simple rights for minimal utilization
        NUpdateUserCommand uu = NWorkspaceSecurityManager.of(session)
                .updateUser(NConstants.Users.ANONYMOUS);
//        for (String right : NutsConstants.Rights.RIGHTS) {
//            if (!NutsConstants.Rights.ADMIN.equals(right)) {
//                uu.addRights(right);
//            }
//        }
        uu.run();
    }

    @Override
    public void startWorkspace(NSession session) {
        NBootManager boot = NBootManager.of(session);
        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(true).get(session);
        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = boot.getBootOptions().getInitScripts().ifEmpty(true).get(session);
        boolean initializeLaunchers = boot.getBootOptions().getInitLaunchers().ifEmpty(true).get(session);

        if (initializeJava) {
            NWorkspaceUtils.of(session).installAllJVM();
        } else {
            NWorkspaceUtils.of(session).installCurrentJVM();
        }
        if (initializeScripts || initializeLaunchers) {
            NWorkspaceUtils.of(session).installScriptsAndLaunchers(initializeLaunchers);
        }
        Boolean installCompanions = NBootManager.of(session).getBootOptions().getInstallCompanions().orElse(false);
        if (installCompanions) {
            NWorkspaceUtils.of(session).installCompanions();
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NCallableSupport.DEFAULT_SUPPORT + 1;
    }
}
