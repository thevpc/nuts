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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NBlankable;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NWorkspace workspace;

    public DefaultNWorkspaceArchetypeComponent(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void initializeWorkspace() {
        LinkedHashMap<String, NAddRepositoryOptions> def = new LinkedHashMap<>();
        List<NRepositoryLocation> defaults = new ArrayList<>();
        for (NAddRepositoryOptions d : workspace.getDefaultRepositories()) {
            if (d.getConfig() != null) {
                def.put(NPath.of(d.getConfig().getLocation().getPath()).toAbsolute().toString(), d);
            } else {
                def.put(NPath.of(d.getLocation()).toAbsolute().toString(), d);
            }
            defaults.add(NRepositoryLocation.of(d.getName(), (String) null));
        }
        NWorkspaceExt.of().getModel().configModel.getStoredConfigMain().setEnablePreviewRepositories(NSession.of().isPreviewRepo());
        NWorkspaceExt.of().getModel().configModel.invalidateStoreModelMain();
        defaults.add(NRepositoryLocation.ofName(NConstants.Names.DEFAULT_REPOSITORY_NAME));
        NRepositoryLocation[] br = NWorkspaceExt.of(workspace).getConfigModel().resolveBootRepositoriesList().resolve(defaults.toArray(new NRepositoryLocation[0]), NRepositoryDB.of());
        for (NRepositoryLocation s : br) {
            NAddRepositoryOptions oo = NRepositorySelectorHelper.createRepositoryOptions(s, false);
            String sloc = NPath.of(oo.getConfig().getLocation().getPath()).toAbsolute().toString();
            if (def.containsKey(sloc)) {
                NAddRepositoryOptions r = def.get(sloc).copy();
                if (!NBlankable.isBlank(s.getName())) {
                    r.setName(oo.getName());
                }
                NRepository nr = workspace.addRepository(r);
                if (
                        "system".equals(nr.getName())
                                && "system".equals(nr.config().getGlobalName())
                                && (
                                nr.config().getLocationPath() == null
                                        || !nr.config().getLocationPath().isDirectory()
                        )
                ) {
                    //runtime disable system repo if It's not accessible.
                    nr.setEnabled(false);
                }
                def.remove(sloc);
            } else {
                workspace.addRepository(oo
                        //.setTemporary(!defaults.containsKey(oo.getName()))
                );
            }
        }
//        for (NutsAddRepositoryOptions d : def.values()) {
//            ws.repos().addRepository(d);
//        }
        NWorkspace.of().addImports(
                "net.thevpc.nuts.toolbox",
                "net.thevpc"
        );

        NWorkspaceSecurityManager.of().updateUser(NConstants.Users.ANONYMOUS)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        NWorkspaceSecurityManager.of().addUser("user")
                .setCredentials("user".toCharArray()).addPermissions(
                        NConstants.Permissions.FETCH_DESC,
                        NConstants.Permissions.FETCH_CONTENT,
                        NConstants.Permissions.DEPLOY,
                        NConstants.Permissions.UNDEPLOY,
                        NConstants.Permissions.PUSH,
                        NConstants.Permissions.SAVE
                ).setRemoteIdentity("contributor")
                .run();
    }

    @Override
    public void startWorkspace() {
        NIsolationLevel nIsolationLevel = workspace.getBootOptions().getIsolationLevel().orNull();
        if(nIsolationLevel == NIsolationLevel.MEMORY){
            return;
        }
        boolean isolated = nIsolationLevel == NIsolationLevel.SANDBOX
                || nIsolationLevel == NIsolationLevel.CONFINED
                ;
//        boolean initializePlatforms = boot.getBootOptions().getInitPlatforms().ifEmpty(false).get(session);
//        boolean initializeJava = boot.getBootOptions().getInitJava().ifEmpty(initializePlatforms).get(session);
        boolean initializeScripts = workspace.getBootOptions().getInitScripts().orElse(!isolated);
        boolean initializeLaunchers = workspace.getBootOptions().getInitLaunchers().orElse(!isolated);
        boolean installCompanions = workspace.getBootOptions().getInstallCompanions().orElse(false);

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
        return NConstants.Support.DEFAULT_SUPPORT + 2;
    }

}
