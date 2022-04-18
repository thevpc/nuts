/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.AbstractNutsUpdateUserCommand;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.*;
import net.thevpc.nuts.runtime.standalone.repository.config.NutsRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;

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
        checkSession();
        NutsSession ws = getSession();
        NutsWorkspaceSecurityManager sec = ws.security().setSession(ws);
        if (!(getCredentials()==null || NutsBlankable.isBlank(new String(getCredentials())))) {
            sec.checkAllowed(NutsConstants.Permissions.SET_PASSWORD, "set-user-credentials");
            String currentLogin = sec.getCurrentUsername();
            if (NutsBlankable.isBlank(login)) {
                if (!NutsConstants.Users.ANONYMOUS.equals(currentLogin)) {
                    login = currentLogin;
                } else {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not logged in"));
                }
            }
            if (repo != null) {
                NutsRepositoryConfigModel rconf = NutsRepositoryConfigManagerExt.of(repo.config()).getModel();
                NutsUserConfig u = rconf.getUser(login, ws);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, NutsMessage.cstyle("no such user %s", login));
                }
                fillNutsUserConfig(u);

                rconf.setUser(u, session);

            } else {
                DefaultNutsWorkspaceConfigModel wconf = NutsWorkspaceConfigManagerExt.of(session.config()).getModel();
                NutsUserConfig u = wconf.getUser(login, ws);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, NutsMessage.cstyle("no such user %s", login));
                }

                fillNutsUserConfig(u);
                wconf.setUser(u, session);
            }
        }
        return this;
    }

    protected void fillNutsUserConfig(NutsUserConfig u) {
        checkSession();
        NutsSession ws = getSession();
        NutsWorkspaceSecurityManager wsec = ws.security().setSession(ws);
        String currentLogin = wsec.getCurrentUsername();
        if (!currentLogin.equals(login)) {
            repo.security().setSession(ws).checkAllowed(NutsConstants.Permissions.ADMIN, "set-user-credentials");
        }
        if (!wsec.isAllowed(NutsConstants.Permissions.ADMIN)) {
            wsec.checkCredentials(u.getCredentials().toCharArray(),
                    getOldCredentials());
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
            u.setCredentials(CoreStringUtils.chrToStr(wsec.createCredentials(getCredentials(), false, CoreStringUtils.strToChr(u.getCredentials()))));
        }
        if (getRemoteCredentials() != null) {
            u.setRemoteCredentials(CoreStringUtils.chrToStr(wsec.createCredentials(getRemoteCredentials(), true, CoreStringUtils.strToChr(u.getRemoteCredentials()))));
        }

        if (resetGroups) {
            u.setGroups(Collections.emptyList());
        }
        if (resetPermissions) {
            u.setPermissions(Collections.emptyList());
        }
        LinkedHashSet<String> g = new LinkedHashSet<>(u.getGroups() == null ? new ArrayList<>() : (u.getGroups()));
        g.addAll(groups);
        for (String group : rm_groups) {
            g.remove(group);
        }
        u.setGroups(new ArrayList<>(g));

        LinkedHashSet<String> r = new LinkedHashSet<>(u.getPermissions() == null ? new ArrayList<>() : (u.getPermissions()));
        for (String group : permissions) {
            if (NutsConstants.Permissions.ALL.contains(group.toLowerCase())
                    || NutsConstants.Permissions.ALL.contains("!" + group.toLowerCase())) {
                r.add(group);
            }
        }
        for (String group : rm_permissions) {
            r.remove(group);
        }
        u.setPermissions(new ArrayList<>(r));

        if (remoteIdentity != null) {
            u.setRemoteIdentity(remoteIdentity);
        }
        if (remoteCredentials != null) {
            u.setRemoteCredentials(remoteIdentity);
        }
    }
}
