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
package net.thevpc.nuts.runtime.standalone.main.archetypes;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.main.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by vpc on 1/23/17.
 */
@NutsSingleton
public class DefaultNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return DEFAULT_SUPPORT + 2;
    }

    @Override
    public void initialize(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) ws.config();
        ws.repos().addRepository(NutsConstants.Names.DEFAULT_REPOSITORY_NAME,session);
        LinkedHashSet<String> br = new LinkedHashSet<>(rm.resolveBootRepositories());
        LinkedHashMap<String, NutsRepositoryDefinition> def = new LinkedHashMap<>();
        for (NutsRepositoryDefinition d : rm.getDefaultRepositories(session)) {
            def.put(ws.io().expandPath(d.getLocation(),null), d.setSession(session));
        }
        for (String s : br) {
            String sloc = ws.io().expandPath(CoreNutsUtils.repositoryStringToDefinition(s).getLocation(),null);
            if (def.containsKey(sloc)) {
                ws.repos().addRepository(def.get(sloc));
                def.remove(sloc);
            } else {
                ws.repos().addRepository(s,session);
            }
        }
        for (NutsRepositoryDefinition d : def.values()) {
            ws.repos().addRepository(d);
        }
        ws.imports().add(new String[]{
                "net.thevpc.nuts.toolbox",
                "net.thevpc"
        }, new NutsAddOptions().setSession(session));

        ws.security().updateUser(NutsConstants.Users.ANONYMOUS, session)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        ws.security().addUser("user", session).setCredentials("user".toCharArray()).addPermissions(
                NutsConstants.Permissions.FETCH_DESC,
                NutsConstants.Permissions.FETCH_CONTENT,
                NutsConstants.Permissions.DEPLOY,
                NutsConstants.Permissions.UNDEPLOY,
                NutsConstants.Permissions.PUSH,
                NutsConstants.Permissions.SAVE
        ).setRemoteIdentity("contributor")
                .run();
    }
}
