/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.archetypes;

import net.vpc.app.nuts.*;

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
        NutsWorkspaceConfigManager rm = ws.config();
        rm.addRepository(new NutsRepositoryDefinition()
                .setDeployOrder(10)
                .setCreate(true)
                .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                .setType(NutsConstants.RepoTypes.NUTS)
        );
//        defaultRepo.addMirror("nuts-server", "http://localhost:8899", NutsConstants.REPOSITORY_TYPE_NUTS, true);

        for (NutsRepositoryDefinition d : rm.getDefaultRepositories()) {
            rm.addRepository(d);
        }

//        workspace.getConfigManager().setEnv(NutsConstants.ENV_KEY_AUTOSAVE, "true");
        ws.config().addImports(new String[]{
                "net.vpc.app.nuts.toolbox",
                "net.vpc.app"
        }, new NutsAddOptions().session(session));

        ws.security().updateUser(NutsConstants.Users.ANONYMOUS)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        ws.security().addUser("user").credentials("user".toCharArray()).permissions(
                NutsConstants.Permissions.FETCH_DESC,
                NutsConstants.Permissions.FETCH_CONTENT,
                NutsConstants.Permissions.DEPLOY,
                NutsConstants.Permissions.UNDEPLOY,
                NutsConstants.Permissions.PUSH,
                NutsConstants.Permissions.SAVE
        ).remoteIdentity("contributor")
                .remoteIdentity("contributor")
                .run();
    }
}
