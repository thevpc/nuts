/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NBlankable;

/**
 * Created by vpc on 1/23/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNWorkspaceArchetypeComponent implements NWorkspaceArchetypeComponent {
    private NLog LOG;

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void initializeWorkspace(NSession session) {
        this.LOG = NLog.of(DefaultNWorkspaceArchetypeComponent.class, session);
        NConfigs nConfigs = NConfigs.of(session);
        DefaultNConfigs rm = (DefaultNConfigs) nConfigs;
        LinkedHashMap<String, NAddRepositoryOptions> def = new LinkedHashMap<>();
        List<NRepositoryLocation> defaults = new ArrayList<>();
        for (NAddRepositoryOptions d : rm.getDefaultRepositories()) {
            if (d.getConfig() != null) {
                def.put(NPath.of(d.getConfig().getLocation().getPath(), session).toAbsolute().toString(), d);
            } else {
                def.put(NPath.of(d.getLocation(), session).toAbsolute().toString(), d);
            }
            defaults.add(NRepositoryLocation.of(d.getName(), null));
        }
        NWorkspaceExt.of(session).getModel().configModel.getStoredConfigMain().setEnablePreviewRepositories(session.isPreviewRepo());
        NWorkspaceExt.of(session).getModel().configModel.invalidateStoreModelMain();
        defaults.add(NRepositoryLocation.of(NConstants.Names.DEFAULT_REPOSITORY_NAME, null));
        NRepositoryLocation[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(defaults.toArray(new NRepositoryLocation[0]), NRepositoryDB.of(session));
        for (NRepositoryLocation s : br) {
            NAddRepositoryOptions oo = NRepositorySelectorHelper.createRepositoryOptions(s, false, session);
            String sloc = NPath.of(oo.getConfig().getLocation().getPath(), session).toAbsolute().toString();
            if (def.containsKey(sloc)) {
                NAddRepositoryOptions r = def.get(sloc).copy();
                if (!NBlankable.isBlank(s.getName())) {
                    r.setName(oo.getName());
                }
                NRepository nr = NRepositories.of(session).addRepository(r);
                if (
                        "system".equals(nr.getName())
                                && "system".equals(nr.config().getGlobalName())
                                && (
                                nr.config().getLocationPath() == null
                                        || !nr.config().getLocationPath().isDirectory()
                        )
                ) {
                    //runtime disable system repo if it is not accessible.
                    nr.setEnabled(false, session);
                }
                def.remove(sloc);
            } else {
                NRepositories.of(session).addRepository(oo
                        //.setTemporary(!defaults.containsKey(oo.getName()))
                );
            }
        }
//        for (NutsAddRepositoryOptions d : def.values()) {
//            ws.repos().addRepository(d);
//        }
        NImports.of(session).addImports(new String[]{
                "net.thevpc.nuts.toolbox",
                "net.thevpc"
        });

        NWorkspaceSecurityManager.of(session).updateUser(NConstants.Users.ANONYMOUS)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        NWorkspaceSecurityManager.of(session).addUser("user")
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
        return NConstants.Support.DEFAULT_SUPPORT + 2;
    }

}
