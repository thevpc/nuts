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
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.log.NLog;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ServerNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NLog LOG;

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public void initializeWorkspace(NSession session) {
        this.LOG = NLog.of(ServerNWorkspaceArchetypeComponent.class, session);
        DefaultNConfigs rm = (DefaultNConfigs) NConfigs.of(session);
        NRepositoryLocation[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(
                new NRepositoryLocation[]{
                        NRepositoryLocation.of("maven-local", null),
                        NRepositoryLocation.of("maven-central", null),
                        NRepositoryLocation.of(NConstants.Names.DEFAULT_REPOSITORY_NAME, null),
                },
                NRepositoryDB.of(session)
        );
        NRepositories repos = NRepositories.of(session);
        for (NRepositoryLocation s : br) {
            repos.addRepository(s.toString());
        }
        NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of(session);

        //has read rights
        sec.addUser("guest").setCredentials("user".toCharArray()).addPermissions(
                NConstants.Permissions.FETCH_DESC,
                NConstants.Permissions.FETCH_CONTENT,
                NConstants.Permissions.DEPLOY
        ).run();

        //has write rights
        sec = NWorkspaceSecurityManager.of(session);
        sec.addUser("contributor").setCredentials("user".toCharArray()).addPermissions(
                NConstants.Permissions.FETCH_DESC,
                NConstants.Permissions.FETCH_CONTENT,
                NConstants.Permissions.DEPLOY,
                NConstants.Permissions.UNDEPLOY
        ).run();
    }

    @Override
    public void startWorkspace(NSession session) {
        NBootManager boot = NBootManager.of(session);
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = boot.getBootOptions().getInitScripts().ifEmpty(true).get(session);
        boolean initializeLaunchers = boot.getBootOptions().getInitLaunchers().ifEmpty(true).get(session);
        Boolean installCompanions = NBootManager.of(session).getBootOptions().getInstallCompanions().orElse(false);

//        if (initializeJava) {
//            NWorkspaceUtils.of(session).installAllJVM();
//        } else {
//            //at least add current vm
//            NWorkspaceUtils.of(session).installCurrentJVM();
//        }
        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetchCmd.of(session).setId(session.getWorkspace().getApiId()).setFailFast(false).getResultId();
            if (api != null) {
                if (initializeScripts || initializeLaunchers) {
                    //api would be null if running in fatjar and no internet/maven is available
                    NWorkspaceUtils.of(session).installScriptsAndLaunchers(initializeLaunchers);
                }
                if (installCompanions) {
                    if (api != null) {
                        NWorkspaceUtils.of(session).installCompanions();
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
