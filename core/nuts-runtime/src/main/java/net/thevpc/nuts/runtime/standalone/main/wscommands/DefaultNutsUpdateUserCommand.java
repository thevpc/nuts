/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsUpdateUserCommand;

import java.util.*;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNutsUpdateUserCommand extends AbstractNutsUpdateUserCommand {


    public DefaultNutsUpdateUserCommand(NutsWorkspace ws) {
        super(ws);
    }

    public DefaultNutsUpdateUserCommand(NutsRepository repo) {
        super(repo.getWorkspace());
    }

    @Override
    public NutsUpdateUserCommand run() {
        if (!CoreStringUtils.isBlank(getCredentials())) {
            ws.security().checkAllowed(NutsConstants.Permissions.SET_PASSWORD, "set-user-credentials", session);
            String currentLogin = ws.security().getCurrentUsername(session);
            if (CoreStringUtils.isBlank(login)) {
                if (!NutsConstants.Users.ANONYMOUS.equals(currentLogin)) {
                    login = currentLogin;
                } else {
                    throw new NutsIllegalArgumentException(ws, "not logged in");
                }
            }
            NutsSession session = getValidWorkspaceSession();
            if (repo != null) {
                NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(login);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "no such user " + login);
                }
                fillNutsUserConfig(u);

                NutsRepositoryConfigManagerExt.of(repo.config()).setUser(u, new NutsUpdateOptions().setSession(session));

            } else {

                NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(login, session);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "no such user " + login);
                }

                fillNutsUserConfig(u);
                NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u, new NutsUpdateOptions().setSession(session));
            }
        }
        return this;
    }

    protected void fillNutsUserConfig(NutsUserConfig u) {
        String currentLogin = ws.security().getCurrentUsername(session);
        if (!currentLogin.equals(login)) {
            repo.security().checkAllowed(NutsConstants.Permissions.ADMIN, "set-user-credentials", session);
        }
        if (!ws.security().isAllowed(NutsConstants.Permissions.ADMIN, session)) {
            ws.security().checkCredentials(u.getCredentials().toCharArray(),
                    getOldCredentials(), session);
//
//            if (CoreStringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("missing old password");
//            }
//            //check old password
//            if (CoreStringUtils.isEmpty(u.getCredentials()) || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
//                throw new NutsSecurityException("invalid password");
//            }
        }
        if (getCredentials() != null) {
            u.setCredentials(CoreStringUtils.chrToStr(ws.security().createCredentials(getCredentials(), false, CoreStringUtils.strToChr(u.getCredentials()), session)));
        }
        if (getRemoteCredentials() != null) {
            u.setRemoteCredentials(CoreStringUtils.chrToStr(ws.security().createCredentials(getRemoteCredentials(), true, CoreStringUtils.strToChr(u.getRemoteCredentials()), session)));
        }

        if (resetGroups) {
            u.setGroups(new String[0]);
        }
        if (resetPermissions) {
            u.setPermissions(new String[0]);
        }
        LinkedHashSet<String> g=new LinkedHashSet<>(u.getGroups()==null?new ArrayList<>():Arrays.asList(u.getGroups()));
        g.addAll(groups);
        for (String group : rm_groups) {
            g.remove(group);
        }
        u.setGroups(g.toArray(new String[0]));

        LinkedHashSet<String> r=new LinkedHashSet<>(u.getPermissions()==null?new ArrayList<>():Arrays.asList(u.getPermissions()));
        for (String group : permissions) {
            if (
                    NutsConstants.Permissions.ALL.contains(group.toLowerCase())
                    ||NutsConstants.Permissions.ALL.contains("!"+group.toLowerCase())
            ) {
                r.add(group);
            }
        }
        for (String group : rm_permissions) {
            r.remove(group);
        }
        u.setPermissions(r.toArray(new String[0]));

        if (remoteIdentity != null) {
            u.setRemoteIdentity(remoteIdentity);
        }
        if (remoteCredentials != null) {
            u.setRemoteCredentials(remoteIdentity);
        }
    }
}
