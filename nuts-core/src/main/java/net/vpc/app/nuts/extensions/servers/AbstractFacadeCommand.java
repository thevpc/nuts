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
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.app.nuts.extensions.util.ListMap;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * Created by vpc on 1/24/17.
 */
public abstract class AbstractFacadeCommand implements FacadeCommand {

    private String name;

    public AbstractFacadeCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void execute(FacadeCommandContext context) throws IOException, LoginException {
        ListMap<String, String> parameters = context.getParameters();
        String userLogin = parameters.getOne("ul");
        String userPassword = parameters.getOne("up");
        String passphrase = context.getWorkspace().getConfigManager().getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);
        userLogin = CoreStringUtils.isEmpty(userLogin) ? "" : new String(CoreSecurityUtils.httpDecrypt(userLogin, passphrase));
        userPassword = CoreStringUtils.isEmpty(userPassword) ? "" : new String(CoreSecurityUtils.httpDecrypt(userPassword, passphrase));
        if (!CoreStringUtils.isEmpty(userLogin)) {
            boolean loggedId = false;
            try {
                context.getWorkspace().getSecurityManager().login(userLogin, userPassword);
                loggedId = true;
                executeImpl(context);
            } finally {
                if (loggedId) {
                    context.getWorkspace().getSecurityManager().logout();
                }
            }
        } else {
            executeImpl(context);
        }
    }

    public abstract void executeImpl(FacadeCommandContext context) throws IOException, LoginException;
}
