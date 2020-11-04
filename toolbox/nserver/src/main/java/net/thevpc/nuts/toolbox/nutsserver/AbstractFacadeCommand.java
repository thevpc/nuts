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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nutsserver;

import net.thevpc.common.strings.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import net.thevpc.common.util.ListValueMap;
import net.thevpc.nuts.NutsWorkspaceSecurityManager;

/**
 * Created by vpc on 1/24/17.
 */
public abstract class AbstractFacadeCommand implements FacadeCommand {

    private String name;

    public AbstractFacadeCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(FacadeCommandContext context) throws IOException, LoginException {
        ListValueMap<String, String> parameters = context.getParameters();
        String userLogin = parameters.getFirst("ul");
        String userPasswordS = parameters.getFirst("up");
        char[] userPassword = userPasswordS == null ? null : userPasswordS.toCharArray();
        NutsWorkspaceSecurityManager secu = context.getWorkspace().security();
        userLogin = userLogin == null ? null :new String(secu.getCredentials(userLogin.toCharArray(), context.getSession()));
        userPassword = userPassword==null?null:secu.getCredentials(userPassword, context.getSession());
        if (!StringUtils.isBlank(userLogin)) {
            boolean loggedId = false;
            try {
                context.getWorkspace().security().login(userLogin, userPassword);
                loggedId = true;
                executeImpl(context);
            } finally {
                if (loggedId) {
                    context.getWorkspace().security().logout();
                }
            }
        } else {
            executeImpl(context);
        }
    }

    public abstract void executeImpl(FacadeCommandContext context) throws IOException, LoginException;
}
