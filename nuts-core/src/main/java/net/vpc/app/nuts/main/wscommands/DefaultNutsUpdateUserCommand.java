/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsUpdateUserCommand;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;

import java.util.*;

import net.vpc.app.nuts.core.config.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
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
            ws.security().checkAllowed(NutsConstants.Permissions.SET_PASSWORD, "set-user-credentials");
            String currentLogin = ws.security().getCurrentUsername();
            if (CoreStringUtils.isBlank(login)) {
                if (!NutsConstants.Users.ANONYMOUS.equals(currentLogin)) {
                    login = currentLogin;
                } else {
                    throw new NutsIllegalArgumentException(ws, "Not logged in");
                }
            }
            if (repo != null) {
                NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(login);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "No such user " + login);
                }
                fillNutsUserConfig(u);

                NutsRepositoryConfigManagerExt.of(repo.config()).setUser(u, new NutsUpdateOptions().session(getValidSession()));

            } else {

                NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(login);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "No such user " + login);
                }

                fillNutsUserConfig(u);
                NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u, new NutsUpdateOptions().session(getValidSession()));
            }
        }
        return this;
    }

    protected void fillNutsUserConfig(NutsUserConfig u) {
        String currentLogin = ws.security().getCurrentUsername();
        if (!currentLogin.equals(login)) {
            repo.security().checkAllowed(NutsConstants.Permissions.ADMIN, "set-user-credentials");
        }
        if (!ws.security().isAllowed(NutsConstants.Permissions.ADMIN)) {
            ws.security().checkCredentials(u.getCredentials().toCharArray(),
                    getOldCredentials());
//
//            if (CoreStringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("Missing old password");
//            }
//            //check old password
//            if (CoreStringUtils.isEmpty(u.getCredentials()) || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
//                throw new NutsSecurityException("Invalid password");
//            }
        }
        if (getCredentials() != null) {
            u.setCredentials(CoreStringUtils.chrToStr(ws.security().createCredentials(getCredentials(), false, CoreStringUtils.strToChr(u.getCredentials()))));
        }
        if (getRemoteCredentials() != null) {
            u.setRemoteCredentials(CoreStringUtils.chrToStr(ws.security().createCredentials(getRemoteCredentials(), true, CoreStringUtils.strToChr(u.getRemoteCredentials()))));
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
