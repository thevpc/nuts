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
package net.vpc.app.nuts.core.archetypes;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

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
        return DEFAULT_SUPPORT + 2;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) {
        NutsWorkspaceRepositoryManager rm = workspace.getRepositoryManager();
        NutsRepository defaultRepo = rm.addRepository(new NutsRepositoryDefinition()
                .setCreate(true)
                .setName(NutsConstants.DEFAULT_REPOSITORY_NAME)
                .setType(NutsConstants.REPOSITORY_TYPE_NUTS)
        );
        defaultRepo.getConfigManager().setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
//        defaultRepo.addMirror("nuts-server", "http://localhost:8899", NutsConstants.REPOSITORY_TYPE_NUTS, true);
        NutsStoreLocationStrategy s = workspace.getConfigManager().getStoreLocationStrategy();
        for (NutsRepositoryDefinition d : rm.getDefaultRepositories()) {
            if (s == NutsStoreLocationStrategy.STANDALONE) {
                d = d.copy().setProxy(true);
            }
            rm.addRepository(d);
        }

//        workspace.getConfigManager().setEnv(NutsConstants.ENV_KEY_AUTOSAVE, "true");
        workspace.getConfigManager().addImports("net.vpc.app.nuts.toolbox");
        workspace.getConfigManager().addImports("net.vpc.app");
        workspace.getConfigManager().setEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);

        workspace.getSecurityManager().setUserRights(NutsConstants.USER_ANONYMOUS, NutsConstants.RIGHT_FETCH_DESC, NutsConstants.RIGHT_FETCH_CONTENT);

        //has read rights
        workspace.getSecurityManager().addUser("user", "user",
                NutsConstants.RIGHT_FETCH_DESC,
                NutsConstants.RIGHT_FETCH_CONTENT,
                NutsConstants.RIGHT_DEPLOY,
                NutsConstants.RIGHT_UNDEPLOY,
                NutsConstants.RIGHT_PUSH,
                NutsConstants.RIGHT_SAVE_WORKSPACE,
                NutsConstants.RIGHT_SAVE_REPOSITORY
        );
        workspace.getSecurityManager().setUserRemoteIdentity("user", "contributor");
    }
}
