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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.env.NUserConfig;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.*;
import net.thevpc.nuts.runtime.standalone.repository.config.NRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNUpdateUserCmd extends AbstractNUpdateUserCmd {

    public DefaultNUpdateUserCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public NUpdateUserCmd run() {
        NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of();
        if (!(getCredentials()==null || NBlankable.isBlank(new String(getCredentials())))) {
            sec.checkAllowed(NConstants.Permissions.SET_PASSWORD, "set-user-credentials");
            String currentLogin = sec.getCurrentUsername();
            if (NBlankable.isBlank(login)) {
                if (!NConstants.Users.ANONYMOUS.equals(currentLogin)) {
                    login = currentLogin;
                } else {
                    throw new NIllegalArgumentException(NMsg.ofPlain("not logged in"));
                }
            }
            if (repository != null) {
                NRepositoryConfigModel rconf = NRepositoryConfigManagerExt.of(repository.config()).getModel();
                NUserConfig u = rconf.findUser(login).get();
                fillNutsUserConfig(u);

                rconf.setUser(u);

            } else {
                DefaultNWorkspaceConfigModel wconf = NConfigsExt.of(NConfigs.of()).getModel();
                NUserConfig u = wconf.getUser(login);
                if (u == null) {
                    throw new NIllegalArgumentException(NMsg.ofC("no such user %s", login));
                }

                fillNutsUserConfig(u);
                wconf.setUser(u);
            }
        }
        return this;
    }

    protected void fillNutsUserConfig(NUserConfig u) {
        NWorkspaceSecurityManager wsec = NWorkspaceSecurityManager.of();
        String currentLogin = wsec.getCurrentUsername();
        if (!currentLogin.equals(login)) {
            repository.security().checkAllowed(NConstants.Permissions.ADMIN, "set-user-credentials");
        }
        if (!wsec.isAllowed(NConstants.Permissions.ADMIN)) {
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
            if (NConstants.Permissions.ALL.contains(group.toLowerCase())
                    || NConstants.Permissions.ALL.contains("!" + group.toLowerCase())) {
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
