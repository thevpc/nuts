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
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsAddUserCommand;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.config.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class DefaultNutsAddUserCommand extends AbstractNutsAddUserCommand {

    public DefaultNutsAddUserCommand(NutsWorkspace ws) {
        super(ws);
    }

    public DefaultNutsAddUserCommand(NutsRepository repo) {
        super(repo.getWorkspace());
    }

    @Override
    public NutsAddUserCommand run() {
        if (CoreStringUtils.isBlank(getUsername())) {
            throw new NutsIllegalArgumentException(ws, "Invalid user");
        }
        if (repo != null) {
            NutsUserConfig security = new NutsUserConfig(getUsername(),
                    CoreStringUtils.chrToStr(repo.security().createCredentials(getCredentials(), false, null)),
                    getGroups(), getPermissions());
            security.setRemoteIdentity(getRemoteIdentity());
            security.setRemoteCredentials(CoreStringUtils.chrToStr(repo.security().createCredentials(getRemoteCredentials(), true, null)));
            NutsRepositoryConfigManagerExt.of(repo.config()).setUser(security, new NutsUpdateOptions().setSession(getSession()));
        } else {
            NutsUserConfig security = new NutsUserConfig(getUsername(),
                    CoreStringUtils.chrToStr(ws.security().createCredentials(getCredentials(), false, null)),
                    getGroups(), getPermissions());
            security.setRemoteIdentity(getRemoteIdentity());
            security.setRemoteCredentials(CoreStringUtils.chrToStr(ws.security().createCredentials(getRemoteCredentials(), true, null)));
            NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(security, new NutsUpdateOptions().setSession(getSession()));
        }
        return this;
    }

}
