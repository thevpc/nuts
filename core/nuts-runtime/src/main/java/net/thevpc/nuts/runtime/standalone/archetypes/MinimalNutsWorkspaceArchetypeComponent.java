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
package net.thevpc.nuts.runtime.standalone.archetypes;

import java.util.HashMap;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

import java.util.Map;

import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;

/**
 * Created by vpc on 1/23/17.
 */
@NutsSingleton
public class MinimalNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {
    private NutsLogger LOG;

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public void initializeWorkspace(NutsSession session) {
        this.LOG = session.log().of(MinimalNutsWorkspaceArchetypeComponent.class);
//        NutsWorkspace ws = session.getWorkspace();

        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
        Map<String, String> defaults = new HashMap<>();
//        defaults.put(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null);
        NutsRepositorySelector.Selection[] br = rm.getModel().resolveBootRepositoriesList().resolveSelectors(defaults);
        NutsRepositoryManager repos = session.repos().setSession(session);
        for (NutsRepositorySelector.Selection s : br) {
            repos.addRepository(s.toString());
        }
        //simple rights for minimal utilization
        NutsUpdateUserCommand uu = session.security().setSession(session)
                .updateUser(NutsConstants.Users.ANONYMOUS);
//        for (String right : NutsConstants.Rights.RIGHTS) {
//            if (!NutsConstants.Rights.ADMIN.equals(right)) {
//                uu.addRights(right);
//            }
//        }
        uu.run();
    }

    @Override
    public void startWorkspace(NutsSession session) {
        NutsBootManager boot = session.boot();
        boolean initializeAllPlatforms = boot.getCustomBootOption("init-platforms").getBoolean(false, false);
        if (initializeAllPlatforms && boot.getCustomBootOption("init-java").getBoolean(false, false)) {
            NutsWorkspaceUtils.of(session).installAllJVM();
        } else {
            NutsWorkspaceUtils.of(session).installCurrentJVM();
        }
        Boolean initScripts = boot.getCustomBootOption("init-launchers").getBoolean(true, false);
        Boolean initLaunchers = boot.getCustomBootOption("init-launchers").getBoolean(false, false);
        if (initScripts || initLaunchers) {
            NutsWorkspaceUtils.of(session).installLaunchers(initLaunchers);
        }
        Boolean skipCompanions = session.boot().getBootOptions().getSkipCompanions();
        if (skipCompanions == null) {
            skipCompanions = true;
        }
        if (!skipCompanions) {
            NutsWorkspaceUtils.of(session).installCompanions();
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return DEFAULT_SUPPORT + 1;
    }
}
