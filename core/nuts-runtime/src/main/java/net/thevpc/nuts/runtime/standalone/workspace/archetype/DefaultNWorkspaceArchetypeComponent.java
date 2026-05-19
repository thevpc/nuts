/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import java.util.*;

import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NConstants;


import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NIsolationLevel;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE + 2)
public class DefaultNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {

    public DefaultNWorkspaceArchetypeComponent() {
    }

    @Override
    public String getName() {
        return "default";
    }

    private String defaultRepoDiscriminator(NRepositorySpec d) {
        NPath repositoriesRoot = NWorkspaceExt.of().getConfigModel().getRepositoriesRoot();
        if (!NBlankable.isBlank(d.sourceLocation())) {
            return NPath.of(d.sourceLocation().getPath()).toAbsolute(repositoriesRoot).toString();
        } else if (!NBlankable.isBlank(d.location())) {
            return NPath.of(d.location()).toAbsolute(repositoriesRoot).toString();
        } else if (!NBlankable.isBlank(d.name())) {
            return NPath.of(d.name()).toAbsolute(repositoriesRoot).toString();
        } else if (d.sourceModel() != null) {
            String n = NAssert.requireNamedNonBlank(
                    NStringUtils.firstNonBlank(d.sourceModel().getName(), d.sourceModel().getUuid()),
                    "RepositoryModel name"
            );
            return NPath.of(n).toAbsolute(NWorkspaceExt.of().getConfigModel().getRepositoriesRoot()).toString();
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("unable to load default repository location: %s", d.location()));
        }
    }

    @Override
    public void initializeWorkspace() {
        NWorkspace workspace = NWorkspace.of();
        List<NRepositorySpec> defaults =new ArrayList<>(workspace.getDefaultRepositories());
        NWorkspaceExt.of().getModel().configModel.getStoredConfigMain().setEnablePreviewRepositories(NSession.of().isPreviewRepo());
        NWorkspaceExt.of().getModel().configModel.invalidateStoreModelMain();
        defaults.add(new NRepositorySpec().sourceLocation(NRepositoryLocation.ofName(NConstants.Names.DEFAULT_REPOSITORY_NAME)));
        NRepositorySpec[] br = NRepositoryUtils.resolve(NWorkspaceExt.of().getConfigModel().resolveBootRepositoriesList(),defaults.toArray(new NRepositorySpec[0]));
        for (NRepositorySpec oo : br) {
            workspace.addRepository(oo);
        }
        workspace.addImports("net.thevpc");

        //has read rights
        try (NSecureString ss = NSecureString.ofSecure("user".toCharArray())) {
            NSecurityManager.of().addUser(
                    NUserSpec.of("user")
                            .setCredential(ss)
                            .setPermissions(
                                    Arrays.asList(
                                            NConstants.Permissions.FETCH_DESC,
                                            NConstants.Permissions.FETCH_CONTENT,
                                            NConstants.Permissions.DEPLOY,
                                            NConstants.Permissions.UNDEPLOY,
                                            NConstants.Permissions.PUSH,
                                            NConstants.Permissions.SAVE
                                    )
                            )
            );
        }
    }

    @Override
    public void startWorkspace() {
        NWorkspace workspace = NWorkspace.of();
        NIsolationLevel nIsolationLevel = workspace.getBootOptions().isolationLevel().orNull();
        if (nIsolationLevel == NIsolationLevel.MEMORY) {
            return;
        }
        boolean isolated = nIsolationLevel != null && nIsolationLevel.ordinal() >= NIsolationLevel.CONFINED.ordinal();
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = workspace.getBootOptions().initScripts().orElse(!isolated);
        boolean initializeLaunchers = workspace.getBootOptions().initLaunchers().orElse(!isolated);
        boolean installCompanions = workspace.getBootOptions().installCompanions().orElse(false);

//        if (initializeJava) {
//            NWorkspaceUtils.of().installAllJVM();
//        } else {
//            //at least add current vm
//            NWorkspaceUtils.of().installCurrentJVM();
//        }

        if (initializeScripts || initializeLaunchers || installCompanions) {
            NId api = NFetch.of().id(workspace.getApiId())
                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                    .failFast(false).getResultId();
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
