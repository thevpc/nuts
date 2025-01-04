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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ServerNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NWorkspace workspace;

    public ServerNWorkspaceArchetypeComponent(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public void initializeWorkspace() {
        NRepositoryLocation[] br = NWorkspaceExt.of(workspace).getConfigModel().resolveBootRepositoriesList().resolve(
                new NRepositoryLocation[]{
                        NRepositoryLocation.ofName("maven-local"),
                        NRepositoryLocation.ofName("maven-central"),
                        NRepositoryLocation.ofName(NConstants.Names.DEFAULT_REPOSITORY_NAME),
                },
                NRepositoryDB.of()
        );
        for (NRepositoryLocation s : br) {
            workspace.addRepository(s.toString());
        }
        NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of();

        //has read rights
        sec.addUser("guest").setCredentials("user".toCharArray()).addPermissions(
                NConstants.Permissions.FETCH_DESC,
                NConstants.Permissions.FETCH_CONTENT,
                NConstants.Permissions.DEPLOY
        ).run();

        //has write rights
        sec = NWorkspaceSecurityManager.of();
        sec.addUser("contributor").setCredentials("user".toCharArray()).addPermissions(
                NConstants.Permissions.FETCH_DESC,
                NConstants.Permissions.FETCH_CONTENT,
                NConstants.Permissions.DEPLOY,
                NConstants.Permissions.UNDEPLOY
        ).run();
    }

    @Override
    public void startWorkspace() {
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = workspace.getBootOptions().getInitScripts().ifEmpty(true).get();
        boolean initializeLaunchers = workspace.getBootOptions().getInitLaunchers().ifEmpty(true).get();
        Boolean installCompanions = NWorkspace.of().getBootOptions().getInstallCompanions().orElse(false);

//        if (initializeJava) {
//            NWorkspaceUtils.of().installAllJVM();
//        } else {
//            //at least add current vm
//            NWorkspaceUtils.of().installCurrentJVM();
//        }
        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetchCmd.of().setId(workspace.getApiId()).setFailFast(false).getResultId();
            if (api != null) {
                if (initializeScripts || initializeLaunchers) {
                    //api would be null if running in fatjar and no internet/maven is available
                    NWorkspaceUtils.of(workspace).installScriptsAndLaunchers(initializeLaunchers);
                }
                if (installCompanions) {
                    if (api != null) {
                        NWorkspaceUtils.of(workspace).installCompanions();
                    }
                }
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
