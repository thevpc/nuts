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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

/**
 * Created by vpc on 1/23/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {
    private NutsLogger LOG;

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void initializeWorkspace(NutsSession session) {
        this.LOG = NutsLogger.of(DefaultNutsWorkspaceArchetypeComponent.class,session);
        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
        LinkedHashMap<String, NutsAddRepositoryOptions> def = new LinkedHashMap<>();
        List<NutsRepositoryLocation> defaults = new ArrayList<>();
        for (NutsAddRepositoryOptions d : rm.getDefaultRepositories()) {
            if (d.getConfig() != null) {
                def.put(NutsPath.of(d.getConfig().getLocation().getPath(),session).toAbsolute().toString(), d);
            } else {
                def.put(NutsPath.of(d.getLocation(),session).toAbsolute().toString(), d);
            }
            defaults.add(NutsRepositoryLocation.of(d.getName(), null));
        }
        defaults.add(NutsRepositoryLocation.of(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null));
        NutsRepositoryLocation[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(defaults.toArray(new NutsRepositoryLocation[0]), NutsRepositoryDB.of(session));
        for (NutsRepositoryLocation s : br) {
            NutsAddRepositoryOptions oo = NutsRepositorySelectorHelper.createRepositoryOptions(s, false, session);
            String sloc = NutsPath.of(oo.getConfig().getLocation().getPath(),session).toAbsolute().toString();
            if (def.containsKey(sloc)) {
                NutsAddRepositoryOptions r = def.get(sloc).copy();
                if(!NutsBlankable.isBlank(s.getName())){
                    r.setName(oo.getName());
                }
                session.repos().addRepository(r);
                def.remove(sloc);
            } else {
                session.repos().addRepository(oo
                        //.setTemporary(!defaults.containsKey(oo.getName()))
                );
            }
        }
//        for (NutsAddRepositoryOptions d : def.values()) {
//            ws.repos().addRepository(d);
//        }
        session.imports().addImports(new String[]{
                "net.thevpc.nuts.toolbox",
                "net.thevpc"
        });

        session.security().updateUser(NutsConstants.Users.ANONYMOUS)
                .resetPermissions()
                //.addRights(NutsConstants.Rights.FETCH_DESC, NutsConstants.Rights.FETCH_CONTENT)
                .run();

        //has read rights
        session.security().setSession(session).addUser("user")
                .setCredentials("user".toCharArray()).addPermissions(
                        NutsConstants.Permissions.FETCH_DESC,
                        NutsConstants.Permissions.FETCH_CONTENT,
                        NutsConstants.Permissions.DEPLOY,
                        NutsConstants.Permissions.UNDEPLOY,
                        NutsConstants.Permissions.PUSH,
                        NutsConstants.Permissions.SAVE
                ).setRemoteIdentity("contributor")
                .run();
    }

    @Override
    public void startWorkspace(NutsSession session) {
        NutsBootManager boot = session.boot();
        boolean initializeAllPlatforms = CoreNutsUtils.isCustomTrue("---init-platforms",session);
        if (initializeAllPlatforms && CoreNutsUtils.isCustomTrue("---init-java",session)) {
            NutsWorkspaceUtils.of(session).installAllJVM();
        } else {
            //at least add current vm
            NutsWorkspaceUtils.of(session).installCurrentJVM();
        }
        Boolean initScripts = CoreNutsUtils.isCustomTrue("---init-scripts",session);
        Boolean initLaunchers = CoreNutsUtils.isCustomTrue("---init-launchers",session);
        if (initScripts || initLaunchers) {
            NutsWorkspaceUtils.of(session).installLaunchers(initLaunchers);
        }
        Boolean skipCompanions = session.boot().getBootOptions().getSkipCompanions().orElse(false);
        if (!skipCompanions) {
            NutsWorkspaceUtils.of(session).installCompanions();
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT + 2;
    }

}
