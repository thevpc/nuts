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

import java.io.IOException;

/**
 * Created by vpc on 1/23/17.
 */
public class ServerNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {
    @Override
    public String getName() {
        return "server";
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) throws IOException {
        NutsRepository defaultRepo = workspace.addRepository(NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_TYPE, true);
        defaultRepo.getConfig().setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
        workspace.getConfig().setEnv(NutsConstants.ENV_KEY_PASSPHRASE, NutsConstants.DEFAULT_PASSPHRASE);


        //has read rights
        workspace.addUser("guest");
        workspace.setUserCredentials("guest", "user");
        NutsSecurityEntityConfig guest = workspace.getConfig().getSecurity("guest");
        guest.addRight(NutsConstants.RIGHT_FETCH_DESC);
        guest.addRight(NutsConstants.RIGHT_FETCH_CONTENT);
        guest.addRight(NutsConstants.RIGHT_DEPLOY);

        //has write rights
        workspace.addUser("contributor");
        workspace.setUserCredentials("contributor", "user");
        NutsSecurityEntityConfig contributor = workspace.getConfig().getSecurity("contributor");
        contributor.addRight(NutsConstants.RIGHT_FETCH_DESC);
        contributor.addRight(NutsConstants.RIGHT_FETCH_CONTENT);
        contributor.addRight(NutsConstants.RIGHT_DEPLOY);
        contributor.addRight(NutsConstants.RIGHT_UNDEPLOY);

    }
}
