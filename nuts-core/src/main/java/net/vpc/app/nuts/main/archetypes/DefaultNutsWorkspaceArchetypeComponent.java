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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.archetypes;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.main.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

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
        for (NutsRepositoryDefinition d : rm.getDefaultRepositories()) {
            def.put(ws.io().expandPath(d.getLocation(),null), d);
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
                "net.vpc.app.nuts.toolbox",
                "net.vpc.app"
        }, new NutsAddOptions().setSession(session));

        ws.security().updateUser(NutsConstants.Users.ANONYMOUS)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        ws.security().addUser("user").setCredentials("user".toCharArray()).addPermissions(
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
