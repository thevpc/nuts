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
package net.vpc.app.nuts.extensions.archetypes;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

/**
 * Created by vpc on 1/23/17.
 */
public class MinimalNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 1;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) {
        NutsRepository defaultRepo = workspace.getRepositoryManager().addRepository(NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_TYPE, true);
        defaultRepo.getConfigManager().setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
        workspace.getConfigManager().setEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);

        //simple rights for minimal utilization
        for (String right : NutsConstants.RIGHTS) {
            if (!NutsConstants.RIGHT_ADMIN.equals(right)) {
                workspace.getSecurityManager().addUserRights(NutsConstants.USER_ANONYMOUS, right);
            }
        }
    }
}
