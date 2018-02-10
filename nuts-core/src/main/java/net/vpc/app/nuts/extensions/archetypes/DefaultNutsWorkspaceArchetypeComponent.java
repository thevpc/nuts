/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.archetypes;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

/**
 * Created by vpc on 1/23/17.
 */
public class DefaultNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 2;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) {
        NutsRepository defaultRepo = workspace.addRepository(NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_TYPE, true);
        defaultRepo.setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
//        defaultRepo.addMirror("nuts-server", "http://localhost:8899", NutsConstants.DEFAULT_REPOSITORY_TYPE, true);

        for (NutsRepositoryDefinition nutsRepositoryDefinition : workspace.getDefaultRepositories()) {
            if (nutsRepositoryDefinition.isProxied()) {
                workspace.addProxiedRepository(nutsRepositoryDefinition.getId(), nutsRepositoryDefinition.getLocation(), nutsRepositoryDefinition.getType(), true);
            } else {
                workspace.addRepository(nutsRepositoryDefinition.getId(), nutsRepositoryDefinition.getLocation(), nutsRepositoryDefinition.getType(), true);
            }
        }

        workspace.setEnv(NutsConstants.ENV_KEY_AUTOSAVE, "true");
        workspace.addImports("net.vpc");
        workspace.setEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);

        workspace.setUserRights(NutsConstants.USER_ANONYMOUS, NutsConstants.RIGHT_FETCH_DESC, NutsConstants.RIGHT_FETCH_CONTENT);

        //has read rights
        workspace.addUser("user", "user",
                NutsConstants.RIGHT_FETCH_DESC,
                NutsConstants.RIGHT_FETCH_CONTENT,
                NutsConstants.RIGHT_DEPLOY,
                NutsConstants.RIGHT_UNDEPLOY,
                NutsConstants.RIGHT_PUSH,
                NutsConstants.RIGHT_SAVE_WORKSPACE,
                NutsConstants.RIGHT_SAVE_REPOSITORY
        );
        workspace.setUserRemoteIdentity("user", "contributor");
    }
}
