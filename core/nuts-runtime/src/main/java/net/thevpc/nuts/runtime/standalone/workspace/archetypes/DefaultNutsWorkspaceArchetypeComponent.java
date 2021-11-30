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
package net.thevpc.nuts.runtime.standalone.workspace.archetypes;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryURL;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

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
        List<NutsRepositoryURL> defaults = new ArrayList<>();
        for (NutsAddRepositoryOptions d : rm.getDefaultRepositories()) {
            if (d.getConfig() != null) {
                def.put(NutsPath.of(d.getConfig().getLocation(),session).toAbsolute().toString(), d);
            } else {
                def.put(NutsPath.of(d.getLocation(),session).toAbsolute().toString(), d);
            }
            defaults.add(NutsRepositoryURL.of(d.getName(), null));
        }
        defaults.add(NutsRepositoryURL.of(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null));
        NutsRepositoryURL[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(defaults.toArray(new NutsRepositoryURL[0]), DefaultNutsRepositoryDB.INSTANCE);
        for (NutsRepositoryURL s : br) {
            NutsAddRepositoryOptions oo = NutsRepositorySelectorHelper.createRepositoryOptions(s, false, session);
            String sloc = NutsPath.of(oo.getConfig().getLocation(),session).toAbsolute().toString();
            if (def.containsKey(sloc)) {
                session.repos().addRepository(def.get(sloc));
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
        boolean initializeAllPlatforms = boot.getBootCustomArgument("---init-platforms").getBooleanValue(true, false);
        if (initializeAllPlatforms && boot.getBootCustomArgument("---init-java").getBooleanValue(true, false)) {
            NutsWorkspaceUtils.of(session).installAllJVM();
        } else {
            //at least add current vm
            NutsWorkspaceUtils.of(session).installCurrentJVM();
        }
        Boolean initScripts = boot.getBootCustomArgument("---init-scripts").getBooleanValue(true, false);
        Boolean initLaunchers = boot.getBootCustomArgument("---init-launchers").getBooleanValue(true, false);
        if (initScripts || initLaunchers) {
            NutsWorkspaceUtils.of(session).installLaunchers(initLaunchers);
        }
        Boolean skipCompanions = session.boot().getBootOptions().getSkipCompanions();
        if (skipCompanions == null) {
            skipCompanions = false;
        }
        if (!skipCompanions) {
            NutsWorkspaceUtils.of(session).installCompanions();
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT + 2;
    }

}
