/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.archetypes;

import java.util.HashMap;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

import java.util.Map;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;

/**
 * Created by vpc on 1/23/17.
 */
@NutsSingleton
public class ServerNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void initializeWorkspace(NutsSession session) {
        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
        Map<String,String> defaults=new HashMap<>();
        defaults.put("maven-local", null);
        defaults.put("maven-central", null);
        defaults.put(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null);
        NutsRepositorySelector.Selection[] br = rm.getModel().resolveBootRepositoriesList().resolveSelectors(defaults);
            NutsRepositoryManager repos = session.repos().setSession(session);
        for (NutsRepositorySelector.Selection s : br) {
            repos.addRepository(s.toString());
        }
        NutsWorkspaceSecurityManager sec = session.security().setSession(session);

        //has read rights
        sec.addUser("guest").setCredentials("user".toCharArray()).addPermissions(
                NutsConstants.Permissions.FETCH_DESC,
                NutsConstants.Permissions.FETCH_CONTENT,
                NutsConstants.Permissions.DEPLOY
        ).run();

        //has write rights
        sec = session.security().setSession(session);
        sec.addUser("contributor").setCredentials("user".toCharArray()).addPermissions(
                NutsConstants.Permissions.FETCH_DESC,
                NutsConstants.Permissions.FETCH_CONTENT,
                NutsConstants.Permissions.DEPLOY,
                NutsConstants.Permissions.UNDEPLOY
        ).run();
    }
}
