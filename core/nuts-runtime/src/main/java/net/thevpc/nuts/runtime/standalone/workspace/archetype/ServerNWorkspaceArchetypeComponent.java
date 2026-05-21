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

import net.thevpc.nuts.core.NConstants;


import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class ServerNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    public ServerNWorkspaceArchetypeComponent() {
    }

    @Override
    public String name() {
        return "server";
    }

    @Override
    public void initializeWorkspace() {
        NRepositorySpec[] br = NRepositoryUtils.resolve(NWorkspaceExt.of().getConfigModel().resolveBootRepositoriesList(),
                new NRepositorySpec[]{
                        new NRepositorySpec().sourceLocation(NRepositoryLocation.ofName("maven-local")),
                        new NRepositorySpec().sourceLocation(NRepositoryLocation.ofName("maven-central")),
                        new NRepositorySpec().sourceLocation(NRepositoryLocation.ofName(NConstants.Names.DEFAULT_REPOSITORY_NAME)),
                }
        );
        for (NRepositorySpec s : br) {
            NWorkspace.of().addRepository(s.toString());
        }
        NSecurityManager sec = NSecurityManager.of();

        try (NSecureString ss = NSecureString.ofSecure("user".toCharArray())) {
            NSecurityManager.of().addUser(
                    NUserSpec.of("guest")
                            .credential(ss)
                            .addPermissions(
                                    NConstants.Permissions.FETCH_DESC,
                                    NConstants.Permissions.FETCH_CONTENT,
                                    NConstants.Permissions.DEPLOY
                            )
            );
        }
        try (NSecureString ss = NSecureString.ofSecure("user".toCharArray())) {
            NSecurityManager.of().addUser(
                    NUserSpec.of("contributor")
                            .credential(ss)
                            .addPermissions(
                                    NConstants.Permissions.FETCH_DESC,
                                    NConstants.Permissions.FETCH_CONTENT,
                                    NConstants.Permissions.DEPLOY,
                                    NConstants.Permissions.UNDEPLOY
                            )
            );
        }
    }

    @Override
    public void startWorkspace() {
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        NWorkspace workspace = NWorkspace.of();
        boolean initializeScripts = workspace.bootOptions().initScripts().onEmpty(true).get();
        boolean initializeLaunchers = workspace.bootOptions().initLaunchers().onEmpty(true).get();
        Boolean installCompanions = workspace.bootOptions().installCompanions().orElse(false);

//        if (initializeJava) {
//            NWorkspaceUtils.of().installAllJVM();
//        } else {
//            //at least add current vm
//            NWorkspaceUtils.of().installCurrentJVM();
//        }
        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetch.of()
                    .id(workspace.apiId()).failFast(false)
                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                    .getResultId();
            if (api != null) {
                NWorkspaceUtils nWorkspaceUtils = NWorkspaceUtils.of();
                if (initializeScripts || initializeLaunchers) {
                    //api would be null if running in fatjar and no internet/maven is available
                    nWorkspaceUtils.installScriptsAndLaunchers(initializeLaunchers);
                }
                if (installCompanions) {
                    if (api != null) {
                        nWorkspaceUtils.installCompanions();
                    }
                }
            }
        }
    }

}
