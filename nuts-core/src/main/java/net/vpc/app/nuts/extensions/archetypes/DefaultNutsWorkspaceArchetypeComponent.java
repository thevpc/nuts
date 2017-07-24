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
public class DefaultNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT +2;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) throws IOException {
        NutsRepository defaultRepo = workspace.addRepository(NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_TYPE, true);
        defaultRepo.getConfig().setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
//        defaultRepo.addMirror("nuts-server", "http://localhost:8899", NutsConstants.DEFAULT_REPOSITORY_TYPE, true);

        workspace.addRepository("maven-local", "~/.m2/repository", "maven", true);

        workspace.addProxiedRepository("maven-central", "http://repo.maven.apache.org/maven2/", "maven", true);

        workspace.addProxiedRepository("maven-vpc-public", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "maven", true);

        workspace.getConfig().setEnv(NutsConstants.ENV_KEY_AUTOSAVE, "true");
        workspace.getConfig().addImport("net.vpc");
        workspace.getConfig().setEnv(NutsConstants.ENV_KEY_PASSPHRASE, NutsConstants.DEFAULT_PASSPHRASE);

        NutsSecurityEntityConfig anonymous = workspace.getConfig().getSecurity(NutsConstants.USER_ANONYMOUS);
        anonymous.addRight(NutsConstants.RIGHT_FETCH_DESC);
        anonymous.addRight(NutsConstants.RIGHT_FETCH_CONTENT);

        //has read rights
        workspace.addUser("user");
        workspace.setUserCredentials("user", "user");
        NutsSecurityEntityConfig user = workspace.getConfig().getSecurity("user");
        user.addRight(NutsConstants.RIGHT_FETCH_DESC);
        user.addRight(NutsConstants.RIGHT_FETCH_CONTENT);
        user.addRight(NutsConstants.RIGHT_DEPLOY);
        user.addRight(NutsConstants.RIGHT_UNDEPLOY);
        user.addRight(NutsConstants.RIGHT_PUSH);
        user.addRight(NutsConstants.RIGHT_SAVE_WORKSPACE);
        user.addRight(NutsConstants.RIGHT_SAVE_REPOSITORY);

        user.setMappedUser("contributor");
    }
}
